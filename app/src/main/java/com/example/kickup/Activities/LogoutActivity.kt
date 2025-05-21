package com.example.kickup.Activities

import android.content.Intent
import org.json.JSONObject
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.kickup.R

class LogoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        val editTextUsername = findViewById<EditText>(R.id.editText_username)
        val editTextPassword = findViewById<EditText>(R.id.editText_password)
        val buttonLogin = findViewById<Button>(R.id.button_login)

        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            loginUser(username, password)
        }
    }

    private fun loginUser(username: String, password: String) {
        val url = "http://192.168.1.13/api_kickup/login.php"

        val jsonBody = JSONObject()
        jsonBody.put("username_email", username)
        jsonBody.put("password", password)

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    val message = response.getString("message")

                    if (success) {
                        Toast.makeText(applicationContext, "✅ $message", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MajorActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(applicationContext, "❌ $message", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(applicationContext, "Error al procesar respuesta JSON", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(applicationContext, "Error en la conexión con el servidor", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

}