package com.wfairclough.rsvp.server.controllers

import com.google.gson.Gson
import com.wfairclough.rsvp.server.dao.InvitationDao
import com.wfairclough.rsvp.server.json.Serializer
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import java.lang.reflect.Type

/**
 * Created by will on 2017-05-22.
 */
open class BaseCtrl {
    protected val gson by lazy { Gson() }

    protected val invitationDao by lazy { InvitationDao() }
}

object Global {
    val gson by lazy { Gson() }
}

fun <T> RoutingContext.bodyAs(clazz: Class<T>): T {
    return Global.gson.fromJson<T>(bodyAsString, clazz)
}

fun <T> RoutingContext.bodyAsOrFail(clazz: Class<T>): T? {
    val data = Global.gson.fromJson<T>(bodyAsString, clazz)
    if (data == null) {
        fail(400)
        return null
    }
    return data
}

fun <T> HttpServerResponse.success(resp: T) {
    setStatusCode(200).end(Global.gson.toJson(resp))
}

//val Response.gson by lazy { Gson() }
//
//val Response.defaultContentType by lazy { "application/json" }
//
//fun Response.bodyAsJson(body: Any): Response {
//    body(gson.toJson(body))
//    type(defaultContentType)
//    return this
//}
//
//fun Response.bodyAsJson(body: Any, typeOfStr: Type): Response {
//    body(gson.toJson(body, typeOfStr))
//    type(defaultContentType)
//    return this
//}
//
//val Request.gson by lazy { Gson() }
//
//fun <T> Request.bodyAs(clazz: Class<T>): T {
//    return gson.fromJson(body(), clazz)
//}
//
//class JsonTransformer : ResponseTransformer {
//
//    protected val gson by lazy { Serializer.gson }
//
//    override fun render(model: Any?): String = gson.toJson(model)
//
//}