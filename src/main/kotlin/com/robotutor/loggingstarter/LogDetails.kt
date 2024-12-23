package com.robotutor.loggingstarter

import org.springframework.http.HttpMethod
import org.springframework.web.server.ServerWebExchange
import reactor.util.context.ContextView

data class LogDetails(
    val errorCode: String? = null,
    val message: String,
    val requestMethod: HttpMethod? = null,
    val requestHeaders: Map<String, Any>? = null,
    val requestBody: String? = null,
    val uriWithParams: String? = null,
    val responseHeaders: Map<String, Any>? = null,
    val responseStatusCode: String? = null,
    val responseTime: Long = -1,
    val responseBody: String? = null,
    val traceId: String? = null,
    val additionalDetails: Map<String, Any?> = emptyMap(),
    val searchableFields: Map<String, Any?> = emptyMap(),
) {
    companion object {
        fun create(
            message: String,
            errorCode: String? = null,
            requestDetails: RequestDetails? = null,
            responseDetails: ResponseDetails? = null,
            traceId: String? = null,
            searchableFields: Map<String, Any?> = emptyMap(),
            additionalDetails: Map<String, Any?> = emptyMap(),
        ): LogDetails {
            return LogDetails(
                message = message,
                errorCode = errorCode,
                requestMethod = requestDetails?.method,
                requestHeaders = requestDetails?.headers,
                uriWithParams = requestDetails?.uriWithParams,
                requestBody = requestDetails?.body,
                responseStatusCode = responseDetails?.statusCode,
                responseTime = responseDetails?.time ?: -1,
                responseHeaders = responseDetails?.headers,
                responseBody = responseDetails?.body,
                traceId = traceId,
                searchableFields = searchableFields,
                additionalDetails = additionalDetails
            )
        }
    }
}


class RequestDetails(
    val method: HttpMethod,
    val headers: Map<String, Any>? = null,
    var uriWithParams: String? = null,
    val body: String? = null
) {
    companion object {
        fun create(contextView: ContextView): RequestDetails? {
            if (contextView.hasKey(ServerWebExchange::class.java)) {
                val request = contextView.get(ServerWebExchange::class.java).request
                return RequestDetails(
                    method = request.method,
                    headers = request.headers,
                    uriWithParams = request.uri.toString(),
                    body = request.body.toString()
                )
            }
            return null
        }
    }
}

class ResponseDetails(
    val headers: Map<String, Any>? = null,
    val statusCode: String = "",
    val time: Long = -1,
    val body: String? = null
) {
    companion object {
        fun create(contextView: ContextView): ResponseDetails? {
            if (contextView.hasKey(ServerWebExchange::class.java)) {
                val response = contextView.get(ServerWebExchange::class.java).response
                return ResponseDetails(
                    headers = response.headers,
                    statusCode = response.statusCode.toString(),
                    time = getResponseTime(contextView),
                    body = response.bufferFactory().toString()
                )
            }
            return null
        }
    }
}
