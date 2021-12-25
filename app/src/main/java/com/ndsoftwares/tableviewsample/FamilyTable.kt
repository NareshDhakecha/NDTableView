package com.ndsoftwares.tableviewsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

import android.content.Context
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup

import com.ndsoftwares.tableview.adapters.BaseTableAdapter
import com.ndsoftwares.tableviewsample.databinding.ActivityFamilyTableBinding
import java.lang.RuntimeException

class FamilyTable : AppCompatActivity() {

    private lateinit var bv: ActivityFamilyTableBinding


    private class NexusTypes internal constructor(val name: String) {
        val list: MutableList<Nexus>
        fun size(): Int {
            return list.size
        }

        operator fun get(i: Int): Nexus {
            return list[i]
        }

        init {
            list = ArrayList()
        }
    }

    private class Nexus(
        name: String,
        company: String,
        version: String,
        api: String,
        storage: String,
        inches: String,
        ram: String
    ) {
        val data: Array<String> = arrayOf(
            name,
            company,
            version,
            api,
            storage,
            inches,
            ram
        )

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bv = ActivityFamilyTableBinding.inflate(layoutInflater)
        setContentView(bv.root)

        val baseTableAdapter: BaseTableAdapter = FamilyNexusAdapter(this)
        bv.table.setAdapter(baseTableAdapter)
    }

    class FamilyNexusAdapter(val context: Context) : BaseTableAdapter() {
        private val familys: Array<NexusTypes> = arrayOf(
            NexusTypes("Mobiles"),
            NexusTypes("Tablets"),
            NexusTypes("Others")
        )
        private val headers = arrayOf(
            "Name",
            "Company",
            "Version",
            "API",
            "Storage",
            "Size",
            "RAM"
        )
        private val widths = intArrayOf(
            120,
            100,
            140,
            60,
            70,
            60,
            60
        )
        private val density: Float = context.getResources().getDisplayMetrics().density
        override fun getRowCount(): Int {
            return 14
        }

        override fun getColumnCount(): Int {
            return 6
        }

        override fun getView(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
            val view: View = when (getItemViewType(row, column)) {
                0 -> getFirstHeader(row, column, convertView, parent)
                1 -> getHeader(row, column, convertView, parent)
                2 -> getFirstBody(row, column, convertView, parent)
                3 -> getBody(row, column, convertView, parent)
                4 -> getFamilyView(row, column, convertView, parent)
                else -> throw RuntimeException("wtf?")
            }
            return view
        }

        private fun getFirstHeader(
            row: Int,
            column: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            var convertView: View? = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_table_header_first, parent, false)
            }
            (convertView!!.findViewById(android.R.id.text1) as TextView).text = headers[0]
            return convertView
        }

        private fun getHeader(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
            var convertView: View? = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_table_header, parent, false)
            }
            (convertView!!.findViewById(android.R.id.text1) as TextView).text = headers[column + 1]
            return convertView
        }

        private fun getFirstBody(
            row: Int,
            column: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            var convertView: View? = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_table_first, parent, false)
            }
            convertView!!.setBackgroundResource(if (row % 2 == 0) R.drawable.bg_table_color1 else R.drawable.bg_table_color2)
            (convertView!!.findViewById(android.R.id.text1) as TextView).text =
                getDevice(row).data[column + 1]
            return convertView
        }

        private fun getBody(row: Int, column: Int, convertView: View?, parent: ViewGroup): View {
            var convertView: View? = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_table, parent, false)
            }
            convertView!!.setBackgroundResource(if (row % 2 == 0) R.drawable.bg_table_color1 else R.drawable.bg_table_color2)
            (convertView.findViewById(android.R.id.text1) as TextView).text =
                getDevice(row).data[column + 1]
            return convertView
        }

        private fun getFamilyView(
            row: Int,
            column: Int,
            convertView: View?,
            parent: ViewGroup
        ): View {
            var convertView: View? = convertView
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.context).inflate(R.layout.item_table_family, parent, false)
            }
            val string: String = if (column == -1) {
                getFamily(row).name
            } else {
                ""
            }
            (convertView!!.findViewById(android.R.id.text1) as TextView).text = string
            return convertView
        }

        override fun getWidth(column: Int): Int {
            return Math.round(widths[column + 1] * density)
        }

        override fun getHeight(row: Int): Int {
            val height: Int
            height = if (row == -1) {
                35
            } else if (isFamily(row)) {
                25
            } else {
                45
            }
            return Math.round(height * density)
        }

        override fun getItemViewType(row: Int, column: Int): Int {
            val itemViewType: Int
            itemViewType = if (row == -1 && column == -1) {
                0
            } else if (row == -1) {
                1
            } else if (isFamily(row)) {
                4
            } else if (column == -1) {
                2
            } else {
                3
            }
            return itemViewType
        }

        private fun isFamily(row: Int): Boolean {
            var row = row
            var family = 0
            while (row > 0) {
                row -= familys[family].size() + 1
                family++
            }
            return row == 0
        }

        private fun getFamily(row: Int): NexusTypes {
            var row = row
            var family = 0
            while (row >= 0) {
                row -= familys[family].size() + 1
                family++
            }
            return familys[family - 1]
        }

        private fun getDevice(row: Int): Nexus {
            var row = row
            var family = 0
            while (row >= 0) {
                row -= familys[family].size() + 1
                family++
            }
            family--
            return familys[family][row + familys[family].size()]
        }

        override fun getViewTypeCount(): Int {
            return 5
        }

        init {
            familys[0].list.add(
                Nexus(
                    "Nexus One",
                    "HTC",
                    "Gingerbread",
                    "10",
                    "512 MB",
                    "3.7\"",
                    "512 MB"
                )
            )
            familys[0].list.add(
                Nexus(
                    "Nexus S",
                    "Samsung",
                    "Gingerbread",
                    "10",
                    "16 GB",
                    "4\"",
                    "512 MB"
                )
            )
            familys[0].list.add(
                Nexus(
                    "Galaxy Nexus (16 GB)",
                    "Samsung",
                    "Ice cream Sandwich",
                    "15",
                    "16 GB",
                    "4.65\"",
                    "1 GB"
                )
            )
            familys[0].list.add(
                Nexus(
                    "Galaxy Nexus (32 GB)",
                    "Samsung",
                    "Ice cream Sandwich",
                    "15",
                    "32 GB",
                    "4.65\"",
                    "1 GB"
                )
            )
            familys[0].list.add(
                Nexus(
                    "Nexus 4 (8 GB)",
                    "LG",
                    "Jelly Bean",
                    "17",
                    "8 GB",
                    "4.7\"",
                    "2 GB"
                )
            )
            familys[0].list.add(
                Nexus(
                    "Nexus 4 (16 GB)",
                    "LG",
                    "Jelly Bean",
                    "17",
                    "16 GB",
                    "4.7\"",
                    "2 GB"
                )
            )
            familys[1].list.add(
                Nexus(
                    "Nexus 7 (16 GB)",
                    "Asus",
                    "Jelly Bean",
                    "16",
                    "16 GB",
                    "7\"",
                    "1 GB"
                )
            )
            familys[1].list.add(
                Nexus(
                    "Nexus 7 (32 GB)",
                    "Asus",
                    "Jelly Bean",
                    "16",
                    "32 GB",
                    "7\"",
                    "1 GB"
                )
            )
            familys[1].list.add(
                Nexus(
                    "Nexus 10 (16 GB)",
                    "Samsung",
                    "Jelly Bean",
                    "17",
                    "16 GB",
                    "10\"",
                    "2 GB"
                )
            )
            familys[1].list.add(
                Nexus(
                    "Nexus 10 (32 GB)",
                    "Samsung",
                    "Jelly Bean",
                    "17",
                    "32 GB",
                    "10\"",
                    "2 GB"
                )
            )
            familys[2].list.add(Nexus("Nexus Q", "--", "Honeycomb", "13", "--", "--", "--"))
        }
    }
}