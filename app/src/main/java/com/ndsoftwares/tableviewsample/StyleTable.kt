package com.ndsoftwares.tableviewsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ndsoftwares.tableviewsample.databinding.ActivityFamilyTableBinding

import android.content.Context
import android.content.res.Resources
import com.ndsoftwares.tableviewsample.adapters.SampleTableAdapter
import java.lang.RuntimeException


class StyleTable : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bv = ActivityFamilyTableBinding.inflate(layoutInflater)
        setContentView(bv.root)

        bv.table.setAdapter(MyAdapter(this))
    }

    class MyAdapter(context: Context) : SampleTableAdapter(context) {
        private val width: Int
        private val height: Int

        override fun getWidth(column: Int): Int {
            return width
        }

        override fun getHeight(row: Int): Int {
            return height
        }

        override fun getCellString(row: Int, column: Int): String {
            return "Lorem ($row, $column)"
        }

        override fun getLayoutResource(row: Int, column: Int): Int {
            val layoutResource: Int
            when (getItemViewType(row, column)) {
                0 -> layoutResource = R.layout.item_table1_header
                1 -> layoutResource = R.layout.item_table1
                else -> throw RuntimeException("wtf?")
            }
            return layoutResource
        }

        override fun getRowCount(): Int =10

        override fun getColumnCount(): Int =6

        override fun getItemViewType(row: Int, column: Int): Int {
            return if (row < 0) {
                0
            } else {
                1
            }
        }

        override fun getViewTypeCount(): Int = 2
//
//        val viewTypeCount: Int
//            get() = 2

        init {
            val resources: Resources = context.getResources()
            width = resources.getDimensionPixelSize(R.dimen.table_width)
            height = resources.getDimensionPixelSize(R.dimen.table_height)
        }
    }

}

