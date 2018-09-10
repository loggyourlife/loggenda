package com.logg.loggenda.widget

import android.content.Context
import android.graphics.Typeface
import android.support.annotation.ColorInt
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment
import com.logg.loggenda.R
import com.logg.loggenda.adapter.MonthAdapter
import com.logg.loggenda.listener.BaseDayClickListener
import com.logg.loggenda.listener.BaseMonthChangeListener
import com.logg.loggenda.listener.BaseSnapBlockListener
import com.logg.loggenda.listener.OnCollapseListener
import com.logg.loggenda.model.EventItem
import com.logg.loggenda.util.*
import kotlinx.android.synthetic.main.layout_loggenda.view.*
import org.joda.time.LocalDate
import java.util.*

class Loggenda @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {
    private var measuredViewHeight: Int = 0
    private var monthAdapter: MonthAdapter? = null
    private var selectedDay: LocalDate? = null
    private var nowDate: LocalDate? = null
    private var monthChangeListener: BaseMonthChangeListener? = null
    private var previousMonthPosition: Int = -1
    private var monthLayoutManager: MonthLayoutManager? = null
    private var dayClickListener: BaseDayClickListener? = null
    private var mSupportFragmentManager: FragmentManager? = null
    private var snapHelper: SnapToBlock? = null
    private var dayTypeFace: Typeface? = null
    private var dayTextColor: Int = 0
    private var dayTextSize: Float = 12f
    private var onCollapseListener: OnCollapseListener? = null

    init {
        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.layout_loggenda, this)
        dayItemHeight = ViewUtils.calculateDayItemHeight(context, 40)
        //setup()
    }

    fun setEvents(eventItems: MutableList<EventItem>?, position: Int) {
        monthAdapter?.getItem(position)!!.dayItems.forEach { dayItem ->
            eventItems?.firstOrNull { it.date == dayItem.date }?.let {
                dayItem.eventItem = it
            }
        }
        monthAdapter?.notifyItemChanged(position)
        monthLayoutManager?.setScrollEnabled(true)
    }

    fun setMonthChangeListener(monthChangeListener: BaseMonthChangeListener?) {
        this.monthChangeListener = monthChangeListener
        this.monthChangeListener?.onMonthChange(monthAdapter?.getItem(2)!!.monthDate, 2)
    }

    fun setDayClickListener(dayClickListener: BaseDayClickListener?) {
        this.dayClickListener = dayClickListener
        dayClickListener?.onDayClick(LocalDate(), 0, 0)
    }

    fun show(now: LocalDate, mSupportFragmentManager: FragmentManager?) {
        nowDate = now
        this.mSupportFragmentManager = mSupportFragmentManager
        setup()
    }

    fun setNow(now: LocalDate) {
        nowDate = now
        monthAdapter?.setNow(now)
    }

    private fun setup() {
        monthLayoutManager = MonthLayoutManager(context)
        monthLayoutManager?.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = monthLayoutManager
        recyclerView.setHasFixedSize(true)
        ViewCompat.setNestedScrollingEnabled(recyclerView, true)
        snapHelper = SnapToBlock(1)
        snapHelper?.attachToRecyclerView(recyclerView)
        recyclerView.itemAnimator = null
        monthAdapter = MonthAdapter(MonthUtils.generate3Month(nowDate!!, typeSelectedDateEnd), nowDate)
        selectedDay?.let {
            monthAdapter?.setSelectedDay(it)
        }
        recyclerView.adapter = monthAdapter
        recyclerView.scrollToPosition(2)
        snapHelper?.snappedPosition = 2
        updateMonthName(2)
        monthAdapter!!.dayClickListener = object : BaseDayClickListener() {
            override fun onDayClick(day: LocalDate, monthPosition: Int, dayPosition: Int) {
                super.onDayClick(day, monthPosition, dayPosition)
                dayClickListener?.onDayClick(day, monthPosition, dayPosition)
                selectedDay = day
            }
        }
        ViewUtils.addDayNameViews(dayNameViewGroup, Locale.getDefault(), R.color.azure, dayTextSize)
        snapHelper?.setSnapBlockCallback(object : BaseSnapBlockListener() {
            override fun onBlockSnapped(snapPosition: Int) {
                super.onBlockSnapped(snapPosition)
                var snapPosition = snapPosition
                if (snapPosition == 0) {
                    monthAdapter?.addMonth(MonthUtils.addMonth(monthAdapter?.getItem(snapPosition)!!.monthDate, true)!!, true)
                    snapPosition += 1
                    snapHelper?.snappedPosition = snapPosition
                    previousMonthPosition += 1
                } else if (snapPosition == monthAdapter?.itemCount!! - 1 && monthAdapter?.getItem(snapPosition)!!.monthDate != LocalDate().dayOfMonth().withMinimumValue()) {
                    monthAdapter?.addMonth(MonthUtils.addMonth(monthAdapter?.getItem(snapPosition)!!.monthDate, false)!!, false)
                    snapPosition -= 1
                    snapHelper?.snappedPosition = snapPosition
                    previousMonthPosition -= 1
                }
                if (previousMonthPosition != snapPosition) {
                    previousMonthPosition = snapPosition
                    monthLayoutManager?.setScrollEnabled(false)
                    monthChangeListener?.onMonthChange(monthAdapter?.getItem(snapPosition)!!.monthDate, snapPosition)
                }
                monthAdapter?.notifyItemChanged(snapPosition)
                updateMonthName(snapPosition)
            }
        })
        btnNext?.setOnClickListener {
            monthAdapter?.getItem(snapHelper?.snappedPosition!!)?.let {
                if (!it.monthDate.isEqual(nowDate?.dayOfMonth()?.withMinimumValue())) {
                    expand()
                    snapHelper?.snapToNext()
                }
            }
        }
        btnPrev?.setOnClickListener {
            expand()
            snapHelper?.snapToPrevious()
        }
        tvMonthName?.setOnClickListener {
            showCalendar()
        }
    }

    private fun showCalendar() {
        // Use the calendar for create ranges
        val calendar = GregorianCalendar.getInstance()
        calendar.clear()
        calendar.time = nowDate?.toDate()
        val maxDate = calendar.timeInMillis // Get milliseconds of the modified date
        val selectedMonth = calendar.get(Calendar.MONTH)
        val selectedYear = calendar.get(Calendar.YEAR)
        calendar.clear()
        calendar.set(2010, 0, 1) // Set minimum date to show in dialog
        val minDate = calendar.timeInMillis // Get milliseconds of the modified date

        val dialogFragment = MonthYearPickerDialogFragment
                .getInstance(selectedMonth, selectedYear, minDate, maxDate)
        dialogFragment.setOnDateSetListener { year, month ->
            val calendar = GregorianCalendar.getInstance()
            calendar.clear()
            calendar.time = nowDate?.toDate()
            if (calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year) {
                calendar.clear()
                calendar.set(year, month, 1) // Set minimum date to show in dialog
                updateCalendar(calendar, 2, typeSelectedDateEnd)
            } else {
                calendar.clear()
                calendar.set(year, month, 1) // Set minimum date to show in dialog
                updateCalendar(calendar, 1, typeSelectedDateCenter)
            }

        }
        dialogFragment.show(mSupportFragmentManager, null)
    }

    fun isCollapsed(): Boolean {
        return isCollapsed
    }

    private fun updateCalendar(calendar: Calendar, position: Int, type: Int) {
        monthAdapter?.refreshData(MonthUtils.generate3Month(LocalDate.fromCalendarFields(calendar), type))
        recyclerView.scrollToPosition(position)
        snapHelper?.snappedPosition = position
        this.monthChangeListener?.onMonthChange(monthAdapter?.getItem(position)!!.monthDate, position)
        updateMonthName(position)
    }

    private fun updateMonthName(position: Int) {
        tvMonthName.text = monthAdapter?.getItem(position)!!.monthDate.monthOfYear().getAsText(Locale.getDefault()) + ", " + monthAdapter?.getItem(position)!!.monthDate.year
    }

    fun setMonthScrollEnabled(isEnabled: Boolean) {
        monthLayoutManager?.setScrollEnabled(isEnabled)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (measuredViewHeight == 0) {
            layoutParams.height = (tvMonthName.measuredHeight + tvCalendarInfo.measuredHeight
                    + dayNameViewGroup.measuredHeight + ((dayItemHeight) * 7))
            measuredViewHeight = layoutParams.height
            isCollapsed = false
            monthLayoutManager?.setScrollEnabled(true)
            onCollapseListener?.OnCollapse(isCollapsed)
        }
    }

    fun collapse() {
        if (!isCollapsed) {
            isCollapsed = true
            monthLayoutManager?.setScrollEnabled(false)
            //recyclerView.isLayoutFrozen = true
            ViewUtils.collapse(this, 0,
                    (tvMonthName.measuredHeight + tvCalendarInfo.measuredHeight
                            + dayNameViewGroup.measuredHeight + dayItemHeight + ViewUtils.convertDpToPixel(context, 4)))
            onCollapseListener?.OnCollapse(isCollapsed)
        }
    }

    fun expand() {
        if (isCollapsed) {
            isCollapsed = false
            monthLayoutManager?.setScrollEnabled(true)
            //recyclerView.isLayoutFrozen = false
            //layoutParams.height = WRAP_CONTENT
            ViewUtils.expand(this, 0, measuredViewHeight)
            onCollapseListener?.OnCollapse(isCollapsed)
        }
    }

    fun setMonthNameTypeFace(typeface: Typeface) {
        tvMonthName.typeface = typeface
    }

    fun setMonthNameTextColor(@ColorInt color: Int) {
        tvMonthName.setTextColor(color)
    }

    fun setMonthNameTextSize(size: Float) {
        tvMonthName.textSize = size
    }

    fun setDayNameTypeFace(typeface: Typeface) {
        dayTypeFace = typeface
        dayNameViewGroup.removeAllViews()
        ViewUtils.addDayNameViews(dayNameViewGroup, Locale.getDefault(), if (dayTextColor != 0) dayTextColor else R.color.azure, dayTextSize, dayTypeFace)
    }

    fun setDayNameTextColor(@ColorInt color: Int) {
        dayTextColor = color
        dayNameViewGroup.removeAllViews()
        ViewUtils.addDayNameViews(dayNameViewGroup, Locale.getDefault(), if (dayTextColor != 0) dayTextColor else R.color.azure, dayTextSize, dayTypeFace)

    }

    fun setDayNameTextSize(size: Float) {
        dayTextSize = size
        dayNameViewGroup.removeAllViews()
        ViewUtils.addDayNameViews(dayNameViewGroup, Locale.getDefault(), if (dayTextColor != 0) dayTextColor else R.color.azure, dayTextSize, dayTypeFace)
    }

    fun setCalendarInfoText(info: String) {
        tvCalendarInfo.text = info
    }

    fun setCalendarInfoTypeFace(typeface: Typeface) {
        tvCalendarInfo.typeface = typeface
    }

    fun setCalendarInfoTextColor(@ColorInt color: Int) {
        tvCalendarInfo.setTextColor(color)
    }

    fun setCalendarInfoTextSize(size: Float) {
        tvCalendarInfo.textSize = size
    }

    fun setLoadingTint(@ColorInt color: Int) {
        pbCalendar?.indeterminateDrawable?.setTint(color)
    }

    fun showLoading() {
        pbCalendar.visibility = View.VISIBLE
        tvCalendarInfo.visibility = View.INVISIBLE
    }

    fun refreshView() {
        measuredViewHeight = 0
        this.measure(measuredWidth, measuredHeight)
        //invalidate()
        //requestLayout()
    }

    fun setPreviousButtonTint(@ColorInt color: Int) {
        btnPrev?.setColorFilter(color)
    }

    fun setNextButtonTint(@ColorInt color: Int) {
        btnNext?.setColorFilter(color)
    }

    fun setPreviousButtonImageResource(resourceId: Int) {
        btnPrev?.setImageResource(resourceId)
    }

    fun setNextButtonImageResource(resourceId: Int) {
        btnNext?.setImageResource(resourceId)
    }

    fun closeLoading() {
        pbCalendar.visibility = View.GONE
        tvCalendarInfo.visibility = View.VISIBLE
    }

    companion object {
        var dayItemHeight: Int = 0
        var isCollapsed: Boolean = false
    }

}