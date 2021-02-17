package com.dgaspar.translator

import android.content.Context
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apertium.utils.IOUtils
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection
import javax.net.ssl.HttpsURLConnection

class MainActivity : AppCompatActivity() {
    /*var instance: com.dgaspar.translator.MainActivity? = null

    fun reportError(ex: Exception) {
        ex.printStackTrace()
        //com.dgaspar.translator.MainActivity.longToast("Error: $ex")
        //org.apertium.android.App.longToast("The error will be reported to the developers. sorry for the inconvenience.")
        //BugSenseHandler.sendException(ex);
    }*/
    var apertiumInstallation: ApertiumInstallation? = null

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
        // https://wiki.apertium.org/wiki/Apertium_Android

        // generate temp dirs
        var packagesDir : File = File(filesDir, "packages") // where packages data are installed
        var bytecodeDir : File = File(filesDir, "bytecode") // where packages bytecode are installed. Must be private
        var bytecodeCacheDir : File = File(filesDir, "bytecodecache") // where bytecode cache is kept. Must be private
        IOUtils.cacheDir = File(cacheDir, "apertium-index-cache") // where cached transducer indexes are kept

        // check installed packages
        var ai = ApertiumInstallation(packagesDir, bytecodeDir, bytecodeCacheDir)
        ai.rescanForPackages()

        // read available packages to install
        // on original => language_pairs.txt and InstallActivity.java
        var line : String = "apertium-es-pt\thttps://svn.code.sf.net/p/apertium/svn/builds/apertium-es-pt/apertium-es-pt.jar"
        var columns = line.split("\t")
        println("CHECK IF THERE ARE AT LEAST 2 COLUMNS")

        // download package
        /*println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")
        //val connection = URL("https://svn.code.sf.net/p/apertium/svn/builds/apertium-es-pt/apertium-es-pt.jar").openConnection() as HttpsURLConnection
        var connection = URL("http://www.android.com/").openConnection() as HttpURLConnection
        println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB : " + isOnline(this).toString())
        //val data = connection.inputStream.bufferedReader().readText()

        */

        // install jar
        //var url : URL = URL("http://www.android.com/")
        var url : URL = URL("https://svn.code.sf.net/p/apertium/svn/builds/apertium-es-pt/apertium-es-pt.jar")
        var pkg : String = "apertium-es-pt"
        //var pkg : String = columns[0]
        //var url : URL = URL(columns[1])
        CoroutineScope(Dispatchers.IO).launch {
            installPackage(ai, pkg, url)
            println("ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ")
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

    /////////////////////////////////////////////////////////////////////////////////////////

    suspend fun installPackage(ai : ApertiumInstallation, pkg : String, url : URL){
        //var connection = url.openConnection() as HttpURLConnection
        var connection : URLConnection = url.openConnection() as HttpsURLConnection

        var lastModified = connection.lastModified
        var contentLength = connection.contentLength

        var tmpjarfile : File = File(cacheDir, pkg + "jar")

        println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")

        // create input and output stream
        var inStream = BufferedInputStream(connection.getInputStream())
        var fos : FileOutputStream = FileOutputStream(tmpjarfile)

        // download data
        var data = ByteArray(8192)
        var count: Int = 0
        var total : Int = 0
        while (inStream.read(data, 0, 1024).also({ count = it }) != -1){
            fos.write(data, 0, count)
            total += count
        }

        // close files
        fos.close()
        inStream.close()

        println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB")

        // install jar
        ai.installJar(tmpjarfile, pkg)

        // delete temp file
        tmpjarfile.delete()

        println("CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC")
    }

    /////////////////////////////////////////////////////////////////////////////////////////

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