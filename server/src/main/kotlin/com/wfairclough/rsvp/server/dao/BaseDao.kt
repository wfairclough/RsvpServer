package com.wfairclough.rsvp.server.dao

import com.mongodb.client.MongoCollection
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.ReturnDocument
import com.wfairclough.rsvp.server.model.Keyable
import org.litote.kmongo.*

object Client {
    val client = KMongo.createClient() //get com.mongodb.MongoClient new instance
    val database = client.getDatabase("rsvp") //normal java driver usage
}

/**
 * Created by will on 2017-05-22.
 */
abstract class BaseDao<T : Keyable> {

    val client = Client.client
    val database = Client.database

    abstract val collection: MongoCollection<T>

    fun filter(predicate: (T) -> Boolean): List<T> = collection.find().filter(predicate)

    fun findFirst(predicate: (T) -> Boolean): T? = collection.find().firstOrNull(predicate)

    fun findByKey(key: String): T? = collection.findOne("{'key': '$key'}")

    fun findAll(skip: Int = 0, limit: Int = 100): List<T> = collection.find().skip(skip).limit(limit).distinct()

    fun update(keyable: T): T? = collection.findOneAndReplace(
                filter = "{'key': '${keyable.key}'}",
                replacement = keyable,
                options = FindOneAndReplaceOptions().returnDocument(ReturnDocument.AFTER))

    fun find(query: String = "{}"): List<T> {
        return collection.find(query).toList()
    }
}