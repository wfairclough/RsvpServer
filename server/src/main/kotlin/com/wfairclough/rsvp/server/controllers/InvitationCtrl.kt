package com.wfairclough.rsvp.server.controllers

import com.google.gson.Gson
import com.sun.xml.internal.ws.client.RequestContext
import com.wfairclough.rsvp.server.model.Guest
import com.wfairclough.rsvp.server.model.Invitation
import com.wfairclough.rsvp.server.utils.Log
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

/**
 * Created by will on 2017-05-22.
 */
object InvitationCtrl : BaseCtrl() {

    data class InvitationGuest(val firstname: String, val lastname: String, val email: String?)
    data class CreateInvitationReq(val code: String, val guest: InvitationGuest)

    val create = Handler<RoutingContext> { ctx ->
        val reqJson: CreateInvitationReq? = ctx.bodyAs(CreateInvitationReq::class.java)

        reqJson?.let {
            Log.i("Json: $reqJson")

            val mainGuest = Guest(firstname = reqJson.guest.firstname,
                    lastname = reqJson.guest.lastname,
                    email = reqJson.guest.email)
            val invitation = Invitation(code = reqJson.code, guests = listOf(mainGuest))

            val invite = invitationDao.create(invitation)

            invite?.let { ctx.response().end(it) } ?: ctx.fail(403)
        } ?: ctx.fail(400)

    }

    data class InvitationQuery(val query: String)

    val get = Handler<RoutingContext> { req ->

    }

    val query = Handler<RoutingContext> { ctx ->
        val queryJson = ctx.bodyAs(InvitationQuery::class.java)
        val ret = invitationDao.findFirst { it.code == queryJson.query}
        ctx.response().setStatusCode(200).end(ret)
    }

//    val get = Route { req, rsp ->
//        val queryJson = req.bodyAs(InvitationQuery::class.java)
//
//        return@Route invitationDao.findFirst { it.code == queryJson.query}
//    }
//
//    val query = Route { req, rsp ->
//        val queryJson = req.bodyAs(InvitationQuery::class.java)
//
//        return@Route invitationDao.findFirst { it.code == queryJson.query}
//    }


}