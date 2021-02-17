package com.dgaspar.translator

import android.util.Log
import dalvik.system.DexClassLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apertium.Translator
import java.io.File
import java.util.*
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
    var titleToMode = HashMap<String, String>()

    init {
        packagesDir.mkdirs()
        bytecodeDir.mkdirs()
        bytecodeCacheDir.mkdirs()
    }

    /*******************************************************************************************/

    fun rescanForPackages(){
        titleToMode.clear()

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
                    }
                } catch (e : Throwable){
                    e.printStackTrace()
                    Log.e("", baseDir, e)
                }
            }
        }
    }

    /*******************************************************************************************/

    fun installJar(){
        CoroutineScope(Dispatchers.IO).launch {
            installJarAsync()
            println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP2")
        }
    }

    suspend fun installJarAsync(){
        println("PPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPPP1")
    }

    /*******************************************************************************************/

    /** auxiliar functions */

    open fun getClassLoaderForPackage(pkg: String): DexClassLoader? {
        if (!bytecodeCacheDir.exists()) {
            bytecodeCacheDir.mkdirs()
        }
        return DexClassLoader(
                "$bytecodeDir/$pkg.jar",
                bytecodeCacheDir.getAbsolutePath(),
                null, this.javaClass.classLoader)
    }

}