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

    data class CreateInvitationReq(val code: String, val guests: List<InvitationGuest>, val force: Boolean = false)
    data class InvitationGuest(val firstname: String, val lastname: String, val email: String?, val plusOne: Boolean = false) {
        val fullname: String
            get() = "$firstname $lastname"
    }

    val create = Handler<RoutingContext> { ctx ->
        val reqJson: CreateInvitationReq = ctx.bodyAsOrFail(CreateInvitationReq::class.java) ?: return@Handler

        Log.d("Create Invite: $reqJson")

        invitationDao.findFirst { it.code == reqJson.code}?.let {
            ctx.fail("Invitation with code '${reqJson.code}' already exists", 400)
            return@Handler
        }

        if (reqJson.guests.isEmpty()) {
            ctx.fail("Invitation must have at least one guest", 400)
            return@Handler
        }

        val inviteKey = DbKeyUtils.generate()
        val guests = reqJson.guests.map { guest ->
            // We may have a guest with the exact same name. Lets allow this to be forced
            if (!reqJson.force) {
                invitationDao.findByFirstAndLast(guest.firstname, guest.lastname)?.let {
                    ctx.fail("Invitation for guest '${guest.fullname}' already exists", 400)
                    return@Handler
                }
            }

            return@map Guest(
                    firstname = guest.firstname,
                    lastname = guest.lastname,
                    email = guest.email,
                    plusOne = guest.plusOne,
                    invitationKey = inviteKey)
        }

        val invitation = Invitation(key = inviteKey, code = reqJson.code, guests = guests)

        val invite = invitationDao.create(invitation)

        invite?.let { ctx.response().success(it) } ?: ctx.fail("Could not create invitation", 400)

    }

    val addGuest = Handler<RoutingContext> { ctx ->
        val reqJson: InvitationGuest = ctx.bodyAsOrFail(InvitationGuest::class.java) ?: return@Handler
        val inviteCode = ctx.pathParam("code") ?: ""
        if (inviteCode.isBlank()) {
            ctx.fail("Could not find invitation with blank code", 400)
            return@Handler
        }

        invitationDao.findByFirstAndLast(reqJson.firstname, reqJson.lastname)?.let {
            ctx.fail("Invitation for guest '${reqJson.fullname}' already exists", 400)
            return@Handler
        }

        invitationDao.findByCode(inviteCode)?.apply {
            val guest = Guest(
                    firstname = reqJson.firstname,
                    lastname = reqJson.lastname,
                    email = reqJson.email,
                    plusOne = reqJson.plusOne,
                    invitationKey = this.key)
            val updated = this.copy(guests = this.guests + listOf(guest))
            invitationDao.update(updated)?.apply {
                ctx.response().success(this)
            } ?: ctx.fail("Failed to add new guest to invitation with code: $inviteCode", 500)
        } ?: ctx.fail("Could not find invitation with code: $inviteCode", 404)
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