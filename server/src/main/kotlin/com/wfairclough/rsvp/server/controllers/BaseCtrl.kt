package com.wfairclough.rsvp.server.controllers

import com.google.gson.Gson
import com.wfairclough.rsvp.server.dao.InvitationDao
import spark.Response
import spark.ResponseTransformer
//import org.jetbrains.ktor.application.ApplicationCall
//import org.jetbrains.ktor.http.ContentType
//import org.jetbrains.ktor.response.respondText
import java.lang.reflect.Type

/**
 * Created by will on 2017-05-22.
 */
open class BaseCtrl {
    protected val gson by lazy { Gson() }

    protected val invitationDao by lazy { InvitationDao() }
}

val Response.gson by lazy { Gson() }

val Response.defaultContentType by lazy { "application/json" }

fun Response.bodyAsJson(body: Any): Response {
    body(gson.toJson(body))
    type(defaultContentType)
    return this
}

fun Response.bodyAsJson(body: Any, typeOfStr: Type): Response {
    body(gson.toJson(body, typeOfStr))
    type(defaultContentType)
    return this
}

//val ApplicationCall.gson by lazy { Gson() }
//
//val ApplicationCall.defaultContentType by lazy { ContentType.Application.Json }
//
//suspend fun ApplicationCall.respondJson(src: Any) {
//    respondText(gson.toJson(src), defaultContentType)
//}
//
//suspend fun ApplicationCall.respondJson(src: Any, typeOfStr: Type) {
//    respondText(gson.toJson(src, typeOfStr), defaultContentType)
//}

class JsonTransformer : ResponseTransformer {

    protected val gson by lazy { Gson() }

    override fun render(model: Any?): String {

        return gson.toJson(model)
    }

}