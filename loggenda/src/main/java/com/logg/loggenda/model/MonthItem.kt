package com.logg.loggenda.model

import org.joda.time.LocalDate

class MonthItem(var startOffset: Int, var dayItems: MutableList<DayItem>, var monthDate: LocalDate)
