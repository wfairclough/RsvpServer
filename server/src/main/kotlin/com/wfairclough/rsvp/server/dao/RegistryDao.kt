package com.wfairclough.rsvp.server.dao

import com.mongodb.MongoException
import com.wfairclough.rsvp.server.model.RegistryItem
import com.wfairclough.rsvp.server.model.ResourceCreated
import com.wfairclough.rsvp.server.utils.Log
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection

/**
 * Created by will on 2017-06-12.
 */
class RegistryDao : BaseDao<RegistryItem>() {

    override val collection by lazy { database.getCollection<RegistryItem>() }

    fun createItem(registryItem: RegistryItem): ResourceCreated<RegistryItem>? {
        try {
            Log.i("Insert one: $registryItem")
            collection.insertOne(registryItem)
            return collection.findOne("{key: '${registryItem.key}'}")?.let {
                Log.i("Found the record for code ${registryItem.key}")
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