package com.wfairclough.rsvp.server.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by will on 2017-05-22.
 */
object Log {

    val logger: Logger by lazy { LoggerFactory.getLogger("RsvpServer") }

    fun d(msg: String) {
        logger.debug(msg)
    }

    fun i(msg: String) {
        logger.info(msg)
    }

    fun w(msg: String) {
        logger.warn(msg)
    }

    fun e(msg: String) {
        logger.error(msg)
    }

}