package com.robotutor.loggingstarter

import com.robotutor.loggingstarter.serializer.DefaultSerializer.serialize
import org.slf4j.LoggerFactory

class Logger(className: Class<out Any>) {
    private val logger = LoggerFactory.getLogger(className)

    fun info(details: LogDetails) {
        logger.info(serialize(details))
    }

    fun error(details: LogDetails, exception: Throwable) {
        logger.error(serialize(details), exception)
    }
}
