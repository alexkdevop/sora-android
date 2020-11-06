/**
* Copyright Soramitsu Co., Ltd. All Rights Reserved.
* SPDX-License-Identifier: GPL-3.0
*/

package jp.co.soramitsu.common.date

import jp.co.soramitsu.common.R
import jp.co.soramitsu.common.resourses.ResourceManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class DateTimeFormatter(
    private val locale: Locale,
    private val resourceManager: ResourceManager
) {

    companion object {
        const val DD_MMM_YYYY = "dd MMM yyyy"
        const val DD_MMM_YYYY_HH_MM_SS = "dd/MM/yy HH:mm:ss"
        const val HH_mm = "HH:mm"
        const val HH_mm_ss = "HH:mm:ss"
        const val DD_MMMM = "dd MMMM"
    }

    fun formatDate(date: Date, dateFormat: String): String {
        return SimpleDateFormat(dateFormat, locale).format(date)
    }

    fun formatTimeWithoutSeconds(date: Date): String {
        return formatTime(date, HH_mm)
    }

    fun formatTimeWithSeconds(date: Date): String {
        return formatTime(date, HH_mm_ss)
    }

    private fun formatTime(date: Date, pattern: String): String {
        return SimpleDateFormat(pattern, locale).format(date)
    }

    fun dateToDayWithoutCurrentYear(date: Date, todayStr: String, yesterdayStr: String): String {
        return if (areInCurrentYear(date)) {
            dateToDay(date, todayStr, yesterdayStr, DD_MMMM)
        } else {
            dateToDayWithYear(date, todayStr, yesterdayStr)
        }
    }

    fun dateToDayWithYear(date: Date, todayStr: String, yesterdayStr: String): String {
        return dateToDay(date, todayStr, yesterdayStr, DD_MMM_YYYY)
    }

    private fun dateToDay(date: Date, todayStr: String, yesterdayStr: String, pattern: String): String {
        val newTime = Date()

        val cal = Calendar.getInstance().apply { time = newTime }
        val oldCal = Calendar.getInstance().apply { time = date }

        val oldYear = oldCal.get(Calendar.YEAR)
        val year = cal.get(Calendar.YEAR)
        val oldDay = oldCal.get(Calendar.DAY_OF_YEAR)
        val day = cal.get(Calendar.DAY_OF_YEAR)

        if (oldYear == year) {
            return when (oldDay - day) {
                -1 -> yesterdayStr
                0 -> todayStr
                else -> this.formatDate(date, pattern)
            }
        }

        return this.formatDate(date, pattern)
    }

    fun areInSameDay(date1: Date, date2: Date): Boolean {
        val cal = Calendar.getInstance().apply { time = date1 }
        val oldCal = Calendar.getInstance().apply { time = date2 }

        val oldYear = oldCal.get(Calendar.YEAR)
        val year = cal.get(Calendar.YEAR)
        val oldDay = oldCal.get(Calendar.DAY_OF_YEAR)
        val day = cal.get(Calendar.DAY_OF_YEAR)

        return oldYear == year && oldDay == day
    }

    private fun areInCurrentYear(date: Date): Boolean {
        val cal = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { time = date }

        return cal.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }

    fun formatTimeFromSeconds(timeInSeconds: Long): String {
        val result = StringBuilder()

        val hours = (timeInSeconds / 60 / 60).toInt()
        if (hours > 0) {
            result.append(hours)
            result.append(resourceManager.getString(R.string.common_hour).first())
            result.append(":")
        }

        val minutes = ((timeInSeconds - hours * 60 * 60) / 60).toInt()
        if (minutes > 0) {
            result.append(minutes)
            result.append(resourceManager.getString(R.string.common_min).first())
            result.append(":")
        }

        val seconds = timeInSeconds - hours * 60 * 60 - minutes * 60
        if (seconds > 0) {
            result.append(seconds)
            result.append(resourceManager.getString(R.string.common_sec).first())
        }

        return result.toString()
    }

    fun formatToOpenVotableDateString(time: Long): String {
        val diffInMillis = time - System.currentTimeMillis()

        if (diffInMillis < 0) return ""

        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return if (diffInDays == 0L) {
            val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
            if (diffInHours == 0L) {
                val diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
                if (diffInMinutes == 0L) {
                    val diffInSeconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
                    if (diffInSeconds == 0L) {
                        ""
                    } else {
                        resourceManager.getQuantityString(
                            R.plurals.project_date_second_plurals,
                            diffInSeconds.toInt(),
                            diffInSeconds.toString()
                        )
                    }
                } else {
                    resourceManager.getQuantityString(
                        R.plurals.project_date_minute_plurals,
                        diffInMinutes.toInt(),
                        diffInMinutes.toString()
                    )
                }
            } else {
                resourceManager.getQuantityString(
                    R.plurals.project_date_hour_plurals,
                    diffInHours.toInt(),
                    diffInHours.toString()
                )
            }
        } else {
            resourceManager.getQuantityString(
                R.plurals.project_date_day_plurals,
                diffInDays.toInt(),
                diffInDays.toString()
            )
        }
    }

    fun formatToClosedVotableDateString(time: Long, usePrefix: Boolean = true): String {
        val diffInMillis = System.currentTimeMillis() - time

        if (diffInMillis < 0) return ""

        val formatted = when (TimeUnit.MILLISECONDS.toDays(diffInMillis)) {
            0L -> {
                resourceManager.getString(R.string.common_today)
            }
            1L -> {
                resourceManager.getString(R.string.common_yesterday)
            }
            else -> {
                val currentCalendar = Calendar.getInstance()
                val projectCalendar = Calendar.getInstance()

                projectCalendar.timeInMillis = time
                val pattern = if (currentCalendar.get(Calendar.YEAR) == projectCalendar.get(Calendar.YEAR)) {
                    DD_MMMM
                } else {
                    DD_MMM_YYYY
                }

                formatDate(Date(time), pattern)
            }
        }

        return if (usePrefix) {
            resourceManager.getString(R.string.project_ended_template).format(formatted)
        } else {
            formatted
        }
    }
}