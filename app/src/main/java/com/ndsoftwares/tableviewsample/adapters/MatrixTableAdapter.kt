package com.ndsoftwares.tableviewsample.adapters

import android.content.Context
import android.content.res.Resources
import android.widget.TextView

import android.view.Gravity

import android.view.ViewGroup

import android.util.TypedValue
import android.view.View

import com.ndsoftwares.tableview.adapters.BaseTableAdapter


class MatrixTableAdapter<T>(context: Context, table: Array<Array<T>>?) :
    BaseTableAdapter() {
    private val context: Context = context
    private var table: Array<Array<T>>? = null
    private val width: Int
    private val height: Int

    constructor(context: Context) : this(context, null) {}

    fun setInformation(table: Array<Array<T>>?) {
        this.table = table
    }

    override fun getRowCount(): Int {
        return table!!.size - 1
    }

    override fun getColumnCount(): Int {
        return table!![0].size - 1
    }

    override fun getView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        var convertView: View? = convertView
        if (convertView == null) {
            convertView = TextView(context)
            (convertView as TextView).gravity = Gravity.CENTER_VERTICAL
        }
        (convertView as TextView).text = table!![row + 1][column + 1].toString()
        return convertView
    }

    override fun getHeight(row: Int): Int {
        return height
    }

    override fun getWidth(column: Int): Int {
        return width
    }

    override fun getItemViewType(row: Int, column: Int): Int {
        return 0
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    companion object {
        private const val WIDTH_DIP = 110
        private const val HEIGHT_DIP = 32
    }

    init {
        val r: Resources = context.resources
        width = Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                WIDTH_DIP.toFloat(),
                r.displayMetrics
            )
        )
        height = Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                HEIGHT_DIP.toFloat(),
                r.displayMetrics
            )
        )
        setInformation(table)
    }
}