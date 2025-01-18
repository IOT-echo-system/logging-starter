package com.robotutor.loggingstarter.serializer

object DefaultSerializer {
    fun serialize(obj: Any?): String {
        return ObjectMapperCache.objectMapper.toJson(obj)
    }

    fun <T> deserialize(str: String, type: Class<T>): T {
        return ObjectMapperCache.objectMapper.fromJson(str, type)
    }
}
