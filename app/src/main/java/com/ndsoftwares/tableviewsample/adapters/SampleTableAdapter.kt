package com.ndsoftwares.tableviewsample.adapters

import android.content.Context
import android.widget.TextView

import android.view.ViewGroup

import android.view.LayoutInflater
import android.view.View

import com.ndsoftwares.tableview.adapters.BaseTableAdapter
import com.ndsoftwares.tableviewsample.R


abstract class SampleTableAdapter(context: Context) : BaseTableAdapter() {
    private val context: Context

    /**
     * Quick access to the LayoutInflater instance that this Adapter retreived
     * from its Context.
     *
     * @return The shared LayoutInflater.
     */
    val inflater: LayoutInflater

    /**
     * Returns the context associated with this array adapter. The context is
     * used to create views from the resource passed to the constructor.
     *
     * @return The Context associated with this adapter.
     */
    fun getContext(): Context {
        return context
    }

    override fun getView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
        var converView: View? = convertView
        if (converView == null) {
            converView = inflater.inflate(getLayoutResource(row, column), parent, false)
        }
        setText(converView!!, getCellString(row, column))
        return converView
    }

    /**
     * Sets the text to the view.
     *
     * @param view
     * @param text
     */
    private fun setText(view: View, text: String) {
        (view.findViewById(android.R.id.text1) as TextView).text = text
    }

    /**
     * @param row
     * the title of the row of this header. If the column is -1
     * returns the title of the row header.
     * @param column
     * the title of the column of this header. If the column is -1
     * returns the title of the column header.
     * @return the string for the cell [row, column]
     */
    abstract fun getCellString(row: Int, column: Int): String
    abstract fun getLayoutResource(row: Int, column: Int): Int

    /**
     * Constructor
     *
     * @param context
     * The current context.
     */
    init {
        this.context = context
        inflater = LayoutInflater.from(context)
    }
}