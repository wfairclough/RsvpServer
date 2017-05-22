package com.wfairclough.rsvp.server.model

/**
 * Created by will on 2017-05-22.
 */
data class Invitation(val key: DbKey?,
                      val code: String,
                      val viewed: Boolean,
                      val sent: Boolean,
                      val guests: List<Guest>,
                      val vists: List<VisitRecord>)