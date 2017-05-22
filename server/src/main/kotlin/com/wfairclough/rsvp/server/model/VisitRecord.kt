package com.wfairclough.rsvp.server.model

import org.joda.time.DateTime

/**
 * Created by will on 2017-05-22.
 */
data class VisitRecord(val datetime: DateTime,
                       val userAgent: String?)