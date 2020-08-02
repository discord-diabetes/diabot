package com.dongtronic.diabot.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class Logger : ReadOnlyProperty<Any, Logger> {
    private var logger: Logger? = null

    override fun getValue(thisRef: Any, property: KProperty<*>): Logger {
        if (logger == null)
            logger = LoggerFactory.getLogger(thisRef.javaClass)
        return logger!!
    }
}

inline fun <reified T> T.logger(): Logger {
    if (T::class.isCompanion) {
        return LoggerFactory.getLogger(T::class.java.enclosingClass)
    }

    return LoggerFactory.getLogger(T::class.java)
}