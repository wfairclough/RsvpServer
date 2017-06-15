package com.wfairclough.rsvp.server.controllers

import com.wfairclough.rsvp.server.model.Guest
import com.wfairclough.rsvp.server.model.GuestMenuItem
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
        val rsvp: Boolean? = ctx.request().queryParams["rsvp"]?.toBoolean()

        val guests = invitationDao.findAllGuests(skip, limit, rsvp)
        ctx.response().success(guests.sortedWith(Guest.guestCompareBy))
    }

    data class InvitationPlusOneGuest(val firstname: String, val lastname: String, val email: String?) {
        val fullname: String
            get() = "$firstname $lastname"
    }

    val addPlusOne = Handler<RoutingContext> { ctx ->
        val guestKey = ctx.pathParam("key") ?: ""
        if (guestKey.isBlank()) {
            ctx.fail("Must include a non-blank guest key", 400)
            return@Handler
        }
        val plusOneGuest = ctx.bodyAsOrFail(InvitationPlusOneGuest::class.java)
        if (plusOneGuest == null) {
            ctx.fail("Could not parse Plus One Guest", 400)
            return@Handler
        }
        invitationDao.findGuestByKey(guestKey)?.let { guest ->
            if (!guest.plusOne) {
                ctx.fail("The guest with key '$guestKey' does not have a plus one", 400)
                return@Handler
            }
            if (guest.hasAddedPlusOne) {
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
                        plusOneGuestKey = guest.key
                )
                val invite = invitation.copy(guests = oldGuests + listOf(newGuest))
                invitationDao.update(invite)?.apply {
                    ctx.response().success(this.sortedCopy())
                } ?: ctx.fail("Could not update invitation with new guest", 500)
            } ?: ctx.fail("Cannot find invitation with key: ${guest.invitationKey}", 404)
        } ?: ctx.fail("Cannot find guest with key: $guestKey", 404)
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
        val rsvpJson = ctx.bodyAsOrFail(RsvpRequest::class.java)
        if (rsvpJson == null) {
            ctx.fail("Could not parse RSVP Request", 400)
            return@Handler
        }

        val invite = invitationDao.findByCode(inviteCode)
        invite?.let {
            val parts = invite.guests.partition { it.key == guestKey }
            val updatedGuest = parts.first.firstOrNull()?.copy(rsvp = rsvpJson.rsvp)
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
        val menuJson = ctx.bodyAsOrFail(GuestMenuItem::class.java)
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
            val updatedGuest = parts.first.firstOrNull()?.copy(menuItem = menuJson)
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
        val invite = invitationDao.findByCode(inviteCode)
        invite?.let {
            val parts = invite.guests.partition { it.key == guestKey }
            val updatedGuest = parts.first.firstOrNull()
            if (updatedGuest == null) {
                ctx.fail("The guest with key $guestKey does not belong to the invitation with code $inviteCode", 400)
                return@Handler
            }
            if (!force && updatedGuest.plusOneGuestKey == null) {
                ctx.fail("Cannot delete this guest. You may only delete guests that where added as a plus one", 400)
                return@Handler
            }
            val parts2 = parts.second.partition { it.key == updatedGuest.plusOneGuestKey }
            val ownerGuest = parts2.first.firstOrNull()?.let { listOf(it.copy(hasAddedPlusOne = false)) } ?: listOf()
            invitationDao.update(it.copy(guests = parts2.second + ownerGuest))?.apply {
                ctx.response().success(this.sortedCopy())
            }
        } ?: ctx.fail("Could not find invite with code: $inviteCode", 404)
    }
}