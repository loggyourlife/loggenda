package com.logg.loggenda.listener

import org.joda.time.LocalDate

interface DayClickListener {
    fun onDayClick(day: LocalDate, dayPosition: Int)
    fun onDayClick(day: LocalDate, monthPosition: Int, dayPosition: Int)
}