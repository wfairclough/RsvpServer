package com.wfairclough.rsvp.server.dao

import com.mongodb.client.MongoCollection
import org.litote.kmongo.*
import org.litote.kmongo.MongoOperator.regex

object Client {
    val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
    val database = client.getDatabase("rsvp") //normal java driver usage
}

/**
 * Created by will on 2017-05-22.
 */
abstract class BaseDao<T> {

    val client = Client.client
    val database = Client.database

    abstract val collection: MongoCollection<T>

    fun filter(predicate: (T) -> Boolean): List<T> = collection.find().filter(predicate)

    fun findFirst(predicate: (T) -> Boolean): T? = collection.find().firstOrNull(predicate)

}