package com.dongtronic.diabot.util

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.sql.Timestamp

class DateTest {

    @Test
    fun stringToDate() {
        val timestamp = 1539672587884L
        val date = Timestamp(timestamp)

        Assertions.assertNotNull(date)
    }
}
