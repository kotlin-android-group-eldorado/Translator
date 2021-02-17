package com.dgaspar.translator

import android.util.Log
import dalvik.system.DexClassLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apertium.Translator
import org.apertium.utils.IOUtils.cacheDir
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.net.URLConnection
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList

/**
 * @author Daniel Gaspar Goncalves - 17-02-2021
*/
class Apertium (packagesDir : File, bytecodeDir : File, bytecodeCacheDir : File) {

    /** This is where the packages are installed */
    private var packagesDir : File = packagesDir

    /** This is where bytecode will be put */
    private var bytecodeDir : File = bytecodeDir

    /** This is where optimized bytecode will be put */
    private var bytecodeCacheDir : File = bytecodeCacheDir

    /** <key, value> = <"Spanish → Portuguese (BR)", "es-pt_BR"> */
    public var titleToMode = HashMap<String, String>()

    /** <key, value> = <"es-pt_BR", "apertium-es-pt_BR"> */
    public var modeToPackage = HashMap<String, String>()

    /** REMOVE LATER*/
    private var ai : ApertiumInstallation = ApertiumInstallation(packagesDir, bytecodeDir, bytecodeCacheDir)

    init {
        packagesDir.mkdirs()
        bytecodeDir.mkdirs()
        bytecodeCacheDir.mkdirs()
    }

    /*******************************************************************************************/

    public fun rescanForPackages(){
        titleToMode.clear()
        modeToPackage.clear()

        /** list packages */
        var installedPackages : ArrayList<String> = packagesDir.list().toCollection(ArrayList())

        /** cycle through installed packages list */
        for (pkg in installedPackages){

            /** filename filter */
            if(pkg.matches("apertium-[a-z][a-z][a-z]?-[a-z][a-z][a-z]?".toRegex())) {

                /** check if is a valid package */
                var baseDir : String = "$packagesDir/$pkg"
                try {
                    Translator.setBase(baseDir, getClassLoaderForPackage(pkg))

                    for (mode in Translator.getAvailableModes()){
                        var title : String = LanguageTitles.getTitle(mode)
                        titleToMode[title] = mode
                        modeToPackage[mode] = pkg
                    }
                } catch (e : Throwable){
                    e.printStackTrace()
                    Log.e("", baseDir, e)
                }
            }
        }
    }

    /*******************************************************************************************/

    public fun installPackage(pkg : String, url : URL){
        CoroutineScope(Dispatchers.IO).launch {
            installPackageAsync(pkg, url)
            println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP3")
        }
    }

    private suspend fun installPackageAsync(pkg : String, url : URL){
        println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP1")

        var connection : URLConnection = url.openConnection() as HttpsURLConnection

        //var lastModified = connection.lastModified
        //var contentLength = connection.contentLength

        var tmpjarfile : File = File(cacheDir, "$pkg.jar")

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

        // install jar
        ai.installJar(tmpjarfile, pkg)

        // delete temp file
        tmpjarfile.delete()

        println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP2")
    }

    /*******************************************************************************************/

    /** auxiliar functions */

    public fun getClassLoaderForPackage(pkg: String): DexClassLoader? {
        if (!bytecodeCacheDir.exists()) {
            bytecodeCacheDir.mkdirs()
        }
        return DexClassLoader(
                "$bytecodeDir/$pkg.jar",
                bytecodeCacheDir.getAbsolutePath(),
                null, this.javaClass.classLoader)
    }

    public fun getBasedirForPackage(pkg: String): String? {
        return "$packagesDir/$pkg"
    }
}