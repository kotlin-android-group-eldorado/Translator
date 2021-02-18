package com.dgaspar.translator

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apertium.Translator
import org.apertium.utils.IOUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HttpsURLConnection

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
        var url : URL = URL("https://svn.code.sf.net/p/apertium/svn/builds/apertium-es-pt/apertium-es-pt.jar")
        var pkgAux : String = "apertium-es-pt"
        apertium.installPackage(pkgAux, url)

        /** add installed package titles to dropDown menu */
        var dropdownMenu : Spinner = findViewById(R.id.dropdown_menu)
        setContentOnDropDownView(
                dropdownMenu,
                apertium.titleToMode.keys.toTypedArray()
        )

        /** active editText if there is any package installed (dropdown_menu != null) */
        if (dropdownMenu != null){ // not empty

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

            inputEditText.setOnKeyListener(View.OnKeyListener{ v, keyCode, event ->
                if(event.action == KeyEvent.ACTION_UP){

                    /** get input text */
                    inputText = inputEditText.text.toString()

                    /** translate */
                    if (inputText.isNotEmpty()){
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
/*
        // PUT BELLOW CODE ON "PackageManagerActivity.kt"
        // read available packages to install
        // on original => language_pairs.txt and InstallActivity.java
        var line : String = "apertium-es-pt\thttps://svn.code.sf.net/p/apertium/svn/builds/apertium-es-pt/apertium-es-pt.jar"
        var columns = line.split("\t")
        println("NEED TO CHECK IF THERE ARE AT LEAST 2 COLUMNS")

        // install jar
        //var url : URL = URL("http://www.android.com/")
        var url : URL = URL("https://svn.code.sf.net/p/apertium/svn/builds/apertium-es-pt/apertium-es-pt.jar")
        var pkg : String = "apertium-es-pt"
        //var pkg : String = columns[0]
        //var url : URL = URL(columns[1])
*/
    }

    /////////////////////////////////////////////////////////////////////////////////////////

    fun setContentOnDropDownView(
        spinner : Spinner,
        items : Array<String>
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
    fun openPackageManagerActivity(view : View){
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
}