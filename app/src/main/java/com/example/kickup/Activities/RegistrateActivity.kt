package com.example.kickup.Activities

import com.example.kickup.R
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.widget.EditText

class RegistrateActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrate)

        //CREAMOS LAS VARIANLES LOS BOTONES QUE NOS VAN A RECIBIR LOS VALORES
        val editTextUsername = findViewById<EditText>(R.id.editText_usarname)
        val editTextPassword = findViewById<EditText>(R.id.editText_password)
        val buttonCrearC = findViewById<Button>(R.id.btnCrear)

        //AQUI TENEMOS LOS VALORES PARA CREAR LAS CUENTA DE USUARIO DE MANERA MANUAL
        buttonCrearC.setOnClickListener {
            val username = editTextUsername.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            if (username.isNotEmpty() && password.isNotEmpty()) {
                registrarUsuario(username, password)  // Llama a la función para registrar usuario
            } else {
                Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            }
        }
    }



    //FUNCION PARA REGISTRAR MANUALMENTE A LOS USUARIOS
    private fun registrarUsuario(username: String, password: String) {
        val url = "http://192.168.1.23/api_kickup/register.php"

        val jsonBody = JSONObject()
        jsonBody.put("username_email", username)
        jsonBody.put("password", password)

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                val success = response.getBoolean("success")
                val message = response.getString("message")

                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()

                if (success) {
                    //SI EL REGISTRO FUE EXITOSO ME DEVUELVE A LA PANTALLA DE LOGOUT PARA INICIAR SESION CON LA CUENTA QUE ACABAMOS DE CREAR
                    val intent = Intent(this, LogoutActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(
                    applicationContext,
                    "Error en la conexión con el servidor",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }
}