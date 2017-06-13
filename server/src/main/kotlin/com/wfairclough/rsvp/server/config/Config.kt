package com.wfairclough.rsvp.server.config

/**
 * Created by will on 13/06/17.
 */
class Config(
        val domain: String = "localhost"
) {


    companion object {

        fun load(): Config {
            return Config()
        }

    }
}