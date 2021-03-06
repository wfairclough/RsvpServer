package com.wfairclough.rsvp.server.controllers

import com.wfairclough.rsvp.server.model.Guest
import com.wfairclough.rsvp.server.model.GuestMenuItem
import com.wfairclough.rsvp.server.model.Invitation
import com.wfairclough.rsvp.server.model.ListResult
import com.wfairclough.rsvp.server.utils.Log
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import org.joda.time.DateTime

/**
 * Created by will on 2017-05-22.
 */
object GuestsCtrl : BaseCtrl() {

    val get = Handler<RoutingContext> { ctx ->
        val key = ctx.pathParam("key") ?: ""
        val guest =  invitationDao.findGuestByKey(key)
        ctx.response().success(guest, pretty = ctx.request().prettyPrint)
    }

    val list = Handler<RoutingContext> { ctx ->
        val limit = ctx.request().queryParams["limit"]?.toIntOrNull() ?: 100
        val skip = ctx.request().queryParams["skip"]?.toIntOrNull() ?: 0
        val rsvp: Boolean? = ctx.request().queryParams["rsvp"]?.toBoolean()

        val guests = invitationDao.findAllGuests(skip, limit, rsvp)
        ctx.response().success(ListResult(guests.sortedWith(Guest.guestUpdatedCompareBy), guests.size), pretty = ctx.request().prettyPrint)
    }

    data class UpdateGuestReq(val firstname: String?, val lastname: String?, val hasPlusOne: Boolean?, val email: String?)

    val updateGuest = Handler<RoutingContext> { ctx ->
        val guestKey = ctx.pathParam("key") ?: ""
        if (guestKey.isBlank()) {
            ctx.fail("Must include a non-blank guest key", 400)
            return@Handler
        }
        val inviteCode = ctx.pathParam("code") ?: ""
        if (inviteCode.isBlank()) {
            ctx.fail("Must include a non-blank invite code", 400)
            return@Handler
        }
        val updateGuestReq = ctx.bodyAsOrFail<UpdateGuestReq>()
        if (updateGuestReq == null) {
            ctx.fail("Could not parse Update Guest Request", 400)
            return@Handler
        }

        invitationDao.findByCode(inviteCode)?.let { invitation ->
            val (guestToUpdateList, restGuest) = invitation.guests.partition { it.key == guestKey }
            val updatedGuests = guestToUpdateList.map {
                var guest = it
                guest = updateGuestReq.firstname?.let { guest.copy(firstname = it) } ?: guest
                guest = updateGuestReq.lastname?.let { guest.copy(lastname = it) } ?: guest
                guest = updateGuestReq.email?.let { guest.copy(email = it) } ?: guest
                guest = updateGuestReq.hasPlusOne?.let { guest.copy(plusOne = it) } ?: guest
                if (!guest.plusOne && (guest.hasAddedPlusOne ?: false)) {
                    ctx.fail("Cannot remove plus one from guest when guest is already added", 400)
                    return@Handler
                }
                return@map guest
            }
            val updatedInvite = invitation.copy(guests = updatedGuests + restGuest)
            invitationDao.update(updatedInvite)?.apply {
                ctx.response().success(this.sortedCopy())
            } ?: ctx.fail("Could not update invitation with updated guest", 500)
        } ?: ctx.fail("No invitation found for guest key $guestKey. YIKES!", 500)
    }

    data class InvitationPlusOneGuest(val firstname: String, val lastname: String, val email: String?) {
        val fullname: String = "$firstname $lastname"
    }

    val addPlusOne = Handler<RoutingContext> { ctx ->
        val guestKey = ctx.pathParam("key") ?: ""
        if (guestKey.isBlank()) {
            ctx.fail("Must include a non-blank guest key", 400)
            return@Handler
        }
        val plusOneGuest = ctx.bodyAsOrFail<InvitationPlusOneGuest>()
        if (plusOneGuest == null) {
            ctx.fail("Could not parse Plus One Guest", 400)
            return@Handler
        }
        invitationDao.findGuestByKey(guestKey)?.let { guest ->
            if (!guest.plusOne) {
                ctx.fail("The guest with key '$guestKey' does not have a plus one", 400)
                return@Handler
            }
            if (guest.hasAddedPlusOne == true) {
                ctx.fail("The guest with key '$guestKey' has already added their plus one", 400)
                return@Handler
            }
            invitationDao.findByKey(guest.invitationKey)?.let { invitation ->
                val oldGuests = invitation.guests.map { g ->
                    if (g.key == guestKey) {
                        return@map g.copy(hasAddedPlusOne = true)
                    }
                    return@map g
                }
                val newGuest = Guest(
                        firstname = plusOneGuest.firstname,
                        lastname = plusOneGuest.lastname,
                        email = plusOneGuest.email,
                        rsvp = true, // Assume they are coming since they are a guest of a guest
                        invitationKey = invitation.key,
                        plusOneGuestKey = guest.key,
                        updated = DateTime.now()
                )
                val invite = invitation.copy(guests = oldGuests + listOf(newGuest))
                invitationDao.update(invite)?.apply {
                    ctx.response().success(this.sortedCopy())
                } ?: ctx.fail("Could not update invitation with new guest", 500)
            } ?: ctx.fail("Cannot find invitation with key: ${guest.invitationKey}", 404)
        } ?: ctx.fail("Cannot find guest with key: $guestKey", 404)
    }

    data class PlusOneRequest(val hasPlusOne: Boolean = false)

    val noPlusOne  = Handler<RoutingContext> { ctx ->
        val guestKey = ctx.pathParam("key") ?: ""
        if (guestKey.isBlank()) {
            ctx.fail("Must include a non-blank guest key", 400)
            return@Handler
        }
        val inviteCode = ctx.pathParam("code") ?: ""
        if (inviteCode.isBlank()) {
            ctx.fail("Must include valid invite code", 400)
            return@Handler
        }
        val rsvpJson = ctx.bodyAsOrFail<PlusOneRequest>()
        if (rsvpJson == null) {
            ctx.fail("Could not parse Plus One Request", 400)
            return@Handler
        }
        val invite = invitationDao.findByCode(inviteCode)
        invite?.let {
            val parts = invite.guests.partition { it.key == guestKey }
            val updatedGuest = parts.first.firstOrNull()?.copy(hasAddedPlusOne = rsvpJson.hasPlusOne, updated = DateTime.now())
            if (updatedGuest == null) {
                ctx.fail("The guest with key $guestKey does not belong to the invitation with code $inviteCode", 400)
                return@Handler
            }
            val updatesGuests = listOf(updatedGuest) + parts.second
            invitationDao.update(invite.copy(guests = updatesGuests))?.apply {
                ctx.response().success(this.sortedCopy())
            } ?: ctx.fail("Could not update invitation with updated guest", 500)
        } ?: ctx.fail("Could not find invite with code: $inviteCode", 404)
    }

    data class RsvpRequest(val rsvp: Boolean = true)

    val rsvp = Handler<RoutingContext> { ctx ->
        val guestKey = ctx.pathParam("key") ?: ""
        if (guestKey.isBlank()) {
            ctx.fail("Must include a non-blank guest key", 400)
            return@Handler
        }
        val inviteCode = ctx.pathParam("code") ?: ""
        if (inviteCode.isBlank()) {
            ctx.fail("Must include valid invite code", 400)
            return@Handler
        }
        val rsvpJson = ctx.bodyAsOrFail<RsvpRequest>()
        if (rsvpJson == null) {
            ctx.fail("Could not parse RSVP Request", 400)
            return@Handler
        }

        val invite = invitationDao.findByCode(inviteCode)
        invite?.let {
            val parts = invite.guests.partition { it.key == guestKey }
            val guest = parts.first.firstOrNull()
            if (guest == null) {
                ctx.fail("The guest with key $guestKey does not belong to the invitation with code $inviteCode", 400)
                return@Handler
            }
            val updatedGuest = when (rsvpJson.rsvp) {
                true -> guest.copy(rsvp = rsvpJson.rsvp, updated = DateTime.now())
                false -> guest.copy(rsvp = rsvpJson.rsvp, hasAddedPlusOne = null, updated = DateTime.now())
            }
            val updatedGuests = listOf(updatedGuest) + parts.second
            // We must delete the plus one if we are unrsvping
            val newInvite = when (guest.hasAddedPlusOne) {
                true -> invite.copy(guests = updatedGuests.filterNot { it.plusOneGuestKey == guestKey })
                else -> invite.copy(guests = updatedGuests)
            }
            invitationDao.update(newInvite)?.apply {
                ctx.response().success(this.sortedCopy())
            } ?: ctx.fail("Could not update invitation with updated guest", 500)
        } ?: ctx.fail("Could not find invite with code: $inviteCode", 404)

    }

    val menu = Handler<RoutingContext> { ctx ->
        val guestKey = ctx.pathParam("key") ?: ""
        if (guestKey.isBlank()) {
            ctx.fail("Must include a non-blank guest key", 400)
            return@Handler
        }
        val inviteCode = ctx.pathParam("code") ?: ""
        if (inviteCode.isBlank()) {
            ctx.fail("Must include valid invite code", 400)
            return@Handler
        }
        val menuJson = ctx.bodyAsOrFail<GuestMenuItem>()
        if (menuJson == null) {
            ctx.fail("Could not parse Menu Item Request", 400)
            return@Handler
        }

        val menuItem = menuDao.findByKey(menuJson.menuItemKey)
        if (menuItem == null) {
            ctx.fail("Could not find menu item with key: ${menuJson.menuItemKey}", 404)
            return@Handler
        }

        val invite = invitationDao.findByCode(inviteCode)
        invite?.let {
            val parts = invite.guests.partition { it.key == guestKey }
            val updatedGuest = parts.first.firstOrNull()?.copy(menuItem = menuJson, updated = DateTime.now())
            if (updatedGuest == null) {
                ctx.fail("The guest with key $guestKey does not belong to the invitation with code $inviteCode", 400)
                return@Handler
            }
            val updatesGuests = listOf(updatedGuest) + parts.second
            invitationDao.update(invite.copy(guests = updatesGuests))?.apply {
                ctx.response().success(this.sortedCopy())
            }
        } ?: ctx.fail("Could not find invite with code: $inviteCode", 404)

    }

    val deletePlusOne = Handler<RoutingContext> { ctx ->
        val force = ctx.normalisedPath().endsWith("force")
        val guestKey = ctx.pathParam("key") ?: ""
        if (guestKey.isBlank()) {
            ctx.fail("Must include a non-blank guest key", 400)
            return@Handler
        }
        val inviteCode = ctx.pathParam("code") ?: ""
        if (inviteCode.isBlank()) {
            ctx.fail("Must include valid invite code", 400)
            return@Handler
        }
        removePlusOneWith(inviteCode, guestKey, ctx, force)
    }

    private fun removePlusOneWith(inviteCode: String, guestKey: String, ctx: RoutingContext? = null, force: Boolean = false): Invitation? {
        Log.d("removePlusOneWith $inviteCode  $guestKey ")
        val invite = invitationDao.findByCode(inviteCode)
        invite?.let {
            val parts = invite.guests.partition { it.key == guestKey }
            val updatedGuest = parts.first.firstOrNull()
            if (updatedGuest == null) {
                ctx?.fail("The guest with key $guestKey does not belong to the invitation with code $inviteCode", 400)
                return null
            }
            if (!force && updatedGuest.plusOneGuestKey == null) {
                ctx?.fail("Cannot delete this guest. You may only delete guests that where added as a plus one", 400)
                return null
            }
            val parts2 = parts.second.partition { it.key == updatedGuest.plusOneGuestKey }
            val ownerGuest = parts2.first.firstOrNull()?.let { listOf(it.copy(hasAddedPlusOne = false)) } ?: listOf()
            invitationDao.update(it.copy(guests = parts2.second + ownerGuest))?.apply {
                val ret = this.sortedCopy()
                ctx?.response()?.success(ret)
                return ret
            } ?: ctx?.fail("Could not update invite ($inviteCode) with updated guest list", 500)
        } ?: ctx?.fail("Could not find invite with code: $inviteCode", 404)
        return null
    }
}