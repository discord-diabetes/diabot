package com.dongtronic.nightscout

object NightscoutEndpoints {
    // I would have used an enum but retrofit requires compile-time constants
    const val STATUS = "/api/v1/status.json"
    const val ENTRIES = "/api/v1/entries.json"
    const val ENTRIES_SPEC = "/api/v1/entries/{spec}"
    const val PEBBLE = "/pebble"
}
