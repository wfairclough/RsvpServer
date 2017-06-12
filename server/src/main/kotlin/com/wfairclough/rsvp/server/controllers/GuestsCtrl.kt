package com.wfairclough.rsvp.server.controllers

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

/**
 * Created by will on 2017-05-22.
 */
object GuestsCtrl : BaseCtrl() {

    val get = Handler<RoutingContext> { ctx ->
        val key = ctx.pathParam("key") ?: ""
        val guest =  invitationDao.findGuestByKey(key)
        ctx.response().success(guest)
    }

    val list = Handler<RoutingContext> { ctx ->
        val limit = ctx.request().queryParams["limit"]?.toIntOrNull() ?: 100
        val skip = ctx.request().queryParams["skip"]?.toIntOrNull() ?: 0

        val guests = invitationDao.findAllGuests(skip, limit)
        ctx.response().success(guests)
    }
}