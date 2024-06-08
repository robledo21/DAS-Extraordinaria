// ViewPhotosActivity.kt
package com.example.extraordinariadas

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ViewPhotosActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var nextButton: Button
    private lateinit var prevButton: Button

    private var photoUrls: MutableList<String> = mutableListOf()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_photos)

        imageView = findViewById(R.id.imageView)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)

        fetchPhotos()

        nextButton.setOnClickListener {
            if (currentIndex < photoUrls.size - 1) {
                currentIndex++
                displayPhoto()
            }
        }

        prevButton.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                displayPhoto()
            }
        }
    }

    private fun fetchPhotos() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://35.195.17.43/get_images2.php") // Cambia esto a tu URL de PHP
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val inputStreamReader = InputStreamReader(connection.inputStream)
                val bufferedReader = BufferedReader(inputStreamReader)
                val response = StringBuilder()
                var inputLine: String?

                while (bufferedReader.readLine().also { inputLine = it } != null) {
                    response.append(inputLine)
                }
                bufferedReader.close()

                // Parse the JSON response
                val jsonArray = JSONArray(response.toString())
                for (i in 0 until jsonArray.length()) {
                    val imageUrl = jsonArray.getString(i)
                    photoUrls.add(imageUrl)
                }

                withContext(Dispatchers.Main) {
                    if (photoUrls.isNotEmpty()) {
                        displayPhoto()
                    }
                }

            } catch (e: Exception) {
                Log.e("ViewPhotosActivity", "Error fetching photos: ${e.message}")
            }
        }
    }

    private fun displayPhoto() {
        Glide.with(this)
            .load(photoUrls[currentIndex])
            .into(imageView)
    }
}
