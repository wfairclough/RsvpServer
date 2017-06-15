package com.wfairclough.rsvp.server.model

import org.joda.time.DateTime

/**
 * Created by will on 2017-05-22.
 */
data class Invitation(override val _id: MongoID? = null,
                      override var key: DbKey = DbKeyUtils.generate(),
                      val code: String,
                      val viewed: Boolean = false,
                      val sent: Boolean = false,
                      val songRequest: String? = null,
                      val guests: List<Guest>,
                      val visits: List<VisitRecord>? = null) : MongoDocumentKeyable {
    fun sortedCopy(): Invitation {
        return this.copy(guests = this.guests.sortedWith(Guest.guestCompareBy))
    }
}

data class VisitRecord(val datetime: DateTime,
                       val userAgent: String?)