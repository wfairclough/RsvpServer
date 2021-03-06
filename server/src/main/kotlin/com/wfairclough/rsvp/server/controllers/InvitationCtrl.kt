package com.wfairclough.rsvp.server.controllers

import com.wfairclough.rsvp.server.model.DbKeyUtils
import com.wfairclough.rsvp.server.model.Guest
import com.wfairclough.rsvp.server.model.Invitation
import com.wfairclough.rsvp.server.model.VisitRecord
import com.wfairclough.rsvp.server.utils.Log
import io.vertx.core.Handler
import io.vertx.core.http.HttpHeaders
import io.vertx.ext.web.RoutingContext
import org.joda.time.DateTime

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
        val reqJson: CreateInvitationReq = ctx.bodyAsOrFail<CreateInvitationReq>() ?: return@Handler

        Log.d("Create Invite: $reqJson")

        val inviteCode = reqJson.code.replace(" ", "").toLowerCase()

        invitationDao.findByCode(inviteCode)?.let {
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

        val invitation = Invitation(key = inviteKey, code = inviteCode, guests = guests)

        val invite = invitationDao.create(invitation)

        invite?.let { ctx.response().success(it) } ?: ctx.fail("Could not create invitation", 400)

    }

    val delete = Handler<RoutingContext> { ctx ->
        val inviteCode = ctx.pathParam("code") ?: ""
        if (inviteCode.isBlank()) {
            ctx.fail("Could not find invitation with blank code", 400)
            return@Handler
        }

        val delRes = invitationDao.removeByCode(inviteCode)

        if (delRes.deletedCount == 1L) {
            ctx.response().success(delRes)
        } else {
            ctx.fail("Did not delete the invitation with code: $inviteCode", 400)
        }

    }

    val addGuest = Handler<RoutingContext> { ctx ->
        val reqJson: InvitationGuest = ctx.bodyAsOrFail<InvitationGuest>() ?: return@Handler
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
                ctx.response().success(this.sortedCopy())
            } ?: ctx.fail("Failed to add new guest to invitation with code: $inviteCode", 500)
        } ?: ctx.fail("Could not find invitation with code: $inviteCode", 404)
    }

    data class InvitationQuery(val query: String?, val code: String?, val key: String?, val selectors: Set<String>?)

    val get = Handler<RoutingContext> { ctx ->

        Log.i("get: ${ctx.normalisedPath()}")
        val code = ctx.pathParam("code") ?: ""
        if (code.isBlank()) {
            ctx.fail("Could not find invitation with blank code", 400)
            return@Handler
        }
        invitationDao.findByCode(code)?.let {
            if (!ctx.normalisedPath().endsWith("get")) {
                val visitRec = VisitRecord(datetime = DateTime.now(),
                        userAgent = ctx.request()?.getHeader(HttpHeaders.USER_AGENT),
                        localAddress = ctx.request()?.localAddress()?.toString(),
                        remoteAddress = ctx.request()?.getHeader("X-REAL-IP") ?: ctx.request()?.getHeader("X_FORWARDED_FOR") ?: ctx.request()?.remoteAddress()?.toString(),
                        httpVersion = ctx.request()?.version()?.toString())
                val visits = it.visits?.let { it + listOf(visitRec) } ?: listOf(visitRec)
                invitationDao.update(it.copy(viewed = true, visits = visits))?.let {
                    Log.d("Set invite ${it.code} to viewed")
                }
            }
            ctx.response().success(it.sortedCopy(), pretty = ctx.request().prettyPrint)
        } ?: ctx.fail("Could not find invitation with code: $code", 404)
    }

    val list = Handler<RoutingContext> { ctx ->
        Log.i("list: ${ctx.normalisedPath()}")
        val invites = invitationDao.find()
        ctx.response().success(invites.sortedWith(compareBy( { !it.viewed }, { it.code } )), pretty = ctx.request().prettyPrint)
    }

    data class RsvpForm(val song: String?, val notes: String?)

    val submit = Handler<RoutingContext> { ctx ->
        val code = ctx.pathParam("code") ?: ""
        if (code.isBlank()) {
            ctx.fail("Could not find invitation with blank code", 400)
            return@Handler
        }
        val rsvpForm = ctx.bodyAsOrFail<RsvpForm>() ?: return@Handler

        invitationDao.findByCode(code)?.let {
            invitationDao.update(it.copy(songRequest = rsvpForm.song, notes = rsvpForm.notes))?.let {
                ctx.response().success(it.sortedCopy())
                return@Handler
            } ?: ctx.fail("Could not update the invitation with code: $code", 500)
        } ?: ctx.fail("Could not find invitation with code: $code", 404)

    }

    val query = Handler<RoutingContext> { ctx ->
        val queryJson = ctx.bodyAsOrFail<InvitationQuery>() ?: return@Handler
        val ret: List<Invitation?> = when {
            queryJson.key != null -> listOf(invitationDao.findFirst { it.key == queryJson.key})
            queryJson.code != null -> listOf(invitationDao.findByCode(queryJson.code))
            queryJson.query != null -> invitationDao.find(queryJson.query)
            else -> {invitationDao.findAll()}
        }

        ret.filterNotNull().let {
            ctx.response().success( it.map { it.sortedCopy().applyProjection(queryJson.selectors) }, pretty = ctx.request().prettyPrint )
        }
    }



}
