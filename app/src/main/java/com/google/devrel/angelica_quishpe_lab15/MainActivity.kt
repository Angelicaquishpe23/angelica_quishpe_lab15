package com.google.devrel.angelica_quishpe_lab15

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.mlkit.common.model.LocalModel
import com.google.devrel.angelica_quishpe_lab15.R.id.imageToLabel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Load the model
        val localModel = LocalModel.Builder()
            .setAssetFilePath("model.tflite")
            .build()

        // Display the image
        val img: ImageView = findViewById(imageToLabel)
        val fileName = "Osteoporosis 636.jpg"
        val bitmap: Bitmap? = assetsToBitmap(fileName)
        bitmap?.apply {
            img.setImageBitmap(this)
        }

        // Labeling setup and output display
        val txtOutput: TextView = findViewById(R.id.txtOutput)
        val btn: Button = findViewById(R.id.btnTest)
        btn.setOnClickListener {
            val scaledBitmap = bitmap?.let { normalizeBitmap(it) }  // Normalize the bitmap
            val image = InputImage.fromBitmap(scaledBitmap!!, 0)

            // Set up the image labeling options
            val options = CustomImageLabelerOptions.Builder(localModel)
                .setConfidenceThreshold(0.36f)
                .setMaxResultCount(3)
                .build()
            val labeler = ImageLabeling.getClient(options)

            // Process the image
            var outputText = ""
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    for (label in labels) {
                        val text = label.text
                        val confidence = label.confidence
                        outputText += "$text : $confidence\n"
                    }
                    txtOutput.text = outputText
                }
                .addOnFailureListener { _ ->
                    txtOutput.text = "Labeling failed"
                }
        }
    }

    // Function to normalize the Bitmap
    private fun normalizeBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val normalizedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = bitmap.getPixel(x, y)

                // Get RGB components and scale to 0-1
                val r = ((pixel shr 16 and 0xff) / 255.0f)
                val g = ((pixel shr 8 and 0xff) / 255.0f)
                val b = ((pixel and 0xff) / 255.0f)

                // Combine normalized values back to a pixel
                val normalizedPixel = (0xff shl 24) or ((r * 255).toInt() shl 16) or ((g * 255).toInt() shl 8) or (b * 255).toInt()
                normalizedBitmap.setPixel(x, y, normalizedPixel)
            }
        }
        return normalizedBitmap
    }

    // Extension function to get bitmap from assets
    private fun Context.assetsToBitmap(fileName: String): Bitmap? {
        return try {
            assets.open(fileName).use {
                BitmapFactory.decodeStream(it)
            }
        } catch (e: IOException) { null }
    }
}