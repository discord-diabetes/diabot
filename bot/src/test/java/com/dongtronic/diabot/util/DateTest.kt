package com.dongtronic.diabot.util

import org.junit.Assert
import org.junit.Test
import java.sql.Timestamp

class DateTest {

    @Test
    fun stringToDate() {
        val timestamp = 1539672587884L
        val date = Timestamp(timestamp)

        Assert.assertNotNull(date)
    }
}
