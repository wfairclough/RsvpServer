package com.wfairclough.rsvp.server.controllers

import com.wfairclough.rsvp.server.model.Guest
import org.jetbrains.ktor.application.ApplicationCall

/**
 * Created by will on 2017-05-22.
 */
object GuestsCtrl : BaseCtrl() {

    suspend fun show(call: ApplicationCall, key: String) {
        println("Hello $key")
//        val test = call.request.headers["X-FORWARDED-FOR"]
        val test = call.request.headers.entries().map { entry -> "${entry.key}: ${entry.value}"}.joinToString(",")
        val guest = Guest(key = key, firstname = "Will", lastname = "Fairclough", email = test)
        call.respondJson(guest)
    }

//    suspend fun addGuest(guest: Guest)

}