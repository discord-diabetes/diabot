package com.dongtronic.diabot.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Creates a SLF4J logger named with the class which it is called from.
 *
 * @return A SLF4J [Logger] instance named with the calling class.
 */
inline fun <reified T> T.logger(): Logger {
    if (T::class.isCompanion) {
        // grab the parent class if this class is a companion object
        return LoggerFactory.getLogger(T::class.java.enclosingClass)
    }

    return LoggerFactory.getLogger(T::class.java)
}
