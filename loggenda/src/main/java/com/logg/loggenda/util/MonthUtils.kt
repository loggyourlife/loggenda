package com.logg.loggenda.util

import com.logg.loggenda.model.DayItem
import com.logg.loggenda.model.MonthItem
import org.joda.time.LocalDate

const val typeSelectedDateCenter = 1
const val typeSelectedDateEnd = 2

object MonthUtils {
    fun generate3Month(selectedLocalDate: LocalDate, type: Int, isFirstDayWithSunday: Boolean = false): MutableList<MonthItem> {
        val monthItemList = mutableListOf<MonthItem>()
        val monthList = mutableListOf<LocalDate>()
        if (type == typeSelectedDateCenter) {
            monthList.add(selectedLocalDate.minusMonths(1))
            monthList.add(selectedLocalDate)
            monthList.add(selectedLocalDate.plusMonths(1))
        } else if (type == typeSelectedDateEnd) {
            monthList.add(selectedLocalDate.minusMonths(2))
            monthList.add(selectedLocalDate.minusMonths(1))
            monthList.add(selectedLocalDate)
        }
        monthList.forEach {
            val startDay = it.dayOfMonth().withMinimumValue()
            val endDay = it.dayOfMonth().withMaximumValue()
            val dayItemList = mutableListOf<DayItem>()
            if (startDay.dayOfWeek != 1) {
                for (i in 1..(startDay.dayOfWeek - 1)) {
                    dayItemList.add(DayItem(null, null, null, true))
                }
            }
            for (i in startDay.dayOfMonth..endDay.dayOfMonth) {

                dayItemList.add(DayItem(i.toString(), if (startDay.dayOfMonth == i) startDay else startDay.plusDays(i - startDay.dayOfMonth), null))
            }
            if (isFirstDayWithSunday) {
                monthItemList.add(MonthItem(if (startDay.dayOfWeek == 7) 1 else startDay.dayOfWeek + 1, dayItemList, startDay))
            } else {
                monthItemList.add(MonthItem(startDay.dayOfWeek, dayItemList, startDay))
            }
        }
        return monthItemList
    }

    fun addMonth(lastMonth: LocalDate, isLeftDirection: Boolean = true, isFirstDayWithSunday: Boolean = false): MonthItem? {
        var monthItem: MonthItem? = null
        val newMonth: LocalDate? = if (isLeftDirection) {
            lastMonth.minusMonths(1)
        } else {
            lastMonth.plusMonths(1)
        }
        newMonth?.let {
            val startDay = it.dayOfMonth().withMinimumValue()
            val endDay = it.dayOfMonth().withMaximumValue()
            val dayItemList = mutableListOf<DayItem>()
            if (startDay.dayOfWeek != 1) {
                for (i in 1..(startDay.dayOfWeek - 1)) {
                    dayItemList.add(DayItem(null, null, null, true))
                }
            }
            for (i in startDay.dayOfMonth..endDay.dayOfMonth) {

                dayItemList.add(DayItem(i.toString(), if (startDay.dayOfMonth == i) startDay else startDay.plusDays(i - startDay.dayOfMonth), null))
            }
            monthItem = if (isFirstDayWithSunday) {
                MonthItem(if (startDay.dayOfWeek == 7) 1 else startDay.dayOfWeek + 1, dayItemList, startDay)
            } else {
                MonthItem(startDay.dayOfWeek, dayItemList, startDay)
            }
        }
        return monthItem
    }
}