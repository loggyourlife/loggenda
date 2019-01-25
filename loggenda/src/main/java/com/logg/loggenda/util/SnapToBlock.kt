package com.logg.loggenda.util


import android.annotation.TargetApi
import android.os.Build
import androidx.core.view.ViewCompat
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.animation.Interpolator
import android.widget.Scroller
import androidx.recyclerview.widget.*
import com.logg.loggenda.listener.BaseSnapBlockListener

class SnapToBlock internal constructor(// Maxim blocks to move during most vigorous fling.
        private val mMaxFlingBlocks: Int) : LinearSnapHelper() {
    private var mRecyclerView: RecyclerView? = null

    // Total number of items in a block of view in the RecyclerView
    private var mBlocksize: Int = 0

    // Maximum number of positions to move on a fling.
    private var mMaxPositionsToMove: Int = 0

    // Width of a RecyclerView item if orientation is horizonal; height of the item if vertical
    private var mItemDimension: Int = 0

    // Callback interface when blocks are snapped.
    private var mSnapBlockCallback: BaseSnapBlockListener? = null

    // When snapping, used to determine direction of snap.
    private var mPriorFirstPosition = RecyclerView.NO_POSITION

    // Our private scroller
    private var mScroller: Scroller? = null

    // Horizontal/vertical layout helper
    private var mOrientationHelper: OrientationHelper? = null

    // LTR/RTL helper
    private var mLayoutDirectionHelper: LayoutDirectionHelper? = null

    private var snapToNext = false
    private var snapToPrevious = false
    var snappedPosition = 0

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: RecyclerView?) {

        if (recyclerView != null) {
            mRecyclerView = recyclerView
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            when {
                layoutManager.canScrollHorizontally() -> {
                    mOrientationHelper = OrientationHelper.createHorizontalHelper(layoutManager)
                    mLayoutDirectionHelper = LayoutDirectionHelper(ViewCompat.getLayoutDirection(mRecyclerView!!))
                }
                layoutManager.canScrollVertically() -> {
                    mOrientationHelper = OrientationHelper.createVerticalHelper(layoutManager)
                    // RTL doesn't matter for vertical scrolling for this class.
                    mLayoutDirectionHelper = LayoutDirectionHelper(RecyclerView.LAYOUT_DIRECTION_LTR)
                }
                else -> throw IllegalStateException("RecyclerView must be scrollable")
            }
            mScroller = Scroller(mRecyclerView!!.context, sInterpolator)
            initItemDimensionIfNeeded(layoutManager)
        }
        super.attachToRecyclerView(recyclerView)
    }

    // Called when the target view is available and we need to know how much more
    // to scroll to get it lined up with the side of the RecyclerView.
    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager,
                                              targetView: View): IntArray {
        val out = IntArray(2)

        if (layoutManager.canScrollHorizontally()) {
            out[0] = mLayoutDirectionHelper!!.getScrollToAlignView(targetView)
        }
        if (layoutManager.canScrollVertically()) {
            out[1] = mLayoutDirectionHelper!!.getScrollToAlignView(targetView)
        }
        if (mSnapBlockCallback != null) {
            if (out[0] == 0 && out[1] == 0) {
                mSnapBlockCallback!!.onBlockSnapped(layoutManager.getPosition(targetView))
            } else {
                mSnapBlockCallback!!.onBlockSnap(layoutManager.getPosition(targetView))
            }
        }
        return out
    }

    // We are flinging and need to know where we are heading.
    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager,
                                        velocityX: Int, velocityY: Int): Int {
        val lm = layoutManager as LinearLayoutManager

        initItemDimensionIfNeeded(layoutManager)
        mScroller!!.fling(0, 0, velocityX, velocityY, Integer.MIN_VALUE, Integer.MAX_VALUE,
                Integer.MIN_VALUE, Integer.MAX_VALUE)

        when {
            snapToNext -> {
                snapToNext = false
                snappedPosition = Math.min(mRecyclerView?.adapter?.itemCount
                        ?: 0, snappedPosition + 1)
            }
            snapToPrevious -> {
                snapToPrevious = false
                snappedPosition = Math.max(0, snappedPosition - 1)
            }
            else -> {
                if (velocityX != 0) {
                    snappedPosition = mLayoutDirectionHelper!!
                            .getPositionsToMove(lm, mScroller!!.finalX, mItemDimension)
                    return snappedPosition
                }
                snappedPosition = if (velocityY != 0) {
                    mLayoutDirectionHelper!!
                            .getPositionsToMove(lm, mScroller!!.finalY, mItemDimension)
                } else RecyclerView.NO_POSITION
            }
        }


        return snappedPosition
    }

    // We have scrolled to the neighborhood where we will snap. Determine the snap position.
    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        // Snap to a view that is either 1) toward the bottom of the data and therefore on screen,
        // or, 2) toward the top of the data and may be off-screen.
        val snapPos = calcTargetPosition(layoutManager as LinearLayoutManager)
        val snapView = if (snapPos == RecyclerView.NO_POSITION)
            null
        else
            layoutManager.findViewByPosition(snapPos)

        if (snapView == null) {
            Log.d(TAG, "<<<<findSnapView is returning null!")
        }
        Log.d(TAG, "<<<<findSnapView snapos=$snapPos")
        return snapView
    }

    // Does the heavy lifting for findSnapView.
    private fun calcTargetPosition(layoutManager: LinearLayoutManager): Int {
        val snapPos: Int
        val firstVisiblePos = layoutManager.findFirstVisibleItemPosition()

        if (firstVisiblePos == RecyclerView.NO_POSITION) {
            return RecyclerView.NO_POSITION
        }
        initItemDimensionIfNeeded(layoutManager)
        if (firstVisiblePos >= mPriorFirstPosition) {
            // Scrolling toward bottom of data
            val firstCompletePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
            snapPos = if (firstCompletePosition != RecyclerView.NO_POSITION && firstCompletePosition % mBlocksize == 0) {
                firstCompletePosition
            } else {
                roundDownToBlockSize(firstVisiblePos + mBlocksize)
            }
        } else {
            // Scrolling toward top of data
            snapPos = roundDownToBlockSize(firstVisiblePos)
            // Check to see if target view exists. If it doesn't, force a smooth scroll.
            // SnapHelper only snaps to existing views and will not scroll to a non-existant one.
            // If limiting fling to single block, then the following is not needed since the
            // views are likely to be in the RecyclerView pool.
            if (layoutManager.findViewByPosition(snapPos) == null) {
                val toScroll = mLayoutDirectionHelper!!.calculateDistanceToScroll(layoutManager, snapPos)
                mRecyclerView!!.smoothScrollBy(toScroll[0], toScroll[1], sInterpolator)
            }
        }
        mPriorFirstPosition = firstVisiblePos

        return snapPos
    }

    private fun initItemDimensionIfNeeded(layoutManager: RecyclerView.LayoutManager) {
        if (mItemDimension != 0) {
            return
        }
        val child: View
        try {
            child = layoutManager.getChildAt(0) ?: return
        } catch (ex: Exception) {
            return
        }

        when {
            layoutManager.canScrollHorizontally() -> {
                mItemDimension = child.width
                mBlocksize = getSpanCount(layoutManager) * (mRecyclerView!!.width / mItemDimension)
            }
            layoutManager.canScrollVertically() -> {
                mItemDimension = child.height
                mBlocksize = getSpanCount(layoutManager) * (mRecyclerView!!.height / mItemDimension)
            }
        }
        mMaxPositionsToMove = mBlocksize * mMaxFlingBlocks
    }

    private fun getSpanCount(layoutManager: RecyclerView.LayoutManager): Int {
        return (layoutManager as? GridLayoutManager)?.spanCount ?: 1
    }

    private fun roundDownToBlockSize(trialPosition: Int): Int {
        if (mBlocksize == 0) {
            mBlocksize = 1
        }
        return trialPosition - trialPosition % mBlocksize
    }

    private fun roundUpToBlockSize(trialPosition: Int): Int {
        return roundDownToBlockSize(trialPosition + mBlocksize - 1)
    }

    override fun createScroller(layoutManager: RecyclerView.LayoutManager): LinearSmoothScroller? {
        return if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            null
        } else object : LinearSmoothScroller(mRecyclerView!!.context) {
            override fun onTargetFound(targetView: View, state: RecyclerView.State, action: RecyclerView.SmoothScroller.Action) {
                val snapDistances = calculateDistanceToFinalSnap(mRecyclerView!!.layoutManager!!, targetView)
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(Math.max(Math.abs(dx), Math.abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, sInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi
            }
        }
    }

    fun setSnapBlockCallback(callback: BaseSnapBlockListener?) {
        mSnapBlockCallback = callback
    }

    /*
        Helper class that handles calculations for LTR and RTL layouts.
     */
    private inner class LayoutDirectionHelper @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    internal constructor(direction: Int) {

        // Is the layout an RTL one?
        private val mIsRTL: Boolean = direction == View.LAYOUT_DIRECTION_RTL

        /*
            Calculate the amount of scroll needed to align the target view with the layout edge.
         */
        internal fun getScrollToAlignView(targetView: View): Int {
            return if (mIsRTL)
                mOrientationHelper!!.getDecoratedEnd(targetView) - mRecyclerView!!.width
            else
                mOrientationHelper!!.getDecoratedStart(targetView)
        }

        /**
         * Calculate the distance to final snap position when the view corresponding to the snap
         * position is not currently available.
         *
         * @param layoutManager LinearLayoutManager or descendent class
         * @param targetPos     - Adapter position to snap to
         * @return int[2] {x-distance in pixels, y-distance in pixels}
         */
        internal fun calculateDistanceToScroll(layoutManager: LinearLayoutManager, targetPos: Int): IntArray {
            val out = IntArray(2)

            val firstVisiblePos: Int = layoutManager.findFirstVisibleItemPosition()
            if (layoutManager.canScrollHorizontally()) {
                if (targetPos <= firstVisiblePos) { // scrolling toward top of data
                    if (mIsRTL) {
                        val lastView = layoutManager.findViewByPosition(layoutManager.findLastVisibleItemPosition())
                        out[0] = mOrientationHelper!!.getDecoratedEnd(lastView) + (firstVisiblePos - targetPos) * mItemDimension
                    } else {
                        val firstView = layoutManager.findViewByPosition(firstVisiblePos)
                        out[0] = mOrientationHelper!!.getDecoratedStart(firstView) - (firstVisiblePos - targetPos) * mItemDimension
                    }
                }
            }
            if (layoutManager.canScrollVertically()) {
                if (targetPos <= firstVisiblePos) { // scrolling toward top of data
                    val firstView = layoutManager.findViewByPosition(firstVisiblePos)
                    out[1] = firstView!!.top - (firstVisiblePos - targetPos) * mItemDimension
                }
            }

            return out
        }

        /*
            Calculate the number of positions to move in the RecyclerView given a scroll amount
            and the size of the items to be scrolled. Return integral multiple of mBlockSize not
            equal to zero.
         */
        internal fun getPositionsToMove(llm: LinearLayoutManager, scroll: Int, itemSize: Int): Int {
            var positionsToMove: Int

            positionsToMove = roundUpToBlockSize(Math.abs(scroll) / itemSize)

            if (positionsToMove < mBlocksize) {
                // Must move at least one block
                positionsToMove = mBlocksize
            } else if (positionsToMove > mMaxPositionsToMove) {
                // Clamp number of positions to move so we don't get wild flinging.
                positionsToMove = mMaxPositionsToMove
            }

            if (scroll < 0) {
                positionsToMove *= -1
            }
            if (mIsRTL) {
                positionsToMove *= -1
            }

            return if (mLayoutDirectionHelper!!.isDirectionToBottom(scroll < 0)) {
                // Scrolling toward the bottom of data.
                roundDownToBlockSize(llm.findFirstVisibleItemPosition()) + positionsToMove
            } else roundDownToBlockSize(llm.findLastVisibleItemPosition()) + positionsToMove
            // Scrolling toward the top of the data.
        }

        internal fun isDirectionToBottom(velocityNegative: Boolean): Boolean {

            return if (mIsRTL) velocityNegative else !velocityNegative
        }
    }

    fun snapToNext() {
        snapToNext = true
        onFling(Int.MAX_VALUE, Int.MAX_VALUE)
    }

    fun snapToPrevious() {
        snapToPrevious = true
        onFling(Int.MAX_VALUE, Int.MAX_VALUE)
    }

    companion object {

        // Borrowed from ViewPager.java
        private val sInterpolator = Interpolator { t ->
            var t = t
            // _o(t) = t * t * ((tension + 1) * t + tension)
            // o(t) = _o(t - 1) + 1
            t -= 1.0f
            t * t * t + 1.0f
        }

        private const val MILLISECONDS_PER_INCH = 100f
        private const val TAG = "SnapToBlock"
    }
}