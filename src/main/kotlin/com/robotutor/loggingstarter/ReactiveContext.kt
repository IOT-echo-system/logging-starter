package com.robotutor.loggingstarter

import com.robotutor.loggingstarter.models.ServerWebExchangeDTO
import org.springframework.http.HttpHeaders
import reactor.util.context.ContextView

internal object ReactiveContext {

    private const val TRACE_ID_HEADER_KEY = "x-trace-id"
    private const val PREMISES_ID_HEADER_KEY = "x-premises-id"

    fun getTraceId(context: ContextView): String = getValueFromRequestHeader(context, TRACE_ID_HEADER_KEY)
        ?: "missing-trace-id"

    fun getPremisesId(context: ContextView): String = getValueFromRequestHeader(context, PREMISES_ID_HEADER_KEY)
        ?: "missing-premises-id"


    private fun getValueFromRequestHeader(
        context: ContextView,
        headerKey: String
    ): String? {
        return when {
            context.isEmpty -> "EMPTY_CONTEXT"
            context.hasKey(ServerWebExchangeDTO::class.java) -> {
                val valueFromRequest = context.get(ServerWebExchangeDTO::class.java)
                    .requestDetails.headers[headerKey]?.first()
                if (valueFromRequest == null && context.hasKey("headers")) {
                    return context.get<HttpHeaders>("headers")[headerKey]?.firstOrNull()
                } else {
                    valueFromRequest
                }
            }

            context.hasKey("headers") -> context.get<HttpHeaders>("headers")[headerKey]?.firstOrNull()
            else -> null
        }
    }
}
