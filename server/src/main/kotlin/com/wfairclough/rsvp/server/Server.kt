package com.wfairclough.rsvp.server

import com.wfairclough.rsvp.server.controllers.GuestsCtrl
import com.wfairclough.rsvp.server.controllers.InvitationCtrl
import com.wfairclough.rsvp.server.controllers.JsonTransformer
import com.wfairclough.rsvp.server.utils.Log
import spark.Route
import spark.Spark.port
import spark.Spark.path
import spark.Spark.before

val defaultTransformer = JsonTransformer()

val defaultContentType = "application/json"

fun main(args: Array<String>) {

    port(8080)

    path("/api") {
        before("/*") { req, rsp -> Log.i("Api request: ${req.uri()}") }
        path("/invitations") {
            post("/create", InvitationCtrl.create)

            post("/query", InvitationCtrl.query)

            get("/guests", GuestsCtrl.list)

            get("/:key", InvitationCtrl.get)

            path("/guests") {
                get("/:key", GuestsCtrl.get)
            }
        }
    }

}

inline fun get(path: String, route: Route) {
    spark.Spark.get(path, defaultContentType, route, defaultTransformer)
}

inline fun put(path: String, route: Route) {
    spark.Spark.put(path, defaultContentType, route, defaultTransformer)
}

inline fun post(path: String, route: Route) {
    spark.Spark.post(path, defaultContentType, route, defaultTransformer)
}

inline fun patch(path: String, route: Route) {
    spark.Spark.patch(path, defaultContentType, route, defaultTransformer)
}

inline fun delete(path: String, route: Route) {
    spark.Spark.delete(path, defaultContentType, route, defaultTransformer)
}

inline fun options(path: String, route: Route) {
    spark.Spark.options(path, defaultContentType, route, defaultTransformer)
}