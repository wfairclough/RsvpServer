package com.wfairclough.rsvp.server.dao

import com.mongodb.MongoException
import com.wfairclough.rsvp.server.model.Invitation
import com.wfairclough.rsvp.server.model.ResourceCreated
import com.wfairclough.rsvp.server.utils.Log
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

/**
 * Created by will on 2017-05-22.
 */
class InvitationDao : BaseDao() {

    val collection = database.getCollection<Invitation>()

    fun create(invitation: Invitation): ResourceCreated<Invitation>? {
        try {
            Log.i("Insert one: $invitation")
            collection.insertOne(invitation)
            return collection.findOne("{code: '${invitation.code}'}")?.let {
                Log.i("Found the record for code ${invitation.code}")
                ResourceCreated(it.key, it)
            }
        } catch (e: MongoException) {
            Log.e("Error: ${e.message}")
        } catch (e: Exception) {
            Log.e("Error2: ${e.message}")
        }
        Log.w("Could not find new record")
        return null
    }
}