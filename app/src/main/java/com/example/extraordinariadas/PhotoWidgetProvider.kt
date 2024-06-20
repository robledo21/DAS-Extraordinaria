package com.example.extraordinariadas

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class PhotoWidgetProvider : AppWidgetProvider() {

    private var photoUrls: MutableList<String> = mutableListOf()
    private var currentIndex = 0

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        fetchPhotos(context, appWidgetManager, appWidgetIds)
    }

    private fun fetchPhotos(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val url = URL("http://34.22.179.17/get_photos.php")
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


                val jsonArray = JSONArray(response.toString())
                for (i in 0 until jsonArray.length()) {
                    val imageUrl = jsonArray.getString(i)
                    photoUrls.add(imageUrl)
                }

                withContext(Dispatchers.Main) {
                    if (photoUrls.isNotEmpty()) {
                        updateWidget(context, appWidgetManager, appWidgetIds)
                    } else {
                        Toast.makeText(context, "No photos available", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("PhotoWidgetProvider", "Error fetching photos: ${e.message}")
                    Toast.makeText(context, "Error fetching photos: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            // Mostrar foto usando Glide
            val appWidgetTarget = AppWidgetTarget(context, R.id.imageView, views, appWidgetId)
            Glide.with(context.applicationContext)
                .asBitmap()
                .load(photoUrls[currentIndex])
                .into(appWidgetTarget)

            // Setup button click handlers
            val nextIntent = Intent(context, PhotoWidgetProvider::class.java).apply {
                action = "com.example.extraordinariadas.NEXT_PHOTO"
            }
            val prevIntent = Intent(context, PhotoWidgetProvider::class.java).apply {
                action = "com.example.extraordinariadas.PREV_PHOTO"
            }

            val nextPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val prevPendingIntent = PendingIntent.getBroadcast(
                context,
                0,
                prevIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            views.setOnClickPendingIntent(R.id.nextButton, nextPendingIntent)
            views.setOnClickPendingIntent(R.id.prevButton, prevPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == "com.example.extraordinariadas.NEXT_PHOTO") {
            currentIndex = (currentIndex + 1) % photoUrls.size
        } else if (intent.action == "com.example.extraordinariadas.PREV_PHOTO") {
            currentIndex = if (currentIndex - 1 < 0) photoUrls.size - 1 else currentIndex - 1
        }

        // Actualizar widgets
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(ComponentName(context, PhotoWidgetProvider::class.java))
        updateWidget(context, appWidgetManager, appWidgetIds)
    }
}
