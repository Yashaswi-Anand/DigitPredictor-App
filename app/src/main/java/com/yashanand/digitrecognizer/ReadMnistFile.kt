package com.yashanand.digitrecognizer

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class ReadMnistFile {

    // return absolute path of mnist model( like mymodel.pt) in string format
    fun loadAssetPath(context: Context, modelName: String): String? {
        val file = File(context.filesDir, modelName)
        if (file.exists()&& file.length()>0){
            Log.d("FilePath",file.absolutePath)
            return file.absolutePath
        }
        context.assets.open(modelName).use{ inputStream ->
            FileOutputStream(file).use { OutputStream ->
                val buffer =ByteArray(4*1024)
                var read:Int
                while (inputStream.read(buffer).also { read = it } != -1){
                    OutputStream.write(buffer,0,read)
                }
                OutputStream.flush()
            }
        }
        Log.d("FilePath1",file.absolutePath)
        return file.absolutePath
    }
}