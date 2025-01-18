package com.robotutor.loggingstarter

import com.robotutor.loggingstarter.models.RequestDetails
import com.robotutor.loggingstarter.models.ResponseDetails
import org.springframework.http.HttpMethod

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
    val traceId: String = "missing-trace-id",
    val premisesId: String = "missing-premises-id",
    val additionalDetails: Map<String, Any?> = emptyMap(),
    val searchableFields: Map<String, Any?> = emptyMap(),
) {
    companion object {
        fun create(
            message: String,
            errorCode: String? = null,
            requestDetails: RequestDetails? = null,
            responseDetails: ResponseDetails? = null,
            traceId: String = "missing-trace-id",
            premisesId: String = "missing-premises-id",
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
                premisesId = premisesId,
                searchableFields = searchableFields,
                additionalDetails = additionalDetails
            )
        }
    }
}
