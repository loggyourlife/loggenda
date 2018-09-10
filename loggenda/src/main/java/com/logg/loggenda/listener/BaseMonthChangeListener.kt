package com.logg.loggenda.listener

import org.joda.time.LocalDate


abstract class BaseMonthChangeListener : MonthChangeListener {
    override fun onMonthChange(date: LocalDate, position: Int) {

    }
}