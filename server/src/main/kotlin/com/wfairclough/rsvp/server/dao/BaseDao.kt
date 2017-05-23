package com.wfairclough.rsvp.server.dao

import org.litote.kmongo.*

object Client {
    val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
    val database = client.getDatabase("rsvp") //normal java driver usage
}

/**
 * Created by will on 2017-05-22.
 */
open class BaseDao {

    val client = Client.client
    val database = Client.database

}