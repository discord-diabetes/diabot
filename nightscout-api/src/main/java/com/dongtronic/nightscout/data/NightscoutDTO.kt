package com.dongtronic.nightscout.data

import com.dongtronic.diabot.data.ConversionDTO
import com.dongtronic.nightscout.TrendArrow
import java.time.Instant

data class NightscoutDTO (
        var glucose: ConversionDTO? = null,
        var delta: ConversionDTO? = null,
        var dateTime: Instant? = null,
        var deltaIsNegative: Boolean = false,
        var low: Int = 0,
        var bottom: Int = 0,
        var top: Int = 0,
        var high: Int = 0,
        var trend: TrendArrow = TrendArrow.NONE,
        var iob: Float = 0.0F,
        var cob: Int = 0,
        var units: String = "",
        var title: String = "Nightscout"
)