package com.yashanand.digitrecognizer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import org.pytorch.Module
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // declare the instance of module
    lateinit var module: Module
    private val predict = Predict()
    // Gallery code
    private val GALLERY_REQUEST_CODE = 1234
    var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // read module of mnist file and set button for detecting the digit ---
        val mnistPath = ReadMnistFile()
        module = Module.load(mnistPath.loadAssetPath(this,"mnist_ann_cpy.pt"))
        Log.d("module", module.toString())
        val predictBtn = findViewById<Button>(R.id.predict)
        predictBtn.setOnClickListener {
            //predicte()
            val resizedBitmap: Bitmap = Bitmap.createScaledBitmap(
                    CanvasView.extraBitmap,
                    28,
                    28,
                    true
            )
            val maxValueIdx = predict.predict(module,resizedBitmap)
            val predictedValue: TextView = findViewById(R.id.predicted_result)
            if(maxValueIdx[1]<= 0) maxValueIdx[1] = maxValueIdx[1]*(-1)
            predictedValue.text = "Result: ${maxValueIdx[0].toString()}" /*and Prob: ${maxScoreIdx[1].toString()}"*/
        }
        clear.setOnClickListener {
            CanvasView.extraCanvas.drawColor(ResourcesCompat.getColor(resources, R.color.white, null))
            predicted_result.text = ""
        }

        otherImg.setOnClickListener {
            pickFromGallery()
            result_predict_gallery.text = ""
        }

        bottom_navigation.setOnNavigationItemSelectedListener{ item ->
            when(item.itemId){
                R.id.home ->{
                    DrawLayout.visibility = View.VISIBLE
                    GalleryLayout.visibility = View.GONE
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
        /*bottomSheetView.findViewById<TextView>(R.id.from_camera).setOnClickListener {
            Toast.makeText(this,"Clicked for camera...",Toast.LENGTH_SHORT).show()

        }*/
        bottomSheetView.findViewById<TextView>(R.id.from_gallery).setOnClickListener {
           // Toast.makeText(this,"Clicked for gallery...",Toast.LENGTH_SHORT).show()
            pickFromGallery()
            DrawLayout.visibility = View.GONE
            GalleryLayout.visibility = View.VISIBLE
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    /**
     * Pick image from gallery
     *
     */

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            GALLERY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }
                } else {
                    Log.e("errorSelection", "Image selection error: Couldn't select that image from memory.")
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    setImage(result.uri)
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.e("errorCrop", "Crop error: ${result.error}")
                }
            }
        }
    }

    private fun setImage(uri: Uri) {
        /*Glide.with(this).load(uri).into(image)*/
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            image_of_gallery.setImageBitmap(bitmap)

            predictFromGallery.setOnClickListener {

                val resizedBitmap: Bitmap = Bitmap.createScaledBitmap(
                        bitmap!!,
                        28,
                        28,
                        true
                )
                val maxValueIdx = predict.predict(module, resizedBitmap)
                result_predict_gallery.text = "Result: ${maxValueIdx[0].toString()}"
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun launchImageCrop(uri: Uri){
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(28,28)
                .setCropShape(CropImageView.CropShape.RECTANGLE) // default is rectangle
                .start(this)
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }
}