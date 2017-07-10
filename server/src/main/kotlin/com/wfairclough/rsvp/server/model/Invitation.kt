package com.wfairclough.rsvp.server.model

import org.joda.time.DateTime
import java.lang.reflect.Method

/**
 * Created by will on 2017-05-22.
 */
data class Invitation(override val _id: MongoID? = null,
                      override var key: DbKey = DbKeyUtils.generate(),
                      val code: String,
                      val viewed: Boolean = false,
                      val sent: Boolean = false,
                      val songRequest: String? = null,
                      val notes: String? = null,
                      val guests: List<Guest>,
                      val visits: List<VisitRecord>? = null) : MongoDocumentKeyable {
    fun sortedCopy(): Invitation {
        return this.copy(guests = this.guests.sortedWith(Guest.guestCompareBy))
    }

    /**
     * Only fill fields that are in the selectors list
     */
    fun applyProjection(selectors: Set<String>? = null): InvitationProjected {
        val selectorsInternal = selectors ?: setOf("key", "code", "viewed", "sent", "songRequest", "notes", "guests", "visits")
        val inviteProj = InvitationProjected()
        val clazz = inviteProj::class.java
        val thisClazz = this::class.java
        selectorsInternal.map { selector ->
            val getterMethod: Method? = thisClazz.getMethod("get${selector.capitalize()}")
            val thisValue = getterMethod?.invoke(this)
            thisValue?.let {
                val setterMethod: Method? = clazz.methods.singleOrNull { it.name == "set${selector.capitalize()}" }
                setterMethod?.invoke(inviteProj, thisValue)
            }
        }
        return inviteProj
    }
}

data class InvitationProjected(override var key: DbKey? = null,
                               var code: String? = null,
                               var viewed: Boolean? = null,
                               var sent: Boolean? = null,
                               var songRequest: String? = null,
                               var notes: String? = null,
                               var guests: List<Guest>? = null,
                               var visits: List<VisitRecord>? = null) : Keyable

data class VisitRecord(val datetime: DateTime,
                       val userAgent: String?,
                       val localAddress: String?,
                       val remoteAddress: String?,
                       val httpVersion: String?)