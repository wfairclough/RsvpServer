package com.wfairclough.rsvp.server.json

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder

/**
 * Created by will on 2017-05-22.
 */
annotation class Exclude

object Serializer {

    val gson by lazy {
        GsonBuilder().addSerializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                return false
            }

            override fun shouldSkipField(f: FieldAttributes?): Boolean {
                return f?.getAnnotation(Exclude::class.java) != null ||
                        f?.name?.take(1) == "_"
            }
        }).create()
    }

}