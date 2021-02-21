package com.dgaspar.translator

import android.Manifest.permission.RECORD_AUDIO
import android.R.attr.data
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.apertium.Translator
import org.apertium.utils.IOUtils
import java.io.File


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()

        if(checkPermissions() != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                    this,
                    "Permissão negada para acesso ao microfone.",
                    Toast.LENGTH_LONG
            ).show()
        }

        // set background color
        var mainLayout : LinearLayout = findViewById(R.id.mainLayout)
        mainLayout.setBackgroundColor(Color.parseColor("#deebf7"))

        // editTexts
        var inputEditText : EditText = findViewById(R.id.inputText)
        var outputEditText : EditText = findViewById(R.id.outputText)
        var btnSpeak : ImageButton = findViewById(R.id.btnSpeak)

        // disable inputEditText
        inputEditText.isEnabled = false
        inputEditText.isFocusable = false
        inputEditText.isFocusableInTouchMode = false

        /////////////////////////////////////////////////////////////////////////////////////

        /**
         * APERTIUM TRANSLATOR - KOTLIN
         * https://wiki.apertium.org/wiki/Apertium_Android
        */

        /** generate temp dirs */
        var packagesDir : File = File(filesDir, "packages") // where packages data are installed
        var bytecodeDir : File = File(filesDir, "bytecode") // where packages bytecode are installed. Must be private
        var bytecodeCacheDir : File = File(filesDir, "bytecodecache") // where bytecode cache is kept. Must be private
        IOUtils.cacheDir = File(cacheDir, "apertium-index-cache") // where cached transducer indexes are kept

        /** initialize apertium */
        var apertium : Apertium = Apertium(packagesDir, bytecodeDir, bytecodeCacheDir)
        apertium.rescanForPackages()
        println(".\nINSTALLED PACKAGES:\n" + apertium.titleToMode.keys + "\n" + apertium.titleToMode.values)

        /** INSTALL es-pt PACKAGE (TEST) */
        /** COMMENT ON FINAL VERSION */
        //var url : URL = URL("https://svn.code.sf.net/p/apertium/svn/builds/apertium-es-pt/apertium-es-pt.jar")
        //var pkgAux : String = "apertium-es-pt"
        //apertium.installPackage(pkgAux, url)

        /** add installed package titles to dropDown menu */
        var dropdownMenu : Spinner = findViewById(R.id.dropdown_menu)
        setContentOnDropDownView(
                dropdownMenu,
                apertium.titleToMode.keys.toTypedArray()
        )

        /** active editText if there is any package installed (dropdown_menu != null) */
        if (dropdownMenu != null && dropdownMenu.adapter.count > 0){ // not empty

            // dropDown selected variables
            var title : String = dropdownMenu.selectedItem.toString()
            var mode = apertium.titleToMode[title].toString()
            var pkg = apertium.modeToPackage[mode].toString()

            // dropDown menu
            dropdownMenu.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    title = dropdownMenu.selectedItem.toString()
                    mode = apertium.titleToMode[title].toString()
                    pkg = apertium.modeToPackage[mode].toString()
                    println("dropdownMenu: $title $mode $pkg")
                }
            }

            // enable inputEditText
            inputEditText.isEnabled = true
            inputEditText.isFocusable = true
            inputEditText.isFocusableInTouchMode = true

            // input and output text
            var inputText : String = ""
            var outputText : String = ""

            inputEditText.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP) {

                    /** get input text */
                    inputText = inputEditText.text.toString()

                    /** translate */
                    if (inputText.isNotEmpty()) {
                        Translator.setBase(apertium.getBasedirForPackage(pkg), apertium.getClassLoaderForPackage(pkg))
                        Translator.setMode(mode)
                        outputText = Translator.translate(inputText)
                    } else {
                        outputText = ""
                    }

                    /** set outputText */
                    println(inputEditText.text)
                    outputEditText.setText(outputText)

                    true
                }
                false
            })
        }


    }

    fun openMicrophoneToSpeak(view: View){
        var inputEditText : EditText = findViewById(R.id.inputText)
        val intent = Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "pt-BR")
        try {
            startActivityForResult(intent, 1)
            inputEditText.setText("")
        } catch (a: ActivityNotFoundException) {
            val t = Toast.makeText(applicationContext,
                    "Opps! Your device doesn't support Speech to Text",
                    Toast.LENGTH_SHORT)
            t.show()
        }
    }

    /**TODO Setar texto falado no inputEditText**/
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {

            when(requestCode == RESULT_OK && null != data) {
                //var text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            }

        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        val layout = findViewById<LinearLayout>(R.id.inputOutputLayout)
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layout.setOrientation(LinearLayout.HORIZONTAL);
            Log.e("MainActivity", "Alterando orientação para horizontal");
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            layout.setOrientation(LinearLayout.VERTICAL);
            Log.e("MainActivity", "Alterando orientação para vertical");
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    fun setContentOnDropDownView(
            spinner: Spinner,
            items: Array<String>
    ){
        var adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                items
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    // open package manager activity
    fun openPackageManagerActivity(view: View){
        var intent = Intent(this, PackageManagerActivity::class.java)
        startActivity(intent)
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val n = cm.activeNetwork
            if (n != null) {
                val nc = cm.getNetworkCapabilities(n)
                //It will check for both wifi and cellular network
                return nc!!.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            }
            return false
        } else {
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnectedOrConnecting
        }
    }

    private fun checkPermissions(): Int {
        return ContextCompat.checkSelfPermission(applicationContext, RECORD_AUDIO)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
                this@MainActivity, arrayOf(RECORD_AUDIO), 1)
    }
}


