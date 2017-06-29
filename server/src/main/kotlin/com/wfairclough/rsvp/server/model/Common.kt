package com.wfairclough.rsvp.server.model

import com.wfairclough.rsvp.server.json.ExcludeJson
import org.bson.types.ObjectId
import java.util.*

/**
 * Created by will on 2017-05-22.
 */

object DbKeyUtils {
    fun generate(): DbKey = UUID.randomUUID().toString()
}

typealias DbKey = String

typealias MongoID = ObjectId

data class ResourceCreated<out T>(val key: DbKey, @ExcludeJson val _data: T)

interface Keyable {
    val key: DbKey?
}

interface MongoDocument {
    val _id: MongoID?
}

interface MongoDocumentKeyable : MongoDocument, Keyable

fun Boolean.toInt(): Int = if (this) 1 else 0

data class ListResult<out T>(val items: List<T>, val count: Int)