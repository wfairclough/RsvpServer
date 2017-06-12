package com.wfairclough.rsvp.server.controllers

import com.wfairclough.rsvp.server.model.DbKeyUtils
import com.wfairclough.rsvp.server.model.Guest
import com.wfairclough.rsvp.server.model.Invitation
import com.wfairclough.rsvp.server.utils.Log
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

/**
 * Created by will on 2017-05-22.
 */
object InvitationCtrl : BaseCtrl() {

    data class CreateInvitationReq(val code: String, val guest: InvitationGuest, val force: Boolean = false)
    data class InvitationGuest(val firstname: String, val lastname: String, val email: String?) {
        val fullname: String
            get() = "$firstname $lastname"
    }

    val create = Handler<RoutingContext> { ctx ->
        val reqJson: CreateInvitationReq? = ctx.bodyAsOrFail(CreateInvitationReq::class.java) ?: return@Handler

        reqJson?.let {
            Log.d("Create Invite: $reqJson")

            invitationDao.findFirst { it.code == reqJson.code}?.let {
                ctx.fail("Invitation with code '${reqJson.code}' already exists", 400)
                return@Handler
            }

            // We may have a guest with the exact same name. Lets allow this to be forced
            if (!reqJson.force) {
                invitationDao.findByFirstAndLast(reqJson.guest.firstname, reqJson.guest.lastname)?.let {
                    ctx.fail("Invitation for guest '${reqJson.guest.fullname}' already exists", 400)
                    return@Handler
                }
            }

            val inviteKey = DbKeyUtils.generate()
            val mainGuest = Guest(
                    firstname = reqJson.guest.firstname,
                    lastname = reqJson.guest.lastname,
                    email = reqJson.guest.email,
                    invitationKey = inviteKey)
            val invitation = Invitation(key = inviteKey, code = reqJson.code, guests = listOf(mainGuest))

            val invite = invitationDao.create(invitation)

            invite?.let { ctx.response().success(it) } ?: ctx.fail("Could not create invitation", 400)
        } ?: ctx.fail("Could not parse request", 400)

    }

    data class InvitationQuery(val query: String)

    val get = Handler<RoutingContext> { ctx ->
        Log.i("get: ${ctx.normalisedPath()}")
        val code = ctx.pathParam("code") ?: ""
        if (code.isBlank()) {
            ctx.fail("Could not find invitation with blank code", 400)
            return@Handler
        }
        val ret = invitationDao.findFirst { it.code == code}
        ret?.let {
            ctx.response().success(ret)
        } ?: ctx.fail("Could not find invitation with code: $code", 404)
    }

    val query = Handler<RoutingContext> { ctx ->
        val queryJson = ctx.bodyAsOrFail(InvitationQuery::class.java) ?: return@Handler
        val ret = invitationDao.findFirst { it.code == queryJson.query}
        ret?.let {
            ctx.response().success(ret)
        } ?: ctx.fail("Could not find invitation with code: ${queryJson.query}", 404)
    }



}