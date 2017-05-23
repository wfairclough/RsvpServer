package com.wfairclough.rsvp.server.controllers

import com.wfairclough.rsvp.server.model.DbKey
import com.wfairclough.rsvp.server.model.Guest
import com.wfairclough.rsvp.server.model.Invitation
import com.wfairclough.rsvp.server.utils.Log
import spark.Route

/**
 * Created by will on 2017-05-22.
 */
object InvitationCtrl : BaseCtrl() {

    data class InvitationGuest(val firstname: String, val lastname: String, val email: String?)
    data class CreateInvitationReq(val code: String, val guest: InvitationGuest)

    val create = Route { req, rsp ->
        val reqJson = req.bodyAs(CreateInvitationReq::class.java)

        Log.i("Json: $reqJson")

        val mainGuest = Guest(firstname = reqJson.guest.firstname,
                lastname = reqJson.guest.lastname,
                email = reqJson.guest.email)
        val invitation = Invitation(code = reqJson.code, guests = listOf(mainGuest))

        return@Route invitationDao.create(invitation)
    }

    val get = Route { req, rsp ->
        "test"
    }


}