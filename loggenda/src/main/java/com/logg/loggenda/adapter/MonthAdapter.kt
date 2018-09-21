package com.logg.loggenda.adapter

import android.annotation.SuppressLint
import android.support.v4.view.ViewCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.logg.loggenda.R
import com.logg.loggenda.listener.BaseDayClickListener
import com.logg.loggenda.model.MonthItem
import com.logg.loggenda.util.SnapToBlock
import kotlinx.android.synthetic.main.recycler_month_item.view.*
import org.joda.time.LocalDate


class MonthAdapter(private val data: MutableList<MonthItem>?, private var nowDate: LocalDate?) : RecyclerView.Adapter<MonthAdapter.MonthAdapterViewHolder>() {

    private var oldSelectedMonthAdapterPosition: Int = -1
    private var selectedMonthAdapterPosition: Int = -1
    var dayClickListener: BaseDayClickListener? = null
    private var selectedDay: LocalDate? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthAdapterViewHolder {
        return MonthAdapterViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_month_item, parent, false))
    }

    fun getItem(position: Int): MonthItem? {
        return if (data != null && position >= 0) {
            if (data.size > position) {
                data[position]
            } else null
        } else null
    }


    override fun onBindViewHolder(vh: MonthAdapterViewHolder, position: Int) {
        initChildLayoutManager(vh, vh.adapterPosition)
    }

    fun setSelectedDay(selectedDay: LocalDate) {
        this.selectedDay = selectedDay
    }

    fun setNow(nowDate: LocalDate?) {
        this.nowDate = nowDate
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initChildLayoutManager(vh: MonthAdapterViewHolder, position: Int) {
        val rvChild = vh.itemView.recyclerDay
        rvChild.onFlingListener = null
        rvChild.layoutManager = GridLayoutManager(rvChild.context, 7)
        rvChild.setHasFixedSize(true)
        ViewCompat.setNestedScrollingEnabled(rvChild, true)


        val snapHelper = SnapToBlock(1)
        snapHelper.attachToRecyclerView(rvChild)
        rvChild.itemAnimator = null
        val dayAdapter = DayAdapter(getItem(position)!!.dayItems, nowDate)
        selectedDay?.let {
            dayAdapter.setSelectedDay(it)
        }
        rvChild.adapter = dayAdapter
        dayAdapter.dayClickListener = object : BaseDayClickListener() {
            override fun onDayClick(day: LocalDate, dayPosition: Int) {
                super.onDayClick(day, dayPosition)
                oldSelectedMonthAdapterPosition = selectedMonthAdapterPosition
                selectedMonthAdapterPosition = vh.adapterPosition
                selectedDay = day
                if (selectedMonthAdapterPosition != oldSelectedMonthAdapterPosition) {
                    notifyItemChanged(oldSelectedMonthAdapterPosition)
                }
                dayClickListener?.onDayClick(day, vh.adapterPosition, dayPosition)
            }
        }

    }

    fun refreshData(data: MutableList<MonthItem>?) {
        data?.let {
            this.data?.clear()
            this.data?.addAll(data)
            notifyDataSetChanged()
        }
    }

    fun addMonth(item: MonthItem, isLeft: Boolean = true) {
        data?.let {
            if (isLeft) {
                it.add(0, item)
                notifyItemInserted(0)
                if (it.size > 1) {
                    it.removeAt(it.size - 1)
                    notifyItemRemoved(it.size)
                }
            } else {
                it.add(item)
                notifyItemInserted(it.size - 1)
                it.removeAt(0)
                notifyItemRemoved(0)
            }
        }
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    class MonthAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
