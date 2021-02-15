package com.dgaspar.translator

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set background color
        var mainLayout : LinearLayout = findViewById(R.id.mainLayout)
        mainLayout.setBackgroundColor(Color.parseColor("#deebf7"))

        // input language
        setContentOnDropDownView(
            R.id.inputLanguage,
            arrayOf("Inglês", "Português")
        )
        //inputLanguage.onItemSelectedListener(this)

        // input language
        setContentOnDropDownView(
            R.id.outputLanguage,
            arrayOf("Português", "Inglês")
        )
    }

    fun setContentOnDropDownView(
        viewId : Int,
        items : Array<String>
    ){
        var spinner : Spinner = findViewById(viewId)

        var adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

    }
}