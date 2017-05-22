package com.wfairclough.rsvp.server

import com.wfairclough.rsvp.server.controllers.GuestsCtrl
import org.jetbrains.ktor.application.ApplicationCall
import org.jetbrains.ktor.host.embeddedServer
import org.jetbrains.ktor.netty.Netty
import org.jetbrains.ktor.routing.get
import org.jetbrains.ktor.routing.routing


fun main(args: Array<String>) {
    val server = embeddedServer(Netty, 8080) {
        routing {
            get("/{key}") { call: ApplicationCall ->
                val key = call.parameters["key"]
                key?.let {
                    GuestsCtrl.show(call, key)
                }
            }
        }
    }
    server.start(wait = true)
}