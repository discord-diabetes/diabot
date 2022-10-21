package com.dongtronic.diabot.exceptions

/**
 * Used when Nightscout data was unreadable for any reason
 */
open class NightscoutDataException(message: String?) : Exception(message)
