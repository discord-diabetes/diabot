package com.dongtronic.diabot.util

import java.util.*

enum class DiabotCollection(private val envName: String? = null, private val defaultCollection: String? = null) {
    CHANNELS,
    NAME_RULES,
    NIGHTSCOUT,
    PROJECTS,
    QUOTE_INDEX,
    QUOTES,
    REWARDS,
    REWARDS_OPTOUT;

    /**
     * Reads the environment variable correlating to this collection.
     *
     * @return The collection name to use
     */
    fun getEnv(): String {
        val env = (envName ?: name).uppercase()
        val collection = (defaultCollection ?: env).lowercase().replace('_', '-')

        return mongoEnv("${env}_COLLECTION", collection)
    }
}
