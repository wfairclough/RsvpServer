package com.wfairclough.rsvp.server.utils

/**
 * Created by will on 2017-06-11.
 */
class Options(val args: Array<String>) {

    fun getArg(key: String, def: String? = null): String? {
        return indexForKey(key)?.let { args[it + 1] } ?: def
    }

    fun hasArg(key: String, negativeDefault: Boolean = false): Boolean {
        if (indexForKey(key) != null) {
            return true
        } else {
            return negativeDefault
        }
    }

    private fun indexForKey(key: String): Int? {
        val keys = listOf("-$key", "--$key")
        return keys.map { args.indexOf(it) }.firstOrNull { it != -1 }
    }

    fun getIntArg(key: String, def: Int? = null): Int? {
        return getArg(key, def = null)?.toIntOrNull() ?: def
    }

    fun getBoolArg(key: String): Boolean? {
        return getArg(key, def = null)?.toBoolean()
    }

}