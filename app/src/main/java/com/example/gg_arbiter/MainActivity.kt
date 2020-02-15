package com.example.gg_arbiter

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
    private var whiteBitmap: Bitmap? = null
    private var blackBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        print("STARTING NOW")

        //setup View instances
        imageView = findViewById(R.id.imageView);
        predictWhiteButton = findViewById(R.id.btnPredictWhite)
        predictBlackButton = findViewById(R.id.btnPredictBlack)
        challengeButton = findViewById(R.id.btnChallenge)

        predictWhiteButton?.setOnClickListener {
            Log.e(TAG, "this is white.")
        }
        predictBlackButton?.setOnClickListener {
            Log.e(TAG, "this is black.")
        }

        ggPieceClassifier
            .initialize()
            .addOnFailureListener {e -> Log.e(TAG, "Error setting up gg piece classifier.", e)}
    }

    override fun onDestroy() {
        ggPieceClassifier.close()
        super.onDestroy()
    }

    companion object{
        private const val TAG = "MainActivity"
    }
}
