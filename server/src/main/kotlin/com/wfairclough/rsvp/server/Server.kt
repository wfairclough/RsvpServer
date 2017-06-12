package com.wfairclough.rsvp.server

import com.wfairclough.rsvp.server.controllers.GuestsCtrl
import com.wfairclough.rsvp.server.controllers.InvitationCtrl
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler

object Rsvp {

    val defaultContentType = "application/json"

    val vertx = Vertx.vertx()
    val server = vertx.createHttpServer()

    val rootRouter = Router.router(vertx)
    val apiRouter = Router.router(vertx)
    val invitationsRouter = Router.router(vertx)

    fun init() {
        rootInit()
        apiInit()
        failureInit()
    }

    private fun rootInit() {
        rootRouter.mountSubRouter("/api", apiRouter)
        rootRouter.get("/:code").consumes(defaultContentType).handler {

            it.reroute("/api/invitations/${it.pathParam("code")}")
        }

        // Set a static server to serve static resources, e.g. the login page
        rootRouter.route().handler(StaticHandler.create("public"))
    }

    private fun apiInit() {
        apiRouter.route().handler(BodyHandler.create())
        apiRouter.route("/*").consumes(defaultContentType).handler { ctx ->
            ctx.response().putHeader("Content-Type", defaultContentType)
            ctx.next()
        }

        apiRouter.post("/invitations/create").consumes(defaultContentType).handler(InvitationCtrl.create)
        apiRouter.post("/invitations/query").consumes(defaultContentType).handler(InvitationCtrl.query)
        apiRouter.get("/invitations/:code").consumes(defaultContentType).handler(InvitationCtrl.get)
        apiRouter.get("/invitations/guests").consumes(defaultContentType).handler(GuestsCtrl.list)
    }

    private fun failureInit() {
//        rootRouter.route("/*").failureHandler { ctx ->
//
//            ctx.response().setStatusCode(500).end("Sorry Bud!")
//        }
    }

    fun start() {
        server.requestHandler({ rootRouter.accept(it) }).listen(8080)
    }
}

fun main(args: Array<String>) {

    Rsvp.server

    Rsvp.init()

    Rsvp.start()

}