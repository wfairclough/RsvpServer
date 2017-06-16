package com.wfairclough.rsvp.server.dao

import com.mongodb.MongoException
import com.wfairclough.rsvp.server.model.Guest
import com.wfairclough.rsvp.server.model.Invitation
import com.wfairclough.rsvp.server.model.ResourceCreated
import com.wfairclough.rsvp.server.utils.Log
import org.litote.kmongo.find
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

/**
 * Created by will on 2017-05-22.
 */
class InvitationDao : BaseDao<Invitation>() {

    override val collection by lazy { database.getCollection<Invitation>() }

    fun create(invitation: Invitation): ResourceCreated<Invitation>? {
        try {
            Log.i("Insert one: $invitation")
            collection.insertOne(invitation)
            return collection.findOne("{code: '${invitation.code}'}")?.let {
                Log.i("Found the record for code ${invitation.code}")
                ResourceCreated(it.key, it.sortedCopy())
            }
        } catch (e: MongoException) {
            Log.e("Error: ${e.message}")
        } catch (e: Exception) {
            Log.e("Error2: ${e.message}")
        }
        Log.w("Could not find new record")
        return null
    }

    fun findByFirstAndLast(firstname: String, lastname: String): Invitation? =
            collection.findOne("{'guests.firstname': /^$firstname$/i, 'guests.lastname': /^$lastname$/i}")

    fun findAllGuests(skip: Int = 0, limit: Int = 100, rsvp: Boolean? = null): List<Guest> {
        val res = rsvp?.let { collection.find("{'guests.rsvp': $it}") } ?: collection.find()
        return res.skip(skip).limit(limit).flatMap { it.guests }.filter { g -> rsvp?.let { g.rsvp == it} ?: true }
    }

    fun findGuestByKey(key: String): Guest? {
        return collection.find("{'guests.key': '$key'}").flatMap { it.guests }.firstOrNull { it.key == key }
    }

    fun findByCode(code: String): Invitation? = collection.findOne("{'code': '${code.toLowerCase()}'}")

}