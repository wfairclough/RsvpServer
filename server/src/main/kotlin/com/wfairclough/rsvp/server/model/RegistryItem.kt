package com.wfairclough.rsvp.server.model

import org.joda.time.DateTime

/**
 * Created by will on 2017-06-11.
 */
data class RegistryItem(val _id: MongoID? = null,
                        var key: DbKey = DbKeyUtils.generate(),
                        val name: String,
                        val link: String,
                        val smImageLink: String?,
                        val lgImageLink: String?,
                        val purchasedRecord: PurchasedRecord?,
                        val linkFollowedRecords: List<LinkFollowedRecord>?)

data class LinkFollowedRecord(val followedAt: DateTime,
                              val followedBy: String?)

data class PurchasedRecord(val purchasedBy: String?)