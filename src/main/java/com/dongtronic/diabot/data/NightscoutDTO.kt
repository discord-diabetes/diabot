package com.dongtronic.diabot.data

import java.time.ZonedDateTime

class NightscoutDTO {
    var glucose: ConversionDTO? = null
    var delta: ConversionDTO? = null
    var dateTime: ZonedDateTime? = null
    var deltaIsNegative: Boolean = false
    var low: Int = 0
    var bottom: Int = 0
    var top: Int = 0
    var high: Int = 0
    var trend: Int = 0
    var iob: Float = 0.0F
    var cob: Int = 0
    var units: String = ""
    var title: String = "Nightscout"
    set (value) {
        if (value != null) {
            field = value
        }
    }

}
