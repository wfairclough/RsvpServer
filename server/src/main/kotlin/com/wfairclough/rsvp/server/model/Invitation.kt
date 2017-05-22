package com.wfairclough.rsvp.server.model

import org.joda.time.DateTime

/**
 * Created by will on 2017-05-22.
 */
data class Invitation(val key: DbKey?,
                      val code: String,
                      val viewed: Boolean,
                      val sent: Boolean,
                      val guests: List<Guest>,
                      val visits: List<VisitRecord>)

data class VisitRecord(val datetime: DateTime,
                       val userAgent: String?)