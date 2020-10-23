package com.dongtronic.diabot.logic.nightscout

object NightscoutEndpoints {
    // i would have used an enum but retrofit requires compile-time constants
    const val STATUS = "/api/v1/status.json"
    const val ENTRIES = "/api/v1/entries.json"
    const val ENTRIES_SPEC = "/api/v1/entries/{spec}"
    const val PEBBLE = "/pebble"
}