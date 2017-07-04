package com.wfairclough.rsvp.server.controllers

import com.wfairclough.rsvp.server.model.MenuItem
import com.wfairclough.rsvp.server.utils.Log
import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext

/**
 * Created by will on 2017-06-12.
 */
object MenuCtrl : BaseCtrl() {

    data class CreateMenuItemReq(val name: String,
                                 val vegetarian: Boolean = false,
                                 val vegan: Boolean = false,
                                 val pescatarian: Boolean = false,
                                 val description: String? = null)

    val createItem = Handler<RoutingContext> { ctx ->
        val reqJson: CreateMenuItemReq = ctx.bodyAsOrFail(CreateMenuItemReq::class.java) ?: return@Handler

        Log.d("Create Menu Item: $reqJson")

        menuDao.findFirst { it.name == reqJson.name}?.let {
            ctx.fail("Menu Item with name '${reqJson.name}' already exists", 400)
            return@Handler
        }

        val menuItem = MenuItem(name = reqJson.name,
                vegetarian = reqJson.vegetarian,
                vegan = reqJson.vegan,
                pescatarian = reqJson.pescatarian,
                description = reqJson.description)

        val createdItem = menuDao.createItem(menuItem)

        createdItem?.let { ctx.response().success(it) } ?: ctx.fail("Could not create invitation", 400)

    }

    val get = Handler<RoutingContext> { ctx ->
        val key = ctx.pathParam("key") ?: ""
        if (key.isBlank()) {
            ctx.fail("Must include a non-blank menu item key", 400)
        }
        menuDao.findByKey(key)?.apply {
            ctx.response().success(this, pretty = ctx.request().prettyPrint)
        } ?: ctx.fail("Could not find menu item for key $key", 404)
    }


    val list = Handler<RoutingContext> { ctx ->
        val skip = ctx.request().queryParams["skip"]?.toIntOrNull() ?: 0
        val limit = ctx.request().queryParams["limit"]?.toIntOrNull() ?: 100

        val menuItems = menuDao.findAll(skip, limit)
        ctx.response().success(menuItems, pretty = ctx.request().prettyPrint)
    }

}