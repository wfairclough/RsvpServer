package com.wfairclough.rsvp.server.controllers

import com.wfairclough.rsvp.server.dao.InvitationDao
import spark.Request
import spark.Response
import spark.Route

//import org.jetbrains.ktor.application.ApplicationCall
//import org.jetbrains.ktor.request.location
//import org.jetbrains.ktor.sessions.session
//import org.jetbrains.ktor.transform.transform

/**
 * Created by will on 2017-05-22.
 */
object InvitationCtrl : BaseCtrl() {

    val create = Route { req, rsp ->
        "test"
    }

    val get = Route { req, rsp ->
        "test"
    }


}