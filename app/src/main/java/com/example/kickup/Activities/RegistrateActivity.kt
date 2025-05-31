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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import org.json.JSONObject
import android.widget.EditText

class RegistrateActivity : AppCompatActivity() {

    //DEFINIMOS LAS VARIABLES DE AUTENTICACION QUE NOS PROPORCIONA GOOGLE Y FIREBASE
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    private val RC_SIGN_IN = 1001  // DEFINIMOS EL CODIGO DE SOLICITUD PARA EL INICIO DE SESION DE GOOGLE

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

        //INICIALIZAMOS LA SENTENCION DE INICIO POR GOOGLE
        auth = FirebaseAuth.getInstance()

        //CONFIGURAMOS GOOGLE
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //DEFINIMOS EL BOTON DE INICIO DE SESION POR GOOGLE
        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    //RESPUESTA DE INICIO DE SESION CON GOOGLE
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)  // Autenticación con Firebase
            } catch (e: ApiException) {
                Toast.makeText(this, "Error en el login: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //AUTENTICACION POR FIREBASE CON EL TOKEN DE GOOGLE
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
        val user = auth.currentUser
        val email = user?.email

        //CONDICIONAL DE SI SE RECIBE EL CORREO CONECTAR CON LA API DE REGISTRAR.PHP
        if (email != null) {
            val url = "http://192.168.1.23/api_kickup/login_google.php"
            val jsonBody = JSONObject()
            jsonBody.put("username_email", email)

            val request = JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                { response ->
                    val success = response.getBoolean("success")
                    val message = response.getString("message")
                    if (success) {
                        val userId = response.getInt("id")
                        val usernameEmail = response.getString("username_email")

                        Toast.makeText(
                            this,
                            "¡Bienvenido $usernameEmail! Tu id es $userId",
                            Toast.LENGTH_LONG
                        ).show()

                        //NOS ACCEDE A LA PANTALLA MAJORACTIVITY(PRINCIPAL Y PROPORCIONA EL ID DEL USUARIO)
                        val intent = Intent(this, MajorActivity::class.java)
                        intent.putExtra("user_id", userId)
                        intent.putExtra("username_email", usernameEmail)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "❌ $message", Toast.LENGTH_SHORT).show()
                    }
                },
                { error ->
                    error.printStackTrace()
                    Toast.makeText(this, "Error al comunicar con el servidor", Toast.LENGTH_SHORT)
                        .show()
                }
            )

            Volley.newRequestQueue(this).add(request)
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