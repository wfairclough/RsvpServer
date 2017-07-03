package com.wfairclough.rsvp.server.model

import org.joda.time.DateTime;

/**
 * Created by will on 2017-05-22.
 */
data class Guest(override val key: DbKey? = DbKeyUtils.generate(),
                 val invitationKey: DbKey,
                 val firstname: String,
                 val lastname: String,
                 val menuItem: GuestMenuItem? = null,
                 val rsvp: Boolean? = null,
                 val plusOne: Boolean = false,
                 val hasAddedPlusOne: Boolean? = null,
                 val email: String? = null,
                 val address: Address? = null,
                 val phone: PhoneNo? = null,
                 val plusOneGuestKey: DbKey? = null,
                 val updated: DateTime? = null) : Keyable {

    companion object {
        val guestCompareBy: Comparator<Guest>
            get() = compareBy(
                    { it.plusOneGuestKey?.let { 1 } ?: 0 },
                    { it.plusOne.toInt() },
                    { it.lastname },
                    { it.firstname })

        val guestUpdatedCompareBy: Comparator<Guest>
            get() = compareBy({ Long.MAX_VALUE - (it.updated?.millis ?: 0) })
    }
}

data class Address(val street1: String,
                   val street2: String?,
                   val city: String,
                   val state: String,
                   val country: Country,
                   val code: String?)

enum class Country(val code: String) {
    Canada("CA"),
    UnitedStates("US"),
    Ireland("IE")
}

typealias PhoneNo = String

data class GuestMenuItem(val menuItemKey: DbKey, val notes: String?, val menuItem: MenuItem? = null)