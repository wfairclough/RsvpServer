package com.wfairclough.rsvp.server.controllers

import com.google.gson.*
import com.wfairclough.rsvp.server.dao.InvitationDao
import com.wfairclough.rsvp.server.dao.MenuDao
import com.wfairclough.rsvp.server.json.ExcludeJson
import com.wfairclough.rsvp.server.json.Serializer
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import java.lang.reflect.Type
import kotlin.reflect.KClass

/**
 * Created by will on 2017-05-22.
 */
open class BaseCtrl {
    protected val gson by lazy { Serializer.gson }

    protected val invitationDao by lazy { InvitationDao() }

    protected val menuDao by lazy { MenuDao() }
}


inline fun <reified T : Any> RoutingContext.bodyAs(): T {
    return Serializer.gson.fromJson<T>(bodyAsString, T::class.java)
}

fun RoutingContext.fail(message: String, code: Int) {
    put("message", message)
    fail(code)
}

inline fun <reified T : Any> RoutingContext.bodyAsOrFail(): T? {
    val data = Serializer.gson.fromJson<T>(bodyAsString, T::class.java)
    if (data == null) {
        fail("Could not parse request body", 400)
        return null
    }
    return data
}

fun <T> HttpServerResponse.success(resp: T, pretty: Boolean = false) {
    val gson = when (pretty) {
        true -> Serializer.gsonPrettyPrint
        false -> Serializer.gson
    }
    setStatusCode(200).end(gson.toJson(resp))
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

val HttpServerRequest.prettyPrint: Boolean
    get() = queryParam("pretty")?.toBoolean() ?: false
