package com.wfairclough.rsvp.server.model

import com.wfairclough.rsvp.server.json.Exclude
import io.vertx.ext.web.RoutingContext
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

data class ResourceCreated<out T>(val key: DbKey, @Exclude val _data: T)

interface Keyable {
    val key: DbKey?
}

