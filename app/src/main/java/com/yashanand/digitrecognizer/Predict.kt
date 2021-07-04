package com.yashanand.digitrecognizer

import android.graphics.Bitmap
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.nio.ByteBuffer

class Predict {

    fun predict(module: Module, resizedBitmap: Bitmap) : ArrayList<Int>{


        //img.setImageBitmap(resizedBitmap)
        Log.d("ResizedBitmap",resizedBitmap.toString())

        val byteBuffer: ByteBuffer = Tensor.allocateByteBuffer(784*4)
        val pixels = IntArray(28 * 28)
        resizedBitmap.getPixels(pixels, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
        //Log.d("ResizedBitmap",resizedBitmap.toString())

        var i = 0
        val pixelArray = FloatArray(28*28)

        for (pixelValue in pixels) {

            /* val r = (pixelValue shr 16 and 0xFF)
             val g = (pixelValue shr 8 and 0xFF)
             val b = (pixelValue and 0xFF)


             // Convert RGB to grayscale and normalize pixel value to [0..1]
             val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
             byteBuffer.putFloat(normalizedPixelValue)
             array[i] = normalizedPixelValue
             i += 1*/

            // Convert RGB to grayscale and normalize pixel value to [0..1] in another way
            val n = (255 - ((pixelValue shr 16 and 0xFF) * 0.299f + (pixelValue shr 8 and 0xFF) * 0.587f + (pixelValue and 0xFF) * 0.114f)) / 255.0f
            byteBuffer.putFloat(n)
            pixelArray[i] = n
            i += 1


        }

        val inputOfTensor = Tensor.fromBlob(pixelArray, longArrayOf(1,1,28, 28))

        val outputFromTensor = module.forward(IValue.from(inputOfTensor)).toTensor()
        Log.d("listIndex2:" , outputFromTensor.toString())
        val scores = outputFromTensor.dataAsFloatArray
        Log.d("listIndex1:" , scores.toString())

        var maxValue: Float = -9999F
        var maxValueIdx = -1

        for (i in scores.indices) {
            Log.d("listIndex:" , scores.indices.toString())
            if (scores[i] > maxValue) {
                maxValue = scores[i]
                maxValueIdx = i
                Log.d("maxVal", maxValueIdx.toString())
            }
        }
        val probability = (scores[maxValueIdx]*100).toInt()
        Log.d("ResultP", probability.toString())
        val arraylist: ArrayList<Int> = ArrayList<Int>()
        arraylist.add(maxValueIdx)
        arraylist.add(probability)
        Log.d("Result", arraylist.toString())
        return  arraylist

       /* val predictedValue: TextView = findViewById(R.id.predicted_result)
        predictedValue.text = "Result: ${maxScoreIdx.toString()}"
        CanvasView.extraCanvas.drawColor(ResourcesCompat.getColor(resources, R.color.black, null))*/
        //Toast.makeText(applicationContext,maxScoreIdx.toString(), Toast.LENGTH_SHORT).show()
    }
}