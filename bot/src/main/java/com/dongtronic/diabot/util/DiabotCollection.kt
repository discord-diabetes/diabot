package com.dongtronic.diabot.util

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
        val env = (envName ?: name).toUpperCase()
        val collection = (defaultCollection ?: env).toLowerCase().replace('_', '-')

        return mongoEnv("${env}_COLLECTION", collection)
    }
}