package com.dongtronic.diabot.logic.diabetes

enum class GlucoseUnit(vararg units: String) {
    // nightscout uses "mmol/l" and "mmol"
    // https://github.com/nightscout/cgm-remote-monitor#required
    MMOL("mmol/L", "mmol"),
    MGDL("mg/dL"),
    // this unit shouldn't be identified by name
    AMBIGUOUS("Ambiguous");

    /**
     * A list of names which identify this particular [GlucoseUnit]
     */
    val units: List<String> = units.toList()

    companion object {
        /**
         * Attempts to find a [GlucoseUnit] which matches the unit name given.
         * This function does not match [GlucoseUnit.AMBIGUOUS].
         *
         * @param unit The name of the unit to look up
         * @return [GlucoseUnit] if a unit was matched, null if no matches were found
         */
        fun byName(unit: String): GlucoseUnit? {
            return values().firstOrNull { glucoseUnit ->
                if (glucoseUnit == AMBIGUOUS) {
                    return@firstOrNull false
                }

                glucoseUnit.units.any { name ->
                    name.equals(unit, ignoreCase = true)
                }
            }
        }
    }
}
