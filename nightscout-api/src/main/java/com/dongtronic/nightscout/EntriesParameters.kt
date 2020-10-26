package com.dongtronic.nightscout

import org.litote.kmongo.MongoOperator
import java.net.URLEncoder
import kotlin.math.absoluteValue

/**
 * A builder/helper for finding specific entries via the Nightscout API.
 *
 * @param inputParameters parameters to add to the builder once initialised
 */
class EntriesParameters(inputParameters: Map<String, String> = emptyMap()) {
    private val parameters = mutableListOf<Pair<String, String>>()

    init {
        inputParameters.forEach { (key, value) ->
            parameters.add(key to value)
        }
    }

    /**
     * Adds a `find` query parameter for Nightscout
     *
     * @param keyName The name of the key to search under
     * @param value The value to search for
     * @param operator An optional MongoDB operator to fine-tune the search.
     * @return This [EntriesParameters] instance
     */
    fun find(keyName: String, value: String = "", operator: MongoOperator? = null): EntriesParameters {
        val keyBuilder = StringBuilder("find")
        keyBuilder.append("[").append(keyName).append("]")

        if (operator != null) {
            keyBuilder.append("[$").append(operator.name).append("]")
        }

        parameters.add(keyBuilder.toString() to value)
        return this
    }

    /**
     * Adds a `count` query parameter for Nightscout
     *
     * @param count The max amount of documents to find
     * @return This [EntriesParameters] instance
     */
    fun count(count: Int): EntriesParameters {
        parameters.add("count" to count.absoluteValue.toString())
        return this
    }

    /**
     * Adds a custom query parameter for Nightscout
     *
     * @param keyName The name of the parameter to add
     * @param value The value of the parameter to add
     * @return This [EntriesParameters] instance
     */
    fun add(keyName: String, value: String): EntriesParameters {
        parameters.add(keyName to value)
        return this
    }

    /**
     * Builds the parameters into a key-value map, while optionally keeping duplicate parameters.
     *
     * @param keepDuplicate Whether to keep duplicate parameters or to overwrite duplicates with the last-added duplicate parameter
     * @return Parameter key-value map
     */
    fun toMap(keepDuplicate: Boolean = true): Map<String, String> {
        if (!keepDuplicate) {
            return parameters.toMap()
        }

        val squashed = mutableMapOf<String, String>()

        parameters.forEach {
            squashed.merge(it.first, it.second) { old: String, toMerge: String ->
                old.plus("&${it.first}=${URLEncoder.encode(toMerge, "UTF-8")}")
            }
        }

        return squashed
    }
}