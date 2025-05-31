package com.example.kickup.Activities

import android.content.Intent
import org.json.JSONObject
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.kickup.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LogoutActivity : AppCompatActivity() {

    //DECLARACIÓN DE VARIABLES PARA AUTENTICACIÓN Y GOOGLE SIGN-IN
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        //RECIBIMOS LOS VALORES EN NUESTRAS VARIABLES DEL LOGIN
        val editTextUsername = findViewById<EditText>(R.id.editText_username)
        val editTextPassword = findViewById<EditText>(R.id.editText_password)
        val buttonLogin = findViewById<Button>(R.id.button_login)

        //CONFIGURACIÓN DEL BOTON DE LOGIN MANUAL Y ENCAPSULAR LAS VALORES
        buttonLogin.setOnClickListener {
            val username = editTextUsername.text.toString()
            val password = editTextPassword.text.toString()
            loginUser(username, password)
        }

        //CONFIGURACIÓN DE FIREBASE Y OPCIONES DE GOOGLE SIGN-IN
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        //INICIALIZACIÓN DEL CLIENTE
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //CONFIGURACIÓN DE BOTÓN DE SIGN-IN CON GOOGLE
        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }

    //CONTROL RESULTADO DE LA ACTIVIDAD DE LOGIN CON GOOGLE
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Error en el login: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //AUTENTICACIÓN EN FIREBASE USANDO TOKEN DE GOOGLE
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
        val user = auth.currentUser
        val email = user?.email

        //CONDICIONAL DE SI SE RECIBE EL CORREO CONECTAR CON LA API DE LOGIN_GOOGLE.PHP
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
                    Toast.makeText(this, "Error al comunicar con el servidor", Toast.LENGTH_SHORT).show()
                }
            )

            Volley.newRequestQueue(this).add(request)
        }
    }

    //FUNCIÓN PARA AUTENTICAR USUARIO MANUALMENTE POR MEDIO DE API DE LOGIN.PHP
    private fun loginUser(username: String, password: String) {
        val url = "http://192.168.1.23/api_kickup/login.php"
        val jsonBody = JSONObject()
        jsonBody.put("username_email", username)
        jsonBody.put("password", password)

        val request = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    Log.d("LOGIN_RESPONSE", response.toString())

                    if (success) {
                        val userId = response.getInt("id")
                        val usernameEmail = response.getString("username_email")

                        Toast.makeText(applicationContext, "Bienvenido $usernameEmail, tu ID es $userId", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, MajorActivity::class.java)
                        intent.putExtra("user_id", userId)
                        intent.putExtra("username_email", usernameEmail)
                        Log.d("LOGIN", "Iniciando MajorActivity con ID: $userId")
                        Toast.makeText(this, "Bienvenido $usernameEmail (ID: $userId)", Toast.LENGTH_SHORT).show()
                        startActivity(intent)
                        finish()
                    } else {
                        val message = response.getString("message")
                        Toast.makeText(applicationContext, "❌ $message", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("LOGIN_ERROR", "Error al procesar el login", e)
                    Toast.makeText(applicationContext, "❌ Error al procesar login: ${e.message}", Toast.LENGTH_LONG).show()
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