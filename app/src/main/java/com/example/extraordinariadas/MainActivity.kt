package com.example.extraordinariadas

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.GridView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var photoGridView: GridView
    private lateinit var photoAdapter: PhotoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        photoGridView = findViewById(R.id.photoGridView)
        photoAdapter = PhotoAdapter(this,ArrayList())
        photoGridView.adapter = photoAdapter

        checkNotificationPermission()

        // Capture image
        findViewById<Button>(R.id.captureButton).setOnClickListener {
            dispatchTakePictureIntent()
        }

        // View photos
        findViewById<Button>(R.id.viewPhotosButton).setOnClickListener {
            fetchPhotos()
        }

        // Initialize FCM
        FirebaseMessaging.getInstance().subscribeToTopic("news")
            .addOnCompleteListener { task ->
                val msg = if (task.isSuccessful) "Subscribed" else "Subscription failed"
                Log.d(TAG, msg)
            }
    }

    private val takePictureResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as Bitmap
                uploadImageToServer(imageBitmap)
            }
        }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureResult.launch(takePictureIntent)
    }

    private fun uploadImageToServer(bitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val byteArray = baos.toByteArray()
        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)

        // Obtener el token de registro del dispositivo
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val url = URL("http://34.76.16.111/upload3.php")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "POST"
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/json")

                    val json = JSONObject()
                    Log.d(TAG, "Token: $token")
                    json.put("image", encodedImage)
                    json.put("token", token) // Agregar el token al JSON

                    val writer = OutputStreamWriter(connection.outputStream)
                    writer.write(json.toString())
                    writer.flush()
                    writer.close()

                    val responseCode = connection.responseCode
                    Log.d(TAG, "Codigo: $responseCode")
                    if (responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().readText()
                        withContext(Dispatchers.Main) {
                            // Manejar la respuesta aquí en el hilo principal
                            Log.d(TAG, "Image uploaded: $response")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e(TAG, "Failed to upload image")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        e.printStackTrace()
                    }
                }
            }
        }.addOnFailureListener { exception ->
            Log.e(TAG, "Error obtaining FCM token", exception)
        }
    }


    private fun fetchPhotos() {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://34.76.16.111/get_images.php")
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
                val photoUrls = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    val imageUrl = jsonArray.getString(i)
                    photoUrls.add(imageUrl)
                }

                // Update GridView with fetched photo URLs
                runOnUiThread {
                    photoAdapter.updateData(photoUrls)
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error fetching photos: ${e.message}")
            }
        }
    }

    private fun checkNotificationPermission() {
        if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            // Las notificaciones no están habilitadas, solicitar permiso
            val intent = Intent().apply {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, applicationInfo.uid)
            }
            startActivity(intent)
        }
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}

