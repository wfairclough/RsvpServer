package com.wfairclough.rsvp.server.controllers

import com.google.gson.Gson
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.http.ContentType
import org.jetbrains.ktor.response.respondText
import java.lang.reflect.Type

/**
 * Created by will on 2017-05-22.
 */
open class BaseCtrl {
    val gson by lazy { Gson() }
}

val ApplicationCall.gson by lazy { Gson() }

val ApplicationCall.defaultContentType by lazy { ContentType.Application.Json }

suspend fun ApplicationCall.respondJson(src: Any) {
    respondText(gson.toJson(src), defaultContentType)
}

suspend fun ApplicationCall.respondJson(src: Any, typeOfStr: Type) {
    respondText(gson.toJson(src, typeOfStr), defaultContentType)
}