package com.wfairclough.rsvp.server.model

/**
 * Created by will on 2017-05-22.
 */
data class Guest(val key: DbKey? = null,
                 val firstname: String,
                 val lastname: String,
                 val menuItem: MenuItem? = null,
                 val email: String? = null,
                 val address: Address? = null,
                 val phone: List<PhoneNo>? = null)

data class Address(val street1: String,
                   val street2: String?,
                   val city: String,
                   val state: String,
                   val country: Country,
                   val code: String?)

enum class Country(val code: String) {
    Canada("CA"),
    UnitedStates("US"),
    Ireland("IRE")
}

typealias PhoneNo = String