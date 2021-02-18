package com.dgaspar.translator

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import android.widget.Toast

class PackageManagerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_package_manager)

        var mainLayout : LinearLayout = findViewById(R.id.mainLayout)

        for (i in 1..4){
            createPackageItem(
                mainLayout,
                i,
                "TESTE-$i"
            )

            // create horizontal line
            createHorizontalLine(
                mainLayout,
                color = "#000000"
            )
        }

    }

    /////////////////////////////////////////////////////////////////////////////////////////

    fun createPackageItem(
        layout : LinearLayout,
        id : Int,
        title : String
    ){
        // item layout params
        var layoutParams : LayoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(0, 50, 0, 50)

        // item layout
        var itemLayout : LinearLayout = LinearLayout(this)
        itemLayout.layoutParams = layoutParams
        itemLayout.orientation = LinearLayout.HORIZONTAL
        itemLayout.gravity = Gravity.CENTER_VERTICAL

        // textView
        var nameTextView : TextView =  createTextView(
            itemLayout,
            title,
            15f
        )

        // button
        var button : Button = createButton(
            itemLayout,
            id,
            "Instalar",
            15f,
            "#9ecae1"
        )

        layout.addView(itemLayout)
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    fun createTextView (
        layout : LinearLayout,
        //id : Int,
        text : String,
        textSize : Float = 20f,
        textColor : String = "#000000"
    ) : TextView {
        var textView : TextView = TextView(this)
        textView.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        textView.textSize = textSize
        //textView.id = id
        textView.text = text
        textView.setTextColor(Color.parseColor(textColor))
        layout.addView(textView)

        return textView
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    fun createButton (
        layout : LinearLayout,
        id : Int,
        text : String,
        textSize : Float = 20f,
        color : String = "#c0c0c0"
    ) : Button {
        var button : Button = Button(this)
        button.layoutParams = LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )
        button.textSize = textSize
        button.text = text
        button.setBackgroundColor(Color.parseColor(color))
        button.setPadding(10, 10, 10, 10)
        button.setOnClickListener {
            Toast.makeText(this, "id: $id", Toast.LENGTH_LONG).show()

            buttonListener(button)
        }
        layout.addView(button)

        return button
    }

    fun buttonListener(button : Button){
        // import package
        button.text = "Remover"
        button.setBackgroundColor(Color.parseColor("#c0c0c0"))

        // remove package
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    fun createHorizontalLine (
        layout : LinearLayout,
        height : Int = 2,
        color : String = "#c0c0c0"
    ){
        var horLine : View = View(this)
        horLine.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            height
        )
        horLine.setBackgroundColor(Color.parseColor(color))

        // add horizontal line
        layout.addView(horLine)
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    fun installButton(view : View){
        println("APPPPPPLLLLLLYYYYYYYYYYYYYYYYYYYYYYYYYY")
    }
}