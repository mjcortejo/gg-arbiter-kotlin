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
import android.widget.TextView
import android.widget.Toast
import java.lang.Exception

class MainActivity : AppCompatActivity() {

//    https://towardsdatascience.com/spam-classification-in-android-with-tensorflow-lite-cde417e81260
//    https://github.com/tensorflow/examples/tree/master/lite/examples/digit_classifier/android/app/src/main

    private var imageView: ImageView? = null
    private var predictWhiteButton: Button? = null
    private var predictBlackButton: Button? = null
    private var challengeButton: Button? = null
    private var textOutput: TextView? = null

    private var ggPieceClassifier = GGPieceClassifier(this);
    private var currentPieceID: String? = null
    private var whiteBitmap: Bitmap? = null
    private var blackBitmap: Bitmap? = null

    val REQUEST_IMAGE_CAPTURE = 1
    private val CAMERA_REQUEST_CODE = 1

    /**
     * Returns resource id provided by the caller (in this case, either predictWhiteBtn or predictBlackBtn)
     */
    private fun getResourceID(v: View): String? {
        var id= v.resources.getResourceEntryName(v.id)
        return id
    }

    /**
     * Define intent for taking pictures
     */
    fun takePictureIntent(view: View) {
        currentPieceID = getResourceID(view)
        Log.i(TAG, currentPieceID)

        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    /**
     * Assign bitmap value from the given resource intent.
     */
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

    /**
     * Initialize instances and listeners
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //setup View instances
        imageView = findViewById(R.id.imageView);
        predictWhiteButton = findViewById(R.id.btnPredictWhite)
        predictBlackButton = findViewById(R.id.btnPredictBlack)
        challengeButton = findViewById(R.id.btnChallenge)
        textOutput = findViewById(R.id.textOutput)

        ggPieceClassifier
            .initialize()
            .addOnFailureListener {e -> Log.e(TAG, "Error setting up gg piece classifier.", e)}

        challengeButton?.setOnClickListener{
            Toast.makeText(this@MainActivity, "Checking...", Toast.LENGTH_SHORT).show()
            try
            {
                classifyDrawing()
            }
            catch (e: Exception)
            {
                println(e)
            }
        }


    }

//    override fun onDestroy() {
//        ggPieceClassifier.close()
//        super.onDestroy()
//    }

    /**
     * Predict the rank outputs and compare ranks
     */
    private fun classifyDrawing()
    {
        Log.d(TAG, "Classifying now")
        val classifyWhitePiece: Bitmap = whiteBitmap as Bitmap
        val classifyBlackPiece: Bitmap = blackBitmap as Bitmap
        var whitePieceName: String? = null
        var blackPieceName: String? = null

        if ((whiteBitmap != null) && (blackBitmap != null) && (ggPieceClassifier.isInitialized)){
            // Classifying white Piece
            var blackResult = ggPieceClassifier.classify(classifyWhitePiece)
            var whiteResult = ggPieceClassifier.classify(classifyBlackPiece)
//            Toast.makeText(this@MainActivity, whiteResult, Toast.LENGTH_SHORT).show()
            var winner = compareRanks(whiteResult, blackResult)
            when {
                winner == 0 -> {
//                    Toast.makeText(this@MainActivity, "Same ranks, remove both pieces. If flag, whoever moves into the same square occupied by the other flag wins", Toast.LENGTH_LONG).show()
                    textOutput?.text = "Same ranks, remove both pieces. If flag, whoever moves into the same square occupied by the other flag wins"
                }
                winner < 0 -> {
//                    Toast.makeText(this@MainActivity, "Black Piece wins", Toast.LENGTH_LONG).show()
                    textOutput?.text = "Black Piece wins"
                }
                winner > 0 -> {
//                    Toast.makeText(this@MainActivity, "White Piece wins", Toast.LENGTH_LONG).show()
                    textOutput?.text = "White Piece wins"
                }
            }
        }
        // https://www.tensorflow.org/lite/guide/faq#how_do_i_inspect_a_tflite_file
    }

    val rank_map = mapOf<String, Int>(
        "spy" to -2,
        "flag" to -1,
        "private" to 0,
        "sergeant" to 1,
        "lieutenant_2" to 2,
        "lieutenant_1" to 3,
        "captain" to 4,
        "major" to 5,
        "lieutenant_colonel" to 6,
        "colonel" to 7,
        "general_1" to 8,
        "general_2" to 9,
        "general_3" to 10,
        "general_4" to 11,
        "general_5" to 12
    )

    private fun compareRanks(whitePiece: String?, blackPiece: String?): Int
    {
        var result = 0
        var compareArray = arrayOf(rank_map[whitePiece], rank_map[blackPiece])
        var initialResult = compareArray[0]!! - compareArray[1]!!

        if (initialResult == 0) // Same ranks remove both pieces, if flag the one who moved to the same space as the other wins
        {
            return result //return result which is already 0
        }
        else if (compareArray.contains(-2)) // one Spy situation
        {
            var index = compareArray.indexOf(-2)
            if (compareArray.contains(0)) //if there is a private
            {
                var privateIndex = compareArray.indexOf(0)
                result = if (privateIndex == 0)
                    1 //white wins
                else
                    -1 //black wins
                return result
            }
            else //if it's another ranking
            {
                /**
                 * variable positions are switched because we are subtracting a negative number
                 * if <=-3 then black
                 * if >=3 then white
                 */
                return compareArray[1]!! - compareArray[0]!!
            }
        }
        else if (compareArray.contains(-1)) // one flag situation
        {
            var index = compareArray.indexOf(-1) // who has the flag?
            result = if (index == 0)
                -1 // Black Wins
            else
                1 // White Wins
            return result
        }
        else
            return initialResult //initial result will be the basis
    }

    companion object{
        private const val TAG = "MainActivity"
    }
}
