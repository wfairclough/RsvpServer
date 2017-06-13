package com.wfairclough.rsvp.server.model

/**
 * Created by will on 2017-06-12.
 */
data class MenuItem(val _id: MongoID? = null,
                    override var key: DbKey = DbKeyUtils.generate(),
                    val name: String,
                    val description: String? = null,
                    val smImageLink: String? = null,
                    val lgImageLink: String? = null,
                    val vegetarian: Boolean = false,
                    val vegan: Boolean = false,
                    val pescatarian: Boolean = false) : Keyable