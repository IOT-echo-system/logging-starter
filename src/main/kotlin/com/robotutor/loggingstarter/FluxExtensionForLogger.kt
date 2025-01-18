package com.robotutor.loggingstarter

import com.google.gson.JsonSyntaxException
import com.robotutor.loggingstarter.ReactiveContext.getPremisesId
import com.robotutor.loggingstarter.ReactiveContext.getTraceId
import com.robotutor.loggingstarter.serializer.DefaultSerializer
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Signal

fun <T> Flux<T>.logOnError(
    logger: Logger,
    errorCode: String? = null,
    errorMessage: String,
    additionalDetails: Map<String, Any?> = emptyMap(),
    searchableFields: Map<String, Any?> = emptyMap(),
    skipAdditionalDetails: Boolean = false,
): Flux<T> {
    return doOnEach { signal ->
        if (signal.isOnError) {
            val traceId = getTraceId(signal.contextView)
            val premisesId = getPremisesId(signal.contextView)
            val throwable = signal.throwable

            val modifiedAdditionalDetails = additionalDetails.toMutableMap()
            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (throwable is WebClientResponseException) {
                modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = errorResponseBodyFrom(throwable)
            }

            val details = LogDetails(
                errorCode = errorCode,
                message = errorMessage,
                traceId = traceId,
                premisesId = premisesId,
                additionalDetails = modifiedAdditionalDetails.toMap(),
                searchableFields = searchableFields,
                responseTime = getResponseTime(signal.contextView),
            )

            val exception = ThrowableWithTracingDetails(
                throwable = throwable,
                traceId = traceId,
            )

            logger.error(details = details, exception = exception)
        }
    }
}

private fun errorResponseBodyFrom(exception: WebClientResponseException): Any {
    val response = exception.responseBodyAsString
    return try {
        DefaultSerializer.deserialize(response, Map::class.java)
    } catch (e: Throwable) {
        response
    }
}

fun <T> Flux<T>.logOnSuccess(
    logger: Logger,
    message: String,
    additionalDetails: Map<String, Any?> = emptyMap(),
    searchableFields: Map<String, Any?> = emptyMap(),
    skipAdditionalDetails: Boolean = false,
    skipResponseBody: Boolean = true,
): Flux<T> {
    return doOnEach { signal ->
        if (signal.isOnComplete) {
            val modifiedAdditionalDetails = additionalDetails.toMutableMap()

            if (skipAdditionalDetails) {
                modifiedAdditionalDetails.clear()
            }

            if (!skipResponseBody) {
                if (signal.hasValue())
                    modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = getDeserializedResponseBody<T>(signal)
                else
                    modifiedAdditionalDetails[LogConstants.RESPONSE_BODY] = "No response body found"
            }

            val logDetails = LogDetails.create(
                message = message,
                traceId = getTraceId(signal.contextView),
                premisesId = getPremisesId(signal.contextView),
                searchableFields = searchableFields,
                errorCode = null,
                requestDetails = RequestDetails.create(signal.contextView),
                responseDetails = ResponseDetails.create(signal.contextView),
                additionalDetails = modifiedAdditionalDetails

            )
            logger.info(details = logDetails)
        }
    }
}

private fun <T> getDeserializedResponseBody(signal: Signal<T>): Any {
    val data = signal.get()!!
    return if (data is String) {
        try {
            DefaultSerializer.deserialize(data, Map::class.java)
        } catch (e: JsonSyntaxException) {
            data
        }
    } else {
        data
    }
}
