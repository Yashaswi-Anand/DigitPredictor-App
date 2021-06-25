package com.yashanand.digitrecognizer

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_main.*
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {

    // declare the instance of module
    lateinit var module: Module

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // read module of mnist file and set button for detecting the digit ---
        val mnistPath = ReadMnistFile()
        module = Module.load(mnistPath.loadAssetPath(this,"mnist_ann_cpy.pt"))
        Log.d("module", module.toString())
        val btn = findViewById<Button>(R.id.predict)
        btn.setOnClickListener {
            predicte()
        }

        bottom_navigation.setOnNavigationItemSelectedListener{ item ->
            when(item.itemId){
                R.id.home ->{

                }
                R.id.challenge ->{
                    BottomSheetView()
                }
            }
            return@setOnNavigationItemSelectedListener true
        }
    }

    private fun BottomSheetView() {
        val bottomSheetDialog =  BottomSheetDialog(this,R.style.BottomSheetDialogTheme)

        val bottomSheetView = LayoutInflater.from(this).inflate(
                R.layout.bottom_sheet_item_view,
                null
        )
        bottomSheetView.findViewById<TextView>(R.id.from_camera).setOnClickListener {
            Toast.makeText(this,"Clicked for camera...",Toast.LENGTH_SHORT).show()
        }
        bottomSheetView.findViewById<TextView>(R.id.from_gallery).setOnClickListener {
            Toast.makeText(this,"Clicked for gallery...",Toast.LENGTH_SHORT).show()
        }
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }


    private  fun predicte() {

        val resizedBitmap: Bitmap = Bitmap.createScaledBitmap(
            CanvasView.extraBitmap,
            28,
            28,
            true
        )

        //img.setImageBitmap(resizedBitmap)
        Log.d("ResizedBitmap",resizedBitmap.toString())

        val byteBuffer: ByteBuffer = Tensor.allocateByteBuffer(784*4)
        val pixels = IntArray(28 * 28)
        resizedBitmap.getPixels(pixels, 0, resizedBitmap.width, 0, 0, resizedBitmap.width, resizedBitmap.height)
        //Log.d("ResizedBitmap",resizedBitmap.toString())

        var i = 0
        val array = FloatArray(28*28)

        for (pixelValue in pixels) {

            val r = (pixelValue shr 16 and 0xFF)
            val g = (pixelValue shr 8 and 0xFF)
            val b = (pixelValue and 0xFF)

            // Convert RGB to grayscale and normalize pixel value to [0..1]
            val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
            byteBuffer.putFloat(normalizedPixelValue)
            array[i] = normalizedPixelValue
            i += 1

        }

        val inputTensor = Tensor.fromBlob(array, longArrayOf(1, 28, 28))

        val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
        Log.d("listIndex2:" , outputTensor.toString())
        val scores = outputTensor.dataAsFloatArray
        Log.d("listIndex1:" , scores.toString())

        var maxScore: Float = -9999F
        var maxScoreIdx = -1


        for (i in scores.indices) {
            Log.d("listIndex:" , scores.indices.toString())
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }

        val predictedValue: TextView = findViewById(R.id.predicted_result)
        predictedValue.text = "Result: ${maxScoreIdx.toString()}"

        CanvasView.extraCanvas.drawColor(ResourcesCompat.getColor(resources, R.color.black, null))
        //Toast.makeText(applicationContext,maxScoreIdx.toString(), Toast.LENGTH_SHORT).show()
    }
}