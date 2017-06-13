package com.wfairclough.rsvp.server.controllers

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.wfairclough.rsvp.server.dao.InvitationDao
import com.wfairclough.rsvp.server.dao.MenuDao
import com.wfairclough.rsvp.server.json.ExcludeJson
import com.wfairclough.rsvp.server.json.Serializer
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.lang.reflect.Type

/**
 * Created by will on 2017-05-22.
 */
open class BaseCtrl {
    protected val gson by lazy { Global.gson }

    protected val invitationDao by lazy { InvitationDao() }

    protected val menuDao by lazy { MenuDao() }
}

object Global {
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
                .create()

    }
}

fun <T> RoutingContext.bodyAs(clazz: Class<T>): T {
    return Global.gson.fromJson<T>(bodyAsString, clazz)
}

fun RoutingContext.fail(message: String, code: Int) {
    put("message", message)
    fail(code)
}

fun <T> RoutingContext.bodyAsOrFail(clazz: Class<T>): T? {
    val data = Global.gson.fromJson<T>(bodyAsString, clazz)
    if (data == null) {
        fail("Could not parse request body", 400)
        return null
    }
    return data
}

fun <T> HttpServerResponse.success(resp: T) {
    setStatusCode(200).end(Global.gson.toJson(resp))
}

val HttpServerRequest.queryParams: Map<String, String>
    get() {
        return query()?.split("&")?.fold(mutableMapOf<String, String>()) { acc, part ->
            val pairParts = part.split("=", limit = 2)
            if (pairParts.size == 2) {
                acc.put(pairParts[0], pairParts[1])
            }
            return@fold acc
        } ?: mapOf()
    }

fun HttpServerRequest.queryParam(key: String): String? {
    return queryParams[key]
}
