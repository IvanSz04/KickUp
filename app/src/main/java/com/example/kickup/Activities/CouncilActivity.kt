package com.example.kickup.Activities

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
import org.json.JSONObject

class CouncilActivity : AppCompatActivity() {

    //SE GUARDA EL ID DEL USUARIO QUE SE PASA POR INTENT
    private var userId: Int = -1
    private lateinit var btnGuardarInforme: Button

    //CAMPOS INGRESAR DATOS DEL CONSEJO
    private lateinit var editTextTittle: EditText
    private lateinit var editTextDifficulty: EditText
    private lateinit var editTextPosition: EditText
    private lateinit var editTextGoals: EditText
    private lateinit var editTextCsj1: EditText
    private lateinit var editTextCsj2: EditText
    private lateinit var editTextCsj3: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_council)

        //OBTENER EL ID DEL USUARIO DESDE EL INTENT
        userId = intent.getIntExtra("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: Usuario no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        //INICIALIZA LOS CAMPOS Y EL BOTÓN DE GUARDAR CONSEJO
        editTextTittle = findViewById(R.id.inputTittle)
        editTextDifficulty = findViewById(R.id.inputDifficulty)
        editTextPosition = findViewById(R.id.inputPosition)
        editTextGoals = findViewById(R.id.inputGoals)
        editTextCsj1 = findViewById(R.id.inputCsj1)
        editTextCsj2 = findViewById(R.id.inputCsj2)
        editTextCsj3 = findViewById(R.id.inputCsj3)
        btnGuardarInforme = findViewById(R.id.btnGuardarInforme)

        // VERIFICA SI VIENE EN MODO EDICIÓN (SI SE PASA UN ID DE CONSEJO)
        val consejoId = intent.getIntExtra("edit_consejo_id", -1)

        if (consejoId != -1) {
            //MODO EDICION: LLENA LOS CAMPOS CON LOS DATOS RECIBIDOS POR INTENT
            editTextTittle.setText(intent.getStringExtra("edit_consejo_tittle"))
            editTextDifficulty.setText(intent.getStringExtra("edit_consejo_difficulty"))
            editTextPosition.setText(intent.getStringExtra("edit_consejo_position"))
            editTextGoals.setText(intent.getStringExtra("edit_consejo_goals"))
            editTextCsj1.setText(intent.getStringExtra("edit_consejo_csj1"))
            editTextCsj2.setText(intent.getStringExtra("edit_consejo_csj2"))
            editTextCsj3.setText(intent.getStringExtra("edit_consejo_csj3"))

            //CONFIGURA EL BOTÓN PARA ACTUALIZAR EN LUGAR DE CREAR
            btnGuardarInforme.setOnClickListener {
                actualizarConsejo(consejoId)
            }
        } else {
            //MODO CREACIÓN: CUANDO NO SE RECIBE UN ID DE CONSEJO
            btnGuardarInforme.setOnClickListener {
                crearInforme(
                    editTextTittle.text.toString(),
                    editTextDifficulty.text.toString(),
                    editTextPosition.text.toString(),
                    editTextGoals.text.toString(),
                    editTextCsj1.text.toString(),
                    editTextCsj2.text.toString(),
                    editTextCsj3.text.toString()
                )
            }
        }
    }

    //FUNCIÓN PARA CREAR UN NUEVO INFORME EN CON NUESTRA C_INFORME.PHP)
    private fun crearInforme(
        tittle: String,
        difficulty: String,
        position: String,
        goals: String,
        csj1: String,
        csj2: String,
        csj3: String
    ) {
        val url = "http://192.168.1.23/api_kickup/c_informe.php"

        // JSON CON LOS DATOS DEL NUEVO INFORME
        val params = JSONObject()
        params.put("user_id", userId)
        params.put("tittle", tittle)
        params.put("difficulty", difficulty)
        params.put("position", position)
        params.put("goals", goals)
        params.put("csj1", csj1)
        params.put("csj2", csj2)
        params.put("csj3", csj3)

        // ENVÍA LA PETICIÓN USANDO VOLLEY
        val request = JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                val success = response.optBoolean("success", false)
                val message = response.optString("message", "No se recibió mensaje")
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (success) {
                    Toast.makeText(this, "Consejo actualizado", Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                }
            },
            { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }

    //FUNCIÓN PARA ACTUALIZAR UN INFORME EXISTENTE EN CON NUESTRA API A U_INFORME.PHP)
    private fun actualizarConsejo(id: Int) {
        val url = "http://192.168.1.23/api_kickup/u_informe.php" // CAMBIA A TU URL REAL

        //CREA EL OBJETO JSON CON LOS DATOS ACTUALIZADOS
        val jsonBody = JSONObject().apply {
            put("id", id)
            put("tittle", editTextTittle.text.toString())
            put("difficulty", editTextDifficulty.text.toString())
            put("position", editTextPosition.text.toString())
            put("goals", editTextGoals.text.toString())
            put("csj1", editTextCsj1.text.toString())
            put("csj2", editTextCsj2.text.toString())
            put("csj3", editTextCsj3.text.toString())
            put("user_id", userId)
        }

        //ENVÍA LA PETICIÓN PARA ACTUALIZAR
        val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                Toast.makeText(this, "Consejo actualizado", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK) // ✅ Agrega esto ANTES de finish()
                finish()
            },
            { error ->
                if (error.networkResponse?.data != null) {
                    val responseString = String(error.networkResponse.data)
                    Log.e("ActualizarConsejo", "Error completo: $responseString")
                }
                Log.e("ActualizarConsejo", "Volley error: ${error.message}")
                Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }
}