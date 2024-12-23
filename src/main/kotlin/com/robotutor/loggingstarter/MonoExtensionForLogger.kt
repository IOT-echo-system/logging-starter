package com.robotutor.loggingstarter

import com.google.gson.JsonSyntaxException
import com.robotutor.loggingstarter.ReactiveContext.getTraceId
import com.robotutor.loggingstarter.serializer.DefaultSerializer
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal
import reactor.util.context.ContextView
import java.time.LocalDateTime
import java.time.ZoneOffset

fun <T> Mono<T>.logOnError(
    errorCode: String? = null,
    errorMessage: String,
    additionalDetails: Map<String, Any?> = emptyMap(),
    searchableFields: Map<String, Any?> = emptyMap(),
    skipAdditionalDetails: Boolean = false,
): Mono<T> {
    return doOnEach { signal ->
        if (signal.isOnError) {
            val logger = Logger(this::class.java)
            val traceId = getTraceId(signal.contextView)
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

fun <T> Mono<T>.logOnSuccess(
    message: String,
    additionalDetails: Map<String, Any?> = emptyMap(),
    searchableFields: Map<String, Any?> = emptyMap(),
    skipAdditionalDetails: Boolean = false,
    skipResponseBody: Boolean = true,
): Mono<T> {
    return doOnEach { signal ->
        if (signal.isOnNext) {
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

            val logger = Logger(this::class.java)
            val logDetails = LogDetails.create(
                message = message,
                traceId = getTraceId(signal.contextView),
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

fun getResponseTime(context: ContextView): Long {
    return context.getOrEmpty<LocalDateTime>(LogConstants.API_CALL_START_TIME)
        .map { (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - it.toEpochSecond(ZoneOffset.UTC)) * 1000 }
        .orElseGet { -1 }
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

object LogConstants {
    const val LOG_TYPE = "logType"
    const val RESPONSE_BODY = "responseBody"
    const val API_CALL_START_TIME = "startTime"
}

class ThrowableWithTracingDetails(throwable: Throwable?, traceId: String?) : Throwable(
    message = throwable?.message + "{ traceId: $traceId" + "  }",
    cause = throwable
)
