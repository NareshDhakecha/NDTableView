package com.ndsoftwares.tableview

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.database.DataSetObserver
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.*
import android.widget.ImageView
import android.widget.Scroller
import com.ndsoftwares.tableview.adapters.TableAdapter
import java.util.ArrayList

class NDTableView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ViewGroup(context, attrs) {
    private var currentX = 0
    private var currentY = 0
    private var adapter: TableAdapter? = null
    private var scrllX = 0
    private var scrllY = 0
    private var firstRow = 0
    private var firstColumn = 0
    private lateinit var widths: IntArray
    private lateinit var heights: IntArray
    private var headView: View? = null
    private val rowViewList: MutableList<View> = mutableListOf()
    private val columnViewList: MutableList<View> = mutableListOf()
    private val bodyViewTable: MutableList<MutableList<View>> = mutableListOf()
    private var rowCount = 0
    private var columnCount = 0
    private var mWidth = 0
    private var mHeight = 0
    private lateinit var recycler: Recycler
    private lateinit var tableAdapterDataSetObserver: TableAdapterDataSetObserver
    private var needRelayout: Boolean
    private val shadows: Array<ImageView?>
    private val shadowSize: Int
    private val minimumVelocity: Int
    private val maximumVelocity: Int
    private val flinger: Flinger
    private var velocityTracker: VelocityTracker? = null
    private val touchSlop: Int

    /**
     * Constructor that is called when inflating a view from XML. This is called
     * when a view is being constructed from an XML file, supplying attributes
     * that were specified in the XML file. This version uses a default style of
     * 0, so the only attribute values applied are those in the Context's Theme
     * and the given AttributeSet.
     *
     * The method onFinishInflate() will be called after all children have been
     * added.
     *
     * @param context
     * The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     * @param attrs
     * The attributes of the XML tag that is inflating the view.
     */
    /**
     * Simple constructor to use when creating a view from code.
     *
     * @param context
     * The Context the view is running in, through which it can
     * access the current theme, resources, etc.
     */
    init {
//        columnViewList = MutableList()
//        bodyViewTable = MutableList()
        needRelayout = true
        shadows = arrayOfNulls(4)
        shadows[0] = ImageView(context)
        shadows[0]!!.setImageResource(R.drawable.shadow_left)
        shadows[1] = ImageView(context)
        shadows[1]!!.setImageResource(R.drawable.shadow_top)
        shadows[2] = ImageView(context)
        shadows[2]!!.setImageResource(R.drawable.shadow_right)
        shadows[3] = ImageView(context)
        shadows[3]!!.setImageResource(R.drawable.shadow_bottom)
        shadowSize = resources.getDimensionPixelSize(R.dimen.shadow_size)
        flinger = Flinger(context)
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop
        minimumVelocity = configuration.scaledMinimumFlingVelocity
        maximumVelocity = configuration.scaledMaximumFlingVelocity
        setWillNotDraw(false)
    }

    /**
     * Returns the adapter currently associated with this widget.
     *
     * @return The adapter used to provide this view's content.
     */
    fun getAdapter(): TableAdapter {
        return adapter!!
    }

    /**
     * Sets the data behind this TableFixHeaders.
     *
     * @param adapter
     * The TableAdapter which is responsible for maintaining the data
     * backing this list and for producing a view to represent an
     * item in that data set.
     */
    fun setAdapter(adapter: TableAdapter) {
        if (this.adapter != null) {
            this.adapter!!.unregisterDataSetObserver(tableAdapterDataSetObserver!!)
        }
        this.adapter = adapter
        tableAdapterDataSetObserver = TableAdapterDataSetObserver()
        this.adapter!!.registerDataSetObserver(tableAdapterDataSetObserver!!)
        recycler = Recycler(adapter.getViewTypeCount())
        scrllX = 0
        scrllY = 0
        firstColumn = 0
        firstRow = 0
        needRelayout = true
        requestLayout()
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        var intercept = false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                currentX = event.rawX.toInt()
                currentY = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val x2 = Math.abs(currentX - event.rawX.toInt())
                val y2 = Math.abs(currentY - event.rawY.toInt())
                if (x2 > touchSlop || y2 > touchSlop) {
                    intercept = true
                }
            }
        }
        return intercept
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (velocityTracker == null) { // If we do not have velocity tracker
            velocityTracker = VelocityTracker.obtain() // then get one
        }
        velocityTracker!!.addMovement(event) // add this movement to it
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!flinger.isFinished) { // If scrolling, then stop now
                    flinger.forceFinished()
                }
                currentX = event.rawX.toInt()
                currentY = event.rawY.toInt()
            }
            MotionEvent.ACTION_MOVE -> {
                val x2 = event.rawX.toInt()
                val y2 = event.rawY.toInt()
                val diffX = currentX - x2
                val diffY = currentY - y2
                currentX = x2
                currentY = y2
                scrollBy(diffX, diffY)
            }
            MotionEvent.ACTION_UP -> {
                val velocityTracker = velocityTracker
                velocityTracker!!.computeCurrentVelocity(1000, maximumVelocity.toFloat())
                val velocityX = velocityTracker.xVelocity.toInt()
                val velocityY = velocityTracker.yVelocity.toInt()
                if (Math.abs(velocityX) > minimumVelocity || Math.abs(velocityY) > minimumVelocity) {
                    flinger.start(
                        actualScrollX,
                        actualScrollY, velocityX, velocityY,
                        maxScrollX,
                        maxScrollY
                    )
                } else {
                    if (this.velocityTracker != null) { // If the velocity less than threshold
                        this.velocityTracker!!.recycle() // recycle the tracker
                        this.velocityTracker = null
                    }
                }
            }
        }
        return true
    }

    override fun scrollTo(x: Int, y: Int) {
        if (needRelayout) {
            scrllX = x
            firstColumn = 0
            scrllY = y
            firstRow = 0
        } else {
            scrollBy(
                x - sumArray(widths, 1, firstColumn) - scrllX,
                y - sumArray(heights, 1, firstRow) - scrllY
            )
        }
    }

    override fun scrollBy(x: Int, y: Int) {
        scrllX += x
        scrllY += y
        if (needRelayout) {
            return
        }
        scrollBounds()

        /*
		 * TODO Improve the algorithm. Think big diagonal movements. If we are
		 * in the top left corner and scrollBy to the opposite corner. We will
		 * have created the views from the top right corner on the X part and we
		 * will have eliminated to generate the right at the Y.
		 */if (scrllX == 0) {
            // no op
        } else if (scrllX > 0) {
            while (widths[firstColumn + 1] < scrllX) {
                if (!rowViewList.isEmpty()) {
                    removeLeft()
                }
                scrllX -= widths[firstColumn + 1]
                firstColumn++
            }
            while (filledWidth < mWidth) {
                addRight()
            }
        } else {
            while (!rowViewList.isEmpty() && filledWidth - widths[firstColumn + rowViewList.size] >= mWidth) {
                removeRight()
            }
            if (rowViewList.isEmpty()) {
                while (scrllX < 0) {
                    firstColumn--
                    scrllX += widths[firstColumn + 1]
                }
                while (filledWidth < mWidth) {
                    addRight()
                }
            } else {
                while (0 > scrllX) {
                    addLeft()
                    firstColumn--
                    scrllX += widths[firstColumn + 1]
                }
            }
        }
        if (scrllY == 0) {
            // no op
        } else if (scrllY > 0) {
            while (heights[firstRow + 1] < scrllY) {
                if (!columnViewList.isEmpty()) {
                    removeTop()
                }
                scrllY -= heights[firstRow + 1]
                firstRow++
            }
            while (filledHeight < mHeight) {
                addBottom()
            }
        } else {
            while (!columnViewList.isEmpty() && filledHeight - heights[firstRow + columnViewList.size] >= mHeight) {
                removeBottom()
            }
            if (columnViewList.isEmpty()) {
                while (scrllY < 0) {
                    firstRow--
                    scrllY += heights[firstRow + 1]
                }
                while (filledHeight < mHeight) {
                    addBottom()
                }
            } else {
                while (0 > scrllY) {
                    addTop()
                    firstRow--
                    scrllY += heights[firstRow + 1]
                }
            }
        }
        repositionViews()
        shadowsVisibility()
        awakenScrollBars()
    }

    /*
	 * The expected value is: percentageOfViewScrolled * computeHorizontalScrollRange()
	 */
    override fun computeHorizontalScrollExtent(): Int {
        val tableSize = (mWidth - widths[0]).toFloat()
        val contentSize = (sumArray(widths) - widths[0]).toFloat()
        val percentageOfVisibleView = tableSize / contentSize
        return Math.round(percentageOfVisibleView * tableSize)
    }

    /*
	 * The expected value is between 0 and computeHorizontalScrollRange() - computeHorizontalScrollExtent()
	 */
    override fun computeHorizontalScrollOffset(): Int {
        val maxScrollX = (sumArray(widths) - mWidth).toFloat()
        val percentageOfViewScrolled = actualScrollX / maxScrollX
        val maxHorizontalScrollOffset = mWidth - widths[0] - computeHorizontalScrollExtent()
        return widths[0] + Math.round(percentageOfViewScrolled * maxHorizontalScrollOffset)
    }

    /*
	 * The base measure
	 */
    override fun computeHorizontalScrollRange(): Int {
        return mWidth
    }

    /*
	 * The expected value is: percentageOfViewScrolled * computeVerticalScrollRange()
	 */
    override fun computeVerticalScrollExtent(): Int {
        val tableSize = (mHeight - heights[0]).toFloat()
        val contentSize = (sumArray(heights) - heights[0]).toFloat()
        val percentageOfVisibleView = tableSize / contentSize
        return Math.round(percentageOfVisibleView * tableSize)
    }

    /*
	 * The expected value is between 0 and computeVerticalScrollRange() - computeVerticalScrollExtent()
	 */
    override fun computeVerticalScrollOffset(): Int {
        val maxScrollY = (sumArray(heights) - mHeight).toFloat()
        val percentageOfViewScrolled = actualScrollY / maxScrollY
        val maxHorizontalScrollOffset = mHeight - heights[0] - computeVerticalScrollExtent()
        return heights[0] + Math.round(percentageOfViewScrolled * maxHorizontalScrollOffset)
    }

    /*
	 * The base measure
	 */
    override fun computeVerticalScrollRange(): Int {
        return mHeight
    }

    val actualScrollX: Int
        get() = scrllX + sumArray(widths, 1, firstColumn)
    val actualScrollY: Int
        get() = scrllY + sumArray(heights, 1, firstRow)
    private val maxScrollX: Int
        private get() = Math.max(0, sumArray(widths) - mWidth)
    private val maxScrollY: Int
        private get() = Math.max(0, sumArray(heights) - mHeight)
    private val filledWidth: Int
        private get() = widths[0] + sumArray(widths, firstColumn + 1, rowViewList.size) - scrllX
    private val filledHeight: Int
        private get() = heights[0] + sumArray(heights, firstRow + 1, columnViewList.size) - scrllY

    private fun addLeft() {
        addLeftOrRight(firstColumn - 1, 0)
    }

    private fun addTop() {
        addTopAndBottom(firstRow - 1, 0)
    }

    private fun addRight() {
        val size = rowViewList.size
        addLeftOrRight(firstColumn + size, size)
    }

    private fun addBottom() {
        val size = columnViewList.size
        addTopAndBottom(firstRow + size, size)
    }

    private fun addLeftOrRight(column: Int, index: Int) {
        var view = makeView(-1, column, widths[column + 1], heights[0])
        rowViewList.add(index, view)
        var i = firstRow
        for (list in bodyViewTable) {
            view = makeView(i, column, widths[column + 1], heights[i + 1])
            list.add(index, view)
            i++
        }
    }

    private fun addTopAndBottom(row: Int, index: Int) {
        var view = makeView(row, -1, widths[0], heights[row + 1])
        columnViewList.add(index, view)
        val list: MutableList<View> = ArrayList()
        val size = rowViewList.size + firstColumn
        for (i in firstColumn until size) {
            view = makeView(row, i, widths[i + 1], heights[row + 1])
            list.add(view)
        }
        bodyViewTable.add(index, list)
    }

    private fun removeLeft() {
        removeLeftOrRight(0)
    }

    private fun removeTop() {
        removeTopOrBottom(0)
    }

    private fun removeRight() {
        removeLeftOrRight(rowViewList.size - 1)
    }

    private fun removeBottom() {
        removeTopOrBottom(columnViewList.size - 1)
    }

    private fun removeLeftOrRight(position: Int) {
        removeView(rowViewList.removeAt(position))
        for (list in bodyViewTable) {
            removeView(list.removeAt(position))
        }
    }

    private fun removeTopOrBottom(position: Int) {
        removeView(columnViewList.removeAt(position))
        val remove = bodyViewTable.removeAt(position)
        for (view in remove) {
            removeView(view)
        }
    }

    override fun removeView(view: View) {
        super.removeView(view)
        val typeView = view.getTag(R.id.tag_type_view) as Int
        if (typeView != TableAdapter.IGNORE_ITEM_VIEW_TYPE) {
            recycler.addRecycledView(view, typeView)
        }
    }

    private fun repositionViews() {
        var left: Int
        var top: Int
        var right: Int
        var bottom: Int
        var i: Int
        left = widths[0] - scrllX
        i = firstColumn
        for (view in rowViewList) {
            right = left + widths[++i]
            view.layout(left, 0, right, heights[0])
            left = right
        }
        top = heights[0] - scrllY
        i = firstRow
        for (view in columnViewList) {
            bottom = top + heights[++i]
            view.layout(0, top, widths[0], bottom)
            top = bottom
        }
        top = heights[0] - scrllY
        i = firstRow
        for (list in bodyViewTable) {
            bottom = top + heights[++i]
            left = widths[0] - scrllX
            var j = firstColumn
            for (view in list) {
                right = left + widths[++j]
                view.layout(left, top, right, bottom)
                left = right
            }
            top = bottom
        }
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val w: Int
        val h: Int
        if (adapter != null) {
            rowCount = adapter!!.getRowCount()
            columnCount = adapter!!.getColumnCount()
            widths = IntArray(columnCount + 1)
            for (i in -1 until columnCount) {
                widths[i + 1] += adapter!!.getWidth(i)
            }
            heights = IntArray(rowCount + 1)
            for (i in -1 until rowCount) {
                heights[i + 1] += adapter!!.getHeight(i)
            }
            if (widthMode == MeasureSpec.AT_MOST) {
                w = Math.min(widthSize, sumArray(widths))
            } else if (widthMode == MeasureSpec.UNSPECIFIED) {
                w = sumArray(widths)
            } else {
                w = widthSize
                val sumArray = sumArray(widths)
                if (sumArray < widthSize) {
                    val factor = widthSize / sumArray.toFloat()
                    for (i in 1 until widths.size) {
                        widths[i] = Math.round(widths[i] * factor)
                    }
                    widths[0] = widthSize - sumArray(widths, 1, widths.size - 1)
                }
            }
            h = if (heightMode == MeasureSpec.AT_MOST) {
                Math.min(heightSize, sumArray(heights))
            } else if (heightMode == MeasureSpec.UNSPECIFIED) {
                sumArray(heights)
            } else {
                heightSize
            }
        } else {
            if (heightMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
                w = 0
                h = 0
            } else {
                w = widthSize
                h = heightSize
            }
        }
        if (firstRow >= rowCount || maxScrollY - actualScrollY < 0) {
            firstRow = 0
            scrllY = Int.MAX_VALUE
        }
        if (firstColumn >= columnCount || maxScrollX - actualScrollX < 0) {
            firstColumn = 0
            scrllX = Int.MAX_VALUE
        }
        setMeasuredDimension(w, h)
    }

    private fun sumArray(array: IntArray, firstIndex: Int = 0, count: Int = array.size): Int {
        var count = count
        var sum = 0
        count += firstIndex
        for (i in firstIndex until count) {
            sum += array[i]
        }
        return sum
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (needRelayout || changed) {
            needRelayout = false
            resetTable()
            if (adapter != null) {
                mWidth = r - l
                mHeight = b - t
                var left: Int
                var top: Int
                var right: Int
                var bottom: Int
                right = Math.min(mWidth, sumArray(widths))
                bottom = Math.min(mHeight, sumArray(heights))
                addShadow(shadows[0], widths[0], 0, widths[0] + shadowSize, bottom)
                addShadow(shadows[1], 0, heights[0], right, heights[0] + shadowSize)
                addShadow(shadows[2], right - shadowSize, 0, right, bottom)
                addShadow(shadows[3], 0, bottom - shadowSize, right, bottom)
                headView = makeAndSetup(-1, -1, 0, 0, widths[0], heights[0])
                scrollBounds()
                adjustFirstCellsAndScroll()
                left = widths[0] - scrllX
                run {
                    var i = firstColumn
                    while (i < columnCount && left < mWidth) {
                        right = left + widths[i + 1]
                        val view =
                            makeAndSetup(-1, i, left, 0, right, heights[0])
                        rowViewList.add(view)
                        left = right
                        i++
                    }
                }
                top = heights[0] - scrllY
                run {
                    var i = firstRow
                    while (i < rowCount && top < mHeight) {
                        bottom = top + heights[i + 1]
                        val view =
                            makeAndSetup(i, -1, 0, top, widths[0], bottom)
                        columnViewList.add(view)
                        top = bottom
                        i++
                    }
                }
                top = heights[0] - scrllY
                var i = firstRow
                while (i < rowCount && top < mHeight) {
                    bottom = top + heights[i + 1]
                    left = widths[0] - scrllX
                    val list: MutableList<View> = ArrayList()
                    var j = firstColumn
                    while (j < columnCount && left < mWidth) {
                        right = left + widths[j + 1]
                        val view = makeAndSetup(i, j, left, top, right, bottom)
                        list.add(view)
                        left = right
                        j++
                    }
                    bodyViewTable.add(list)
                    top = bottom
                    i++
                }
                shadowsVisibility()
            }
        }
    }

    private fun scrollBounds() {
        scrllX = scrollBounds(scrllX, firstColumn, widths, mWidth)
        scrllY = scrollBounds(scrllY, firstRow, heights, mHeight)
    }

    private fun scrollBounds(
        desiredScroll: Int,
        firstCell: Int,
        sizes: IntArray,
        viewSize: Int
    ): Int {
        var desiredScroll = desiredScroll
        if (desiredScroll == 0) {
            // no op
        } else if (desiredScroll < 0) {
            desiredScroll = Math.max(desiredScroll, -sumArray(sizes, 1, firstCell))
        } else {
            desiredScroll = Math.min(
                desiredScroll,
                Math.max(
                    0,
                    sumArray(sizes, firstCell + 1, sizes.size - 1 - firstCell) + sizes[0] - viewSize
                )
            )
        }
        return desiredScroll
    }

    private fun adjustFirstCellsAndScroll() {
        var values: IntArray
        values = adjustFirstCellsAndScroll(scrllX, firstColumn, widths)
        scrllX = values[0]
        firstColumn = values[1]
        values = adjustFirstCellsAndScroll(scrllY, firstRow, heights)
        scrllY = values[0]
        firstRow = values[1]
    }

    private fun adjustFirstCellsAndScroll(scroll: Int, firstCell: Int, sizes: IntArray): IntArray {
        var scroll = scroll
        var firstCell = firstCell
        if (scroll == 0) {
            // no op
        } else if (scroll > 0) {
            while (sizes[firstCell + 1] < scroll) {
                firstCell++
                scroll -= sizes[firstCell]
            }
        } else {
            while (scroll < 0) {
                scroll += sizes[firstCell]
                firstCell--
            }
        }
        return intArrayOf(scroll, firstCell)
    }

    private fun shadowsVisibility() {
        val actualScrollX = actualScrollX
        val actualScrollY = actualScrollY
        val remainPixels = intArrayOf(
            actualScrollX,
            actualScrollY,
            maxScrollX - actualScrollX,
            maxScrollY - actualScrollY
        )
        for (i in shadows.indices) {
            setAlpha(shadows[i], Math.min(remainPixels[i] / shadowSize.toFloat(), 1f))
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private fun setAlpha(imageView: ImageView?, alpha: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            imageView!!.alpha = alpha
        } else {
            imageView!!.setAlpha(Math.round(alpha * 255))
        }
    }

    private fun addShadow(imageView: ImageView?, l: Int, t: Int, r: Int, b: Int) {
        imageView!!.layout(l, t, r, b)
        addView(imageView)
    }

    private fun resetTable() {
        headView = null
        rowViewList.clear()
        columnViewList.clear()
        bodyViewTable.clear()
        removeAllViews()
    }

    private fun makeAndSetup(
        row: Int,
        column: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): View {
        val view = makeView(row, column, right - left, bottom - top)
        view.layout(left, top, right, bottom)
        return view
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val ret: Boolean
        val row = child.getTag(R.id.tag_row) as Int?
        val column = child.getTag(R.id.tag_column) as Int?
        // row == null => Shadow view
        if (row == null || row == -1 && column == -1) {
            ret = super.drawChild(canvas, child, drawingTime)
        } else {
            canvas.save()
            if (row == -1) {
                canvas.clipRect(widths[0], 0, canvas.width, canvas.height)
            } else if (column == -1) {
                canvas.clipRect(0, heights[0], canvas.width, canvas.height)
            } else {
                canvas.clipRect(widths[0], heights[0], canvas.width, canvas.height)
            }
            ret = super.drawChild(canvas, child, drawingTime)
            canvas.restore()
        }
        return ret
    }

    private fun makeView(row: Int, column: Int, w: Int, h: Int): View {
        val itemViewType: Int = adapter!!.getItemViewType(row, column)
        val recycledView: View? = if (itemViewType == TableAdapter.IGNORE_ITEM_VIEW_TYPE) {
            null
        } else {
            recycler.getRecycledView(itemViewType)
        }
        val view: View = adapter!!.getView(row, column, recycledView, this)
        view.setTag(R.id.tag_type_view, itemViewType)
        view.setTag(R.id.tag_row, row)
        view.setTag(R.id.tag_column, column)
        view.measure(
            MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
        )
        addTableView(view, row, column)
        return view
    }

    private fun addTableView(view: View, row: Int, column: Int) {
        if (row == -1 && column == -1) {
            addView(view, childCount - 4)
        } else if (row == -1 || column == -1) {
            addView(view, childCount - 5)
        } else {
            addView(view, 0)
        }
    }

    private inner class TableAdapterDataSetObserver : DataSetObserver() {
        override fun onChanged() {
            needRelayout = true
            requestLayout()
        }

        override fun onInvalidated() {
            // Do nothing
        }
    }

    // http://stackoverflow.com/a/6219382/842697
    private inner class Flinger internal constructor(context: Context?) :
        Runnable {
        private val scroller: Scroller
        private var lastX = 0
        private var lastY = 0
        fun start(
            initX: Int,
            initY: Int,
            initialVelocityX: Int,
            initialVelocityY: Int,
            maxX: Int,
            maxY: Int
        ) {
            scroller.fling(initX, initY, initialVelocityX, initialVelocityY, 0, maxX, 0, maxY)
            lastX = initX
            lastY = initY
            post(this)
        }

        override fun run() {
            if (scroller.isFinished) {
                return
            }
            val more = scroller.computeScrollOffset()
            val x = scroller.currX
            val y = scroller.currY
            val diffX = lastX - x
            val diffY = lastY - y
            if (diffX != 0 || diffY != 0) {
                scrollBy(diffX, diffY)
                lastX = x
                lastY = y
            }
            if (more) {
                post(this)
            }
        }

        val isFinished: Boolean
            get() = scroller.isFinished

        fun forceFinished() {
            if (!scroller.isFinished) {
                scroller.forceFinished(true)
            }
        }

        init {
            scroller = Scroller(context)
        }
    }

}