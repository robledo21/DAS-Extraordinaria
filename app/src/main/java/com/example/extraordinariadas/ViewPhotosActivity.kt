package com.example.extraordinariadas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
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

    private lateinit var auth: FirebaseAuth

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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // Manejar el cierre de sesión
                handleLogout()
                true
            }
            R.id.action_settings -> {
                // Abrir diálogo de configuración
                showSettingsDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleLogout() {
        auth = FirebaseAuth.getInstance()
        auth.signOut()  //Hacer logout

        //volver a la pantalla de login
        val intent = Intent(this, RegistroLoginActivity::class.java)
        startActivity(intent)
    }

    private fun showSettingsDialog() {
        val options = arrayOf("Cambiar idioma", "Cambiar tema")
        val builder = AlertDialog.Builder(this, R.style.Theme_Extraordinariadas_Dialog)
        builder.setTitle("Ajustes")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> showLanguageDialog()
                1 -> showThemeDialog()
            }
        }
        builder.show()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Español", "Inglés")
        val builder = AlertDialog.Builder(this, R.style.Theme_Extraordinariadas_Dialog)
        builder.setTitle("Seleccionar idioma")
        builder.setItems(languages) { dialog, which ->
            // Lógica para cambiar idioma
        }
        builder.show()
    }

    private fun showThemeDialog() {
        val themes = arrayOf("Claro", "Oscuro")
        val builder = AlertDialog.Builder(this, R.style.Theme_Extraordinariadas_Dialog)
        builder.setTitle("Seleccionar tema")
        builder.setItems(themes) { dialog, which ->
            when (which) {
                0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
        builder.show()
    }
    private fun fetchPhotos() {
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
