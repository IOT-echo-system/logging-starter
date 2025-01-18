package com.robotutor.loggingstarter.models

import com.robotutor.loggingstarter.getResponseTime
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.server.ServerWebExchange
import reactor.util.context.ContextView

data class ServerWebExchangeDTO(
    val requestDetails: RequestDetails,
    val responseDetails: ResponseDetails
) {
    companion object {
        fun from(exchange: ServerWebExchange): ServerWebExchangeDTO {
            val request = exchange.request
            val response = exchange.response
            return ServerWebExchangeDTO(
                requestDetails = RequestDetails(
                    method = request.method,
                    headers = request.headers,
                    uriWithParams = request.uri.toString(),
                    body = request.body.toString()
                ),
                responseDetails = ResponseDetails(
                    headers = response.headers,
                    statusCode = response.statusCode.toString(),
                    time = -1,
                    body = response.bufferFactory().toString()
                )
            )
        }
    }
}

data class RequestDetails(
    val method: HttpMethod,
    val headers: HttpHeaders,
    val uriWithParams: String? = null,
    val body: String? = null
) {
    companion object {
        fun create(contextView: ContextView): RequestDetails? {
            if (contextView.hasKey(ServerWebExchangeDTO::class.java)) {
                val request = contextView.get(ServerWebExchangeDTO::class.java).requestDetails
                return RequestDetails(
                    method = request.method,
                    headers = request.headers,
                    uriWithParams = request.uriWithParams,
                    body = request.body
                )
            }
            return null
        }
    }
}

data class ResponseDetails(
    val headers: HttpHeaders,
    val statusCode: String = "",
    val time: Long = -1,
    val body: String? = null
) {
    companion object {
        fun create(contextView: ContextView): ResponseDetails? {
            if (contextView.hasKey(ServerWebExchangeDTO::class.java)) {
                val response = contextView.get(ServerWebExchangeDTO::class.java).responseDetails
                return ResponseDetails(
                    headers = response.headers,
                    statusCode = response.statusCode,
                    time = getResponseTime(contextView),
                    body = response.body
                )
            }
            return null
        }
    }
}
