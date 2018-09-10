package com.logg.loggenda.listener

import org.joda.time.LocalDate


abstract class BaseDayClickListener : DayClickListener {
    override fun onDayClick(day: LocalDate, dayPosition: Int) {

    }

    override fun onDayClick(day: LocalDate, monthPosition: Int, dayPosition: Int) {

    }
}