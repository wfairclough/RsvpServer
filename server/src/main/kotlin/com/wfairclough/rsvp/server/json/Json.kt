package com.wfairclough.rsvp.server.json

import com.google.gson.*
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by will on 2017-05-22.
 */

object Serializer {

    val strategy = object : ExclusionStrategy {
        override fun shouldSkipClass(clazz: Class<*>?): Boolean {
            val ann = clazz?.getAnnotation(ExcludeJson::class.java)
            return ann != null
        }

        override fun shouldSkipField(f: FieldAttributes?): Boolean {
            val ann = f?.getAnnotation(ExcludeJson::class.java)
            return (f?.name?.startsWith("_") ?: false) || ann != null
        }

    }

    val gson by lazy {
        GsonBuilder()
                .addSerializationExclusionStrategy(strategy)
                .addDeserializationExclusionStrategy(strategy)
                .registerTypeAdapter(DateTime::class.java, DateTimeConverter())
                .create()

    }

}

