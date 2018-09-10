package com.logg.loggenda.listener

import org.joda.time.LocalDate

interface MonthChangeListener {
    fun onMonthChange(date: LocalDate, position: Int)
}