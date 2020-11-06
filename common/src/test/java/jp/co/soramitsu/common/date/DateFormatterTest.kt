/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.common.date

import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.resourses.ResourceManager
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@RunWith(MockitoJUnitRunner::class)
class DateTimeFormatterTest {

    private lateinit var dateTimeFormatter: DateTimeFormatter
    private val date = Date(0)

    @Mock private lateinit var  resourceManager: ResourceManager

    @Before
    fun setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

        dateTimeFormatter = DateTimeFormatter(Locale.ENGLISH, resourceManager)
    }

    @Test
    fun `dd MMMM date formatting`() {
        val expectedResult = "01 January"

        val actualResult = dateTimeFormatter.formatDate(date, DateTimeFormatter.DD_MMMM)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `dd MMM YYYY date formatting`() {
        val expectedResult = "01 Jan 1970"

        val actualResult = dateTimeFormatter.formatDate(date, DateTimeFormatter.DD_MMM_YYYY)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `yesterday date to day formatting with year`() {
        val expectedResult = "Today"
        val yesterdayString = "Yesterday"
        val todayDate = Date()

        val actualResult = dateTimeFormatter.dateToDayWithYear(todayDate, expectedResult, yesterdayString)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `today date to day formatting with year`() {
        val todayString = "Today"
        val expectedResult = "Yesterday"
        val yesterdayDate = Date(Date().time - 24 * 60 * 60 * 1000)

        val actualResult = dateTimeFormatter.dateToDayWithYear(yesterdayDate, todayString, expectedResult)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `other date to day formatting with year`() {
        val todayString = "Today"
        val yesterdayString = "Yesterday"
        val expectedResult = "01 Jan 1970"

        val actualResult = dateTimeFormatter.dateToDayWithYear(date, todayString, yesterdayString)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `yesterday date to day formatting without year`() {
        val expectedResult = "Today"
        val yesterdayString = "Yesterday"
        val todayDate = Date()

        val actualResult = dateTimeFormatter.dateToDayWithoutCurrentYear(todayDate, expectedResult, yesterdayString)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `today date to day formatting without year`() {
        val todayString = "Today"
        val expectedResult = "Yesterday"
        val yesterdayDate = Date(Date().time - 24 * 60 * 60 * 1000)

        val actualResult = dateTimeFormatter.dateToDayWithoutCurrentYear(yesterdayDate, todayString, expectedResult)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `other date to day formatting without year`() {
        val todayString = "Today"
        val yesterdayString = "Yesterday"
        val expectedResult = "01 Jan 1970"

        val actualResult = dateTimeFormatter.dateToDayWithoutCurrentYear(date, todayString, yesterdayString)

        assertEquals(expectedResult, actualResult)
    }

    @Test
    fun `are in same day called`() {
        val date1 = Date()
        val date2 = Date(0)
        val actualResult = dateTimeFormatter.areInSameDay(date1, date1)

        assertEquals(true, actualResult)

        val actualResult2 = dateTimeFormatter.areInSameDay(date1, date2)

        assertEquals(false, actualResult2)
    }

    @Test
    fun `format Time From Seconds called`() {
        given(resourceManager.getString(R.string.common_hour)).willReturn("hour")
        given(resourceManager.getString(R.string.common_min)).willReturn("min")
        given(resourceManager.getString(R.string.common_sec)).willReturn("sec")
        val seconds = 7400.toLong()
        val exptectedString = "2h:3m:20s"
        val actualResult = dateTimeFormatter.formatTimeFromSeconds(seconds)

        assertEquals(exptectedString, actualResult)
    }
}
