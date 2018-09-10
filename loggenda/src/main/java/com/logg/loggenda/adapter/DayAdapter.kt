package com.logg.loggenda.adapter

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.logg.loggenda.R
import com.logg.loggenda.listener.BaseDayClickListener
import com.logg.loggenda.model.DayItem
import com.logg.loggenda.util.ViewUtils
import com.logg.loggenda.widget.Loggenda
import kotlinx.android.synthetic.main.recycler_day_item.view.*
import org.joda.time.LocalDate


class DayAdapter(private val data: List<DayItem>?, private var nowDate: LocalDate?) : RecyclerView.Adapter<DayAdapter.DayAdapterViewHolder>() {
    private var selectedDay: LocalDate? = null
    private var oldSelectedDay: LocalDate? = null
    var dayClickListener: BaseDayClickListener? = null
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayAdapterViewHolder {
        val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_day_item, parent, false)
        val params = view.layoutParams as GridLayoutManager.LayoutParams
        params.height = Loggenda.dayItemHeight
        params.width = Loggenda.dayItemHeight
        view.layoutParams = params
        view.requestLayout()
        setIconSize(view.ivIcon1)
        setIconSize(view.ivIcon2)
        setIconSize(view.ivIcon3)
        return DayAdapterViewHolder(view)
    }

    fun getItem(position: Int): DayItem? {
        return if (data != null) {
            data[position]

        } else null
    }

    private fun setIconSize(view: View) {
        val params = view.layoutParams as ConstraintLayout.LayoutParams
        params.height = (Loggenda.dayItemHeight - ViewUtils.convertDpToPixel(view.context, 4)) / 3
        params.width = params.height
        view.layoutParams = params
        view.requestLayout()
    }

    fun setSelectedDay(selectedDay: LocalDate) {
        oldSelectedDay = this.selectedDay
        this.selectedDay = selectedDay
    }

    override fun onBindViewHolder(vh: DayAdapterViewHolder, position: Int) {
        if (getItem(position)!!.isOverload) {
            vh.itemView.visibility = View.INVISIBLE
            vh.itemView.isEnabled = false
            vh.itemView.setOnClickListener(null)
        } else {
            vh.itemView.visibility = View.VISIBLE
            vh.itemView.isEnabled = true
            vh.itemView.dayItemLayout.background = null
            vh.itemView.tvDay.text = getItem(position)!!.day
            vh.itemView.tvDay.setTextColor(vh.itemView.resources.getColor(R.color.oxford_blue))
            vh.itemView.tvMoreCount.setTextColor(vh.itemView.resources.getColor(R.color.oxford_blue))
            vh.itemView.viewReminderDot.visibility = View.GONE
            vh.itemView.ivIcon2.visibility = View.INVISIBLE
            vh.itemView.ivIcon1.visibility = View.INVISIBLE
            vh.itemView.ivIcon3.visibility = View.INVISIBLE
            vh.itemView.setOnClickListener(null)
            if (getItem(position)!!.eventItem == null) {
                if (nowDate == getItem(position)!!.date) {
                    vh.itemView.dayItemLayout.setBackgroundResource(R.drawable.today_day_background)
                } else {
                    vh.itemView.dayItemLayout.background = null
                }
            } else {
                if (nowDate == getItem(position)!!.date) {
                    vh.itemView.dayItemLayout.setBackgroundResource(R.drawable.today_has_activity_day_background)
                } else {
                    vh.itemView.dayItemLayout.setBackgroundResource(R.drawable.has_activity_day_background)
                }

                if (getItem(position)!!.eventItem?.haveReminder!!) {
                    vh.itemView.viewReminderDot.visibility = View.VISIBLE
                }
            }
            getItem(position)?.eventItem?.iconList?.let {
                when (it.size) {
                    1 -> {
                        vh.itemView.ivIcon1.setImageResource(it[0])
                        vh.itemView.ivIcon1.visibility = View.VISIBLE
                        vh.itemView.ivIcon2.visibility = View.GONE
                        vh.itemView.ivIcon3.visibility = View.GONE
                        if (selectedDay != getItem(position)?.date) {
                            vh.itemView.ivIcon1.setColorFilter(vh.itemView.resources.getColor(R.color.azure))
                        }
                    }
                    2 -> {
                        vh.itemView.ivIcon1.setImageResource(it[0])
                        vh.itemView.ivIcon2.setImageResource(it[1])
                        vh.itemView.ivIcon2.visibility = View.VISIBLE
                        vh.itemView.ivIcon1.visibility = View.VISIBLE
                        vh.itemView.ivIcon3.visibility = View.GONE
                        if (selectedDay != getItem(position)?.date) {
                            vh.itemView.ivIcon1.setColorFilter(vh.itemView.resources.getColor(R.color.azure))
                            vh.itemView.ivIcon2.setColorFilter(vh.itemView.resources.getColor(R.color.azure))
                        }
                    }
                    3 -> {
                        vh.itemView.ivIcon1.setImageResource(it[0])
                        vh.itemView.ivIcon2.setImageResource(it[1])
                        vh.itemView.ivIcon3.setImageResource(it[2])
                        vh.itemView.ivIcon2.visibility = View.VISIBLE
                        vh.itemView.ivIcon1.visibility = View.VISIBLE
                        vh.itemView.ivIcon3.visibility = View.VISIBLE
                        if (selectedDay != getItem(position)?.date) {
                            vh.itemView.ivIcon1.setColorFilter(vh.itemView.resources.getColor(R.color.azure))
                            vh.itemView.ivIcon2.setColorFilter(vh.itemView.resources.getColor(R.color.azure))
                            vh.itemView.ivIcon3.setColorFilter(vh.itemView.resources.getColor(R.color.azure))
                        }
                    }
                }
            }
            if (getItem(position)?.eventItem != null && getItem(position)?.eventItem!!.totalEventCount > 3) {
                vh.itemView.tvMoreCount.text = "+" + (getItem(position)?.eventItem?.totalEventCount!! - 3)
                vh.itemView.tvMoreCount.visibility = View.VISIBLE
            } else {
                vh.itemView.tvMoreCount.visibility = View.GONE
            }
            if (selectedDay == getItem(position)?.date) {
                vh.itemView.setBackgroundResource(R.drawable.selected_day_background)
                vh.itemView.tvDay.setTextColor(vh.itemView.resources.getColor(R.color.white))
                vh.itemView.tvMoreCount.setTextColor(vh.itemView.resources.getColor(R.color.white))
                vh.itemView.ivIcon1.setColorFilter(vh.itemView.resources.getColor(R.color.white))
                vh.itemView.ivIcon2.setColorFilter(vh.itemView.resources.getColor(R.color.white))
                vh.itemView.ivIcon3.setColorFilter(vh.itemView.resources.getColor(R.color.white))
            }
            vh.itemView.setOnClickListener {
                getItem(vh.adapterPosition)?.date?.let {
                    if (it.isBefore(nowDate) || it.isEqual(nowDate)) {
                        oldSelectedDay = selectedDay
                        selectedDay = getItem(vh.adapterPosition)?.date
                        oldSelectedDay?.let {
                            notifyItemChanged(getPosition(it))
                        }
                        vh.itemView.setBackgroundResource(R.drawable.selected_day_background)
                        vh.itemView.tvDay.setTextColor(vh.itemView.resources.getColor(R.color.white))
                        vh.itemView.tvMoreCount.setTextColor(vh.itemView.resources.getColor(R.color.white))
                        vh.itemView.ivIcon1.setColorFilter(vh.itemView.resources.getColor(R.color.white))
                        vh.itemView.ivIcon2.setColorFilter(vh.itemView.resources.getColor(R.color.white))
                        vh.itemView.ivIcon3.setColorFilter(vh.itemView.resources.getColor(R.color.white))
                        dayClickListener?.onDayClick(getItem(vh.adapterPosition)!!.date!!, vh.adapterPosition)
                    }
                }
            }
        }
        //vh.itemView.tv
    }

    private fun getPosition(localDate: LocalDate): Int {
        return data?.indexOfFirst { it.date == localDate } ?: run { -1 }
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }

    class DayAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
        }
    }

}
