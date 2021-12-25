package com.ndsoftwares.tableviewsample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ndsoftwares.tableviewsample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var bv: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bv = ActivityMainBinding.inflate(layoutInflater)
        setContentView(bv.root)

        bv.button.setOnClickListener {
            startActivity(Intent(this, SimpleTable::class.java))
        }

        bv.button2.setOnClickListener {
            startActivity(Intent(this, FamilyTable::class.java))
        }

        bv.button3.setOnClickListener {
            startActivity(Intent(this, StyleTable::class.java))
        }

    }
}