package com.wfairclough.rsvp.server

import com.wfairclough.rsvp.server.config.Config
import com.wfairclough.rsvp.server.controllers.GuestsCtrl
import com.wfairclough.rsvp.server.controllers.InvitationCtrl
import com.wfairclough.rsvp.server.controllers.MenuCtrl
import com.wfairclough.rsvp.server.utils.Log
import com.wfairclough.rsvp.server.utils.Options
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.StaticHandler
import io.netty.handler.codec.http.HttpResponseStatus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import org.litote.kmongo.util.KMongoConfiguration


object Rsvp {

    val defaultContentType = "application/json"

    var staticPath = "public"
    var adminStaticPath = "public/admin"
    var port = 3000
    var cacheEnabled = true

    val vertx = Vertx.vertx()
    val server = vertx.createHttpServer()

    val rootRouter = Router.router(vertx)
    val apiRouter = Router.router(vertx)
    val invitationsRouter = Router.router(vertx)

    const val KEY_CONFIG = "config"

    private val config by lazy { Config.load() }

    fun init() {
        initKMongoConfig()
        rootInit()
        apiInit()
        failureInit()
    }

    private fun initKMongoConfig() {
        KMongoConfiguration.bsonMapper.registerModule(JodaModule())
        KMongoConfiguration.extendedJsonMapper.registerModule(JodaModule())
    }

    private fun rootInit() {
        rootRouter.mountSubRouter("/api", apiRouter)

        // Set a static server to serve static resources, e.g. the login page
        rootRouter.route().handler(StaticHandler.create(staticPath).setCachingEnabled(cacheEnabled))
        rootRouter.route("/admin").handler(StaticHandler.create(adminStaticPath).setCachingEnabled(cacheEnabled))
    }

    private fun apiInit() {
        apiRouter.route().handler(BodyHandler.create())
        apiRouter.route("/*").handler { ctx ->
            ctx.addBodyEndHandler {
                Log.i("REQ: [${ctx.request().method().name}] (${ctx.response().statusCode}) - ${ctx.normalisedPath()}")
            }
            ctx.put(KEY_CONFIG, config)
            ctx.next()
        }
        apiRouter.route("/*").consumes(defaultContentType).handler { ctx ->
            ctx.response().putHeader("Content-Type", defaultContentType)
            ctx.next()
        }

        apiRouter.post("/invitations/create").consumes(defaultContentType).handler(InvitationCtrl.create)
        apiRouter.post("/invitations/query").consumes(defaultContentType).handler(InvitationCtrl.query)
        apiRouter.get("/invitations/guests").handler(GuestsCtrl.list)
        apiRouter.get("/invitations/guests/:key").handler(GuestsCtrl.get)
        apiRouter.post("/invitations/:code/guests").consumes(defaultContentType).handler(InvitationCtrl.addGuest)
        apiRouter.post("/invitations/:code/guests/:key/add").consumes(defaultContentType).handler(GuestsCtrl.addPlusOne)
        apiRouter.delete("/invitations/:code/guests/:key").handler(GuestsCtrl.deletePlusOne)
        apiRouter.delete("/invitations/:code/guests/:key/force").handler(GuestsCtrl.deletePlusOne)
        apiRouter.put("/invitations/:code/guests/:key/rsvp").consumes(defaultContentType).handler(GuestsCtrl.rsvp)
        apiRouter.put("/invitations/:code/guests/:key/menu").consumes(defaultContentType).handler(GuestsCtrl.menu)
        apiRouter.get("/invitations/:code/get").handler(InvitationCtrl.get)
        apiRouter.get("/invitations/:code").handler(InvitationCtrl.get)
        apiRouter.post("/menu/createitem").consumes(defaultContentType).handler(MenuCtrl.createItem)
        apiRouter.get("/menu/items/:key").handler(MenuCtrl.get)
        apiRouter.get("/menu/items").handler(MenuCtrl.list)
    }

    private fun failureInit() {
        rootRouter.route("/*").failureHandler { ctx ->
            val statusCode = ctx.statusCode().let { if (it == -1) return@let 500 else return@let it }
            val json = JsonObject().apply {
                put("timestamp", System.nanoTime())
                put("status", statusCode)
                put("error", HttpResponseStatus.valueOf(statusCode).reasonPhrase())
                put("path", ctx.request().path())
                ctx.get<String?>("message")?.let { put("message", it) }
                ctx.failure()?.let {
                    val stacktrace = it.stackTrace.map { it.toString() }.fold(JsonArray().add(it.message)) { jsonArr, line -> jsonArr.add(line) }
                    put("stacktrace", stacktrace)
                    Log.e("Mess $it", it)
                }
            }

            ctx.response().putHeader("Content-Type", "$defaultContentType; charset=utf-8")
            ctx.response().statusCode = statusCode
            ctx.response().end(json.encodePrettily())
        }
    }

    fun start() {
        Log.i("Starting server on port $port")
        server.requestHandler({ rootRouter.accept(it) }).listen(port)
    }
}

fun main(args: Array<String>) {

    val options = Options(args)

    options.getArg("path", "public")?.let { Rsvp.staticPath = it }
    options.getArg("admin-path", "public/admin")?.let { Rsvp.adminStaticPath = it }

    if (options.hasArg("version")) {
        Log.i("Version: 1.0")
    }

    options.getIntArg("port", 3000)?.let { Rsvp.port = it }

    Rsvp.cacheEnabled = !options.hasArg("disable-cache")

    Rsvp.init()

    Rsvp.start()
}