package com.dongtronic.diabot.logic.nightscout

enum class TrendArrows(val unicode: String = "") {
    NONE,
    DOUBLEUP("↟"),
    SINGLEUP("↑"),
    FORTYFIVEUP("↗"),
    FLAT("→"),
    FORTYFIVEDOWN("↘"),
    SINGLEDOWN("↓"),
    DOUBLEDOWN("↡"),
    NOT_COMPUTABLE("↮"),
    RATE_OUT_OF_RANGE("↺");

    companion object {
        fun getTrend(name: String): TrendArrows {
            val sanitised = name.toUpperCase().replace(' ', '_')

            return values().firstOrNull { it.name == sanitised }
                    ?: throw IllegalArgumentException("Unknown direction $name")
        }

        fun getTrend(trend: Int): TrendArrows {
            return values().firstOrNull { it.ordinal == trend }
                    ?: throw IllegalArgumentException("Unknown trend $trend")
        }
    }
}