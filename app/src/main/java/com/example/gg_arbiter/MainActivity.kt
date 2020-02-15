package com.example.gg_arbiter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

//    https://towardsdatascience.com/spam-classification-in-android-with-tensorflow-lite-cde417e81260
//    https://github.com/tensorflow/examples/tree/master/lite/examples/digit_classifier/android/app/src/main

    private var imageView: ImageView? = null
    private var predictWhiteButton: Button? = null
    private var predictBlackButton: Button? = null
    private var challengeButton: Button? = null
    private var ggPieceClassifier = GGPieceClassifier(this);
    private var currentPieceID: String? = null
    private var whiteBitmap: Bitmap? = null
    private var blackBitmap: Bitmap? = null

    val REQUEST_IMAGE_CAPTURE = 1
    private val CAMERA_REQUEST_CODE = 1

    private fun getResourceID(v: View): String? {
        var id= v.resources.getResourceEntryName(v.id)
        return id
    }

    fun takePictureIntent(view: View) {
        currentPieceID = getResourceID(view)
        Log.i(TAG, currentPieceID)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {

                    val extras = data?.getExtras()
                    val imageBitmap = extras?.get("data") as Bitmap
                    imageView?.setImageBitmap(imageBitmap)

                    if (currentPieceID == "btnPredictWhite") whiteBitmap = imageBitmap
                    else if (currentPieceID == "btnPredictBlack") blackBitmap = imageBitmap

                    Log.d(TAG, currentPieceID)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setup View instances
        imageView = findViewById(R.id.imageView);
        predictWhiteButton = findViewById(R.id.btnPredictWhite)
        predictBlackButton = findViewById(R.id.btnPredictBlack)
        challengeButton = findViewById(R.id.btnChallenge)

//        predictWhiteButton?.setOnClickListener {
//            currentPieceID = "white"
//        }
//        predictBlackButton?.setOnClickListener {
//            currentPieceID = "black"
//        }

        challengeButton?.setOnClickListener{
            classifyDrawing()
        }

        ggPieceClassifier
            .initialize()
            .addOnFailureListener {e -> Log.e(TAG, "Error setting up gg piece classifier.", e)}
    }

    override fun onDestroy() {
        ggPieceClassifier.close()
        super.onDestroy()
    }

    private fun classifyDrawing()
    {
        Log.d(TAG, "Classifying now")
//        val bitmap = imageView?.getBitmap()
//
//        when(color){
//            "white" -> {
//                currentPieceID = "white"
//            }
//            "black" -> Log.d(TAG, "This is really black")
//        }
    }

    companion object{
        private const val TAG = "MainActivity"
    }
}
