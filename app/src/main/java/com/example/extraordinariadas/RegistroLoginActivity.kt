package com.example.extraordinariadas

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegistroLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registro_login_activity)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.btnRegistro).setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
            registerUser(email, password)
        }

        findViewById<Button>(R.id.btnLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.editTextEmail).text.toString()
            val password = findViewById<EditText>(R.id.editTextPassword).text.toString()
            signInUser(email, password)
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registro exitoso
                    val user = auth.currentUser
                    Log.d("RegistroLoginActivity","User created: ${user?.email}")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Error en el registro
                    Log.d("RegistroLoginActivity","Failed to create user", task.exception)
                }
            }
    }

    private fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Inicio de sesión exitoso
                    val user = auth.currentUser
                    Log.d("RegistroLoginActivity","User signed in: ${user?.email}")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Error en el inicio de sesión
                    Log.d("RegistroLoginActivity","Failed to sign in user", task.exception)
                }
            }
    }
}
