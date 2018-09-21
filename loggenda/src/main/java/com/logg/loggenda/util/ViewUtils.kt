package com.logg.loggenda.util

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Point
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import java.text.DateFormatSymbols
import java.util.*


object ViewUtils {
    fun addDayNameViews(rootLayout: ViewGroup, locale: Locale, color: Int, textSize: Float, typeface: Typeface? = null, isFirstDayWithSunday: Boolean = false) {
        val shortWeekDays = DateFormatSymbols.getInstance(locale).shortWeekdays.toMutableList()
        shortWeekDays.removeAt(0)
        if (!isFirstDayWithSunday) {
            val tempDay = shortWeekDays[0]
            shortWeekDays.removeAt(0)
            shortWeekDays.add(tempDay)
        }
        shortWeekDays.forEach { days ->
            val textView = TextView(rootLayout.context)
            textView.text = days.toUpperCase(locale)
            textView.textSize = textSize
            val lparams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, convertDpToPixel(rootLayout.context, 25))
            lparams.weight = 1f
            lparams.gravity = Gravity.CENTER_HORIZONTAL
            textView.layoutParams = lparams
            textView.gravity = Gravity.CENTER_HORIZONTAL
            //textView.setPadding(convertDpToPixel(rootLayout.context, 4), 0, 0, 0)
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            typeface?.let {
                textView.typeface = it
            }
            textView.setTextColor(color)
            rootLayout.addView(textView)
        }
    }

    fun calculateDayItemHeight(context: Context, marginDp: Int): Int {
        val display = (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        val width = size.x
        return (width - convertDpToPixel(context, (16 + marginDp))) / 7
    }

    /*fun convertPixelsToDp(context: Context, px: Float): Float {
        val metrics = context.resources.displayMetrics
        val dp = px / (metrics.densityDpi / 160f)
        return Math.round(dp).toFloat()
    }*/

    fun convertDpToPixel(context: Context, dp: Int): Int {
        val metrics = context.resources.displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return Math.round(px)
    }

    fun expand(v: View, duration: Int, targetHeight: Int) {
        val prevHeight = v.height
        v.visibility = View.VISIBLE
        val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
        valueAnimator.addUpdateListener { animation ->
            v.layoutParams.height = animation.animatedValue as Int
            v.requestLayout()
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }

    fun collapse(v: View, duration: Int, targetHeight: Int) {
        val prevHeight = v.height
        val valueAnimator = ValueAnimator.ofInt(prevHeight, targetHeight)
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.addUpdateListener { animation ->
            v.layoutParams.height = animation.animatedValue as Int
            v.requestLayout()
        }
        valueAnimator.interpolator = DecelerateInterpolator()
        valueAnimator.duration = duration.toLong()
        valueAnimator.start()
    }
}