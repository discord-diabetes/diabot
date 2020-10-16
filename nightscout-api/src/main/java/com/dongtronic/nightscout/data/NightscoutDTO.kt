package com.dongtronic.nightscout.data

import com.dongtronic.nightscout.exceptions.NoNightscoutDataException

data class NightscoutDTO (
        var entries: Set<BgEntry> = emptySet(),
        var low: Int = 0,
        var bottom: Int = 0,
        var top: Int = 0,
        var high: Int = 0,
        var iob: Float = 0.0F,
        var cob: Int = 0,
        var units: String = "",
        var title: String = "Nightscout"
) {
    /**
     * Gets the most recent BG entry.
     *
     * @return the most recent BG entry
     * @throws NoNightscoutDataException if there is no BG entry available
     */
    fun getNewestEntry(): BgEntry {
        return entries.maxBy { it.dateTime }
                ?: throw NoNightscoutDataException()
    }

    /**
     * Gets the most recent BG entry, or null if there is not one.
     *
     * @return the most recent BG entry, or null
     */
    fun getNewestEntryOrNull(): BgEntry? {
        return try {
            getNewestEntry()
        } catch (e: NoNightscoutDataException) {
            null
        }
    }

    /**
     * Adds or replaces a BG entry in the set of entries
     *
     * @param bgEntry the BG entry to add/replace
     * @return true if the BG entry was replaced
     */
    fun replaceBgEntry(bgEntry: BgEntry): Boolean {
        val newEntries = entries.toMutableSet()
        val existed = newEntries.remove(bgEntry)
        newEntries.add(bgEntry)
        entries = newEntries
        return existed
    }
}