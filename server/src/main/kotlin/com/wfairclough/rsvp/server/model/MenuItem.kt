package com.wfairclough.rsvp.server.model

/**
 * Created by will on 2017-05-22.
 */
data class MenuItem(val key: DbKey?,
                    val name: String,
                    val notes: String? = null)