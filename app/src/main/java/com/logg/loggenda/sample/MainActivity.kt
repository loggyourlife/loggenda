package com.logg.loggenda.sample

import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.logg.loggenda.listener.BaseDayClickListener
import com.logg.loggenda.listener.BaseMonthChangeListener
import com.logg.loggenda.model.EventItem
import kotlinx.android.synthetic.main.activity_main.*
import org.joda.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private var iconList: List<Int>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnCollapseToggle?.setOnClickListener(this)
        btnCustomize?.setOnClickListener(this)
        iconList = listOf(R.mipmap.accident, R.mipmap.baby_care, R.mipmap.baseball, R.mipmap.pilates, R.mipmap.eat)
        setupCalendarView()
    }

    private fun setupCalendarView() {
        loggenda.show(LocalDate.now(), supportFragmentManager)
        loggenda.setMonthChangeListener(object : BaseMonthChangeListener() {
            override fun onMonthChange(date: LocalDate, position: Int) {
                super.onMonthChange(date, position)
                loggenda.showLoading()
                Handler().postDelayed({
                    loggenda?.setEvents(generateData(date), position)
                    loggenda.closeLoading()
                }, 2000)
            }
        })
        loggenda.setDayClickListener(object : BaseDayClickListener() {
            override fun onDayClick(day: LocalDate, monthPosition: Int, dayPosition: Int) {
                super.onDayClick(day, monthPosition, dayPosition)
                tvSelectedDateText.text = day?.toString("dd MMMM yyyy", Locale.getDefault())
            }
        })
    }

    private fun customize() {
        loggenda.setLoadingTint(resources.getColor(R.color.oxford_blue))
        loggenda.setCalendarInfoText("Activities")
        loggenda.setCalendarInfoTextColor(resources.getColor(R.color.brick_red))
        loggenda.setCalendarInfoTextSize(18f)
        loggenda.setCalendarInfoTypeFace(Typeface.DEFAULT_BOLD)
        loggenda.setDayNameTextColor(resources.getColor(R.color.oxford_blue))
        loggenda.setDayNameTextSize(20f)
        loggenda.setDayNameTypeFace(Typeface.MONOSPACE)
        loggenda.setMonthNameTextColor(resources.getColor(R.color.brick_red))
        loggenda.setMonthNameTextSize(22f)
        loggenda.setMonthNameTypeFace(Typeface.SANS_SERIF)
        loggenda.setNextButtonImageResource(R.mipmap.collapse)
        loggenda.setNextButtonTint(resources.getColor(R.color.brick_red))
        loggenda.refreshView()
    }

    private fun generateData(localeDate: LocalDate): MutableList<EventItem>? {
        var localeDate = localeDate
        val endOfMonth = localeDate.dayOfMonth().withMaximumValue()
        val events = mutableListOf<EventItem>()
        val day = if (endOfMonth.isBefore(LocalDate())) {
            endOfMonth.dayOfMonth
        } else {
            LocalDate().dayOfMonth
        }
        (1..day).forEach {
            val rnd = getRandomNumber()
            events.add(EventItem(false, iconList!!.shuffled().subList(0, rnd), localeDate, if (rnd == 3) rnd * getRandomNumber() else rnd))
            localeDate = localeDate.plusDays(1)
        }
        return events
    }

    private fun getRandomNumber(): Int {
        return (1..3).shuffled().last()
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnCustomize -> {
                customize()
            }
            R.id.btnCollapseToggle -> {
                if (loggenda.isCollapsed()!!) {
                    loggenda.expand()
                    btnCollapseToggle?.setImageResource(R.mipmap.expand)
                } else {
                    loggenda.collapse()
                    btnCollapseToggle?.setImageResource(R.mipmap.collapse)
                }
            }
            R.id.tvSelectedDateText -> {
                if (loggenda.isCollapsed()!!) {
                    loggenda.expand()
                    btnCollapseToggle?.setImageResource(R.mipmap.expand)
                } else {
                    loggenda.collapse()
                    btnCollapseToggle?.setImageResource(R.mipmap.collapse)
                }
            }
        }
    }
}
