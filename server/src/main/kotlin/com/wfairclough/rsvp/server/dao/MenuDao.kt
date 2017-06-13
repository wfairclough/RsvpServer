package com.wfairclough.rsvp.server.dao

import com.mongodb.MongoException
import com.wfairclough.rsvp.server.model.MenuItem
import com.wfairclough.rsvp.server.model.ResourceCreated
import com.wfairclough.rsvp.server.utils.Log
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

/**
 * Created by will on 2017-06-12.
 */
class MenuDao : BaseDao<MenuItem>() {

    override val collection by lazy { database.getCollection<MenuItem>() }

    fun createItem(menuItem: MenuItem): ResourceCreated<MenuItem>? {
        try {
            Log.i("Insert one: $menuItem")
            collection.insertOne(menuItem)
            return collection.findOne("{key: '${menuItem.key}'}")?.let {
                Log.i("Found the record for code ${menuItem.key}")
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