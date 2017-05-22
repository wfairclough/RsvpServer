package com.wfairclough.rsvp.server.controllers

import com.wfairclough.rsvp.server.model.Address
import com.wfairclough.rsvp.server.model.Country
import com.wfairclough.rsvp.server.model.Guest
import com.wfairclough.rsvp.server.model.MenuItem
import spark.Request
import spark.Response
import spark.Route

//import org.jetbrains.ktor.application.ApplicationCall

/**
 * Created by will on 2017-05-22.
 */
object GuestsCtrl : BaseCtrl() {

    val get = Route { req, rsp ->
        rsp?.type("application/json")
        rsp?.status(200)
        Guest(key = req.params("key"),
                firstname = "Will",
                lastname = "Fairclough",
                email = "wfairclough@gmail.com",
                menuItem = MenuItem(name = "Pork"),
                address = Address("228 Latchford Rd.", null, "Ottawa", "Ontario", Country.Canada, "K1Z 1B9"),
                phone = "613-292-5351")
    }

    val list = Route { req, rsp ->
        "list"
    }
}