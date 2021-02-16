package com.dgaspar.translator

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.*
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // set background color
        var mainLayout : LinearLayout = findViewById(R.id.mainLayout)
        mainLayout.setBackgroundColor(Color.parseColor("#deebf7"))

        // editTexts
        var inputEditText : EditText = findViewById(R.id.inputText)
        var outputEditText : EditText = findViewById(R.id.outputText)

        /////////////////////////////////////////////////////////////////////////////////////

        // configure translator

        val options = TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.PORTUGUESE)
                .build()
        val englishPortugueseTranslator = Translation.getClient(options)

        var conditions = DownloadConditions.Builder()
                .requireWifi()
                .build()
        englishPortugueseTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener {
                    // Model downloaded successfully. Okay to start translating.
                    // (Set a flag, unhide the translation UI, etc.)
                    inputEditText.isEnabled = true
                    inputEditText.isFocusable = true
                }
                .addOnFailureListener { exception ->
                    // Model couldn’t be downloaded or other internal error.
                    // ...
                    Toast.makeText(this, "Não foi possível baixar o pacote de tradução!", Toast.LENGTH_SHORT).show()
                }

        /////////////////////////////////////////////////////////////////////////////////////

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

        /////////////////////////////////////////////////////////////////////////////////////

        // watch editText
        var outputText : String = ""

        inputEditText.setOnKeyListener(View.OnKeyListener{ v, keyCode, event ->
            // if(event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
            if(event.action == KeyEvent.ACTION_UP){

                // get input text
                outputText = inputEditText.text.toString()

                // translate


                // set outputText
                println(inputEditText.text)
                outputEditText.setText(outputText)

                true
            }
            false
        })
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