package com.ndsoftwares.tableviewsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ndsoftwares.tableviewsample.databinding.ActivitySimpleTableBinding
import com.ndsoftwares.tableviewsample.adapters.MatrixTableAdapter

class SimpleTable : AppCompatActivity() {
    private lateinit var bv: ActivitySimpleTableBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bv = ActivitySimpleTableBinding.inflate(layoutInflater)
        setContentView(bv.root)

        val matrixTableAdapter = MatrixTableAdapter(
            this, arrayOf(
                arrayOf(
                    "Header 1",
                    "Header 2"
                    ,
                    "Header 3",
                    "Header 4",
                    "Header 5",
                    "Header 6"
                ), arrayOf(
                    "Lorem",
                    "sed"
                    ,
                    "do",
                    "eiusmod",
                    "tempor",
                    "incididunt"
                )
                , arrayOf(
                    "ipsum",
                    "irure",
                    "occaecat",
                    "enim",
                    "laborum",
                    "reprehenderit"
                ), arrayOf(
                    "dolor",
                    "fugiat",
                    "nulla",
                    "reprehenderit",
                    "laborum",
                    "consequat"
                ), arrayOf(
                    "sit",
                    "consequat",
                    "laborum",
                    "fugiat",
                    "eiusmod",
                    "enim"
                ), arrayOf(
                    "amet",
                    "nulla",
                    "Excepteur",
                    "voluptate",
                    "occaecat",
                    "et"
                ), arrayOf(
                    "consectetur",
                    "occaecat",
                    "fugiat",
                    "dolore",
                    "consequat",
                    "eiusmod"
                ), arrayOf(
                    "adipisicing",
                    "fugiat",
                    "Excepteur",
                    "occaecat",
                    "fugiat",
                    "laborum"
                ), arrayOf(
                    "elit",
                    "voluptate",
                    "reprehenderit",
                    "Excepteur",
                    "fugiat",
                    "nulla"
                )
            )
        )
        bv.table.setAdapter(matrixTableAdapter)
    }
}