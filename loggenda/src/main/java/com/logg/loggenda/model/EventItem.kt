package com.logg.loggenda.model

import org.joda.time.LocalDate

class EventItem(var haveReminder: Boolean, var iconList: List<Int>?, var date: LocalDate?, var totalEventCount: Int = 0)

