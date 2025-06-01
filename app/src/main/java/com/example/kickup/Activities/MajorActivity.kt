package com.example.kickup.Activities

import ConsejoAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.kickup.Model.Consejos
import com.example.kickup.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MajorActivity : AppCompatActivity() {

    private var userIdActual: Int = -1
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var consejoAdapter: ConsejoAdapter
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>


    //URI DEL ARCHIVO DE LA FOTO
    private var photoUri: Uri? = null

    //LANZADOR TAKE A FOTO
    private lateinit var takePictureLauncher: ActivityResultLauncher<Uri>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_major)

        //LOGEO CON GOOGLE
        auth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        //RECIBIR ID DEL USUARIO LOGEADO PARA CREAR CONSEJO
        userIdActual = intent.getIntExtra("user_id", -1)
        Log.d("MajorActivity", "userId recibido: $userIdActual")

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                abrirCamara()
            } else {
                Toast.makeText(this, "Se requieren permisos de cámara", Toast.LENGTH_SHORT).show()
            }
        }

        //LANZADOR TAKE A PHOTO
        takePictureLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                Toast.makeText(this, "Te tomaste esta foto", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "No se tomó la foto", Toast.LENGTH_SHORT).show()
            }
        }

        //DEFINIMOS EL BOTON DE LA CAMARA
        val btnCamara = findViewById<FloatingActionButton>(R.id.btnCamara)
        btnCamara.setOnClickListener {
            val cameraPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)

            if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
                abrirCamara()
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }

        //EMPEZAMOS CON LA FUNCION DE UBICACION
        val btnUbicacion = findViewById<FloatingActionButton>(R.id.btnUbicacion)
        btnUbicacion.setOnClickListener {
            val intent = Intent(this@MajorActivity, GeolocationActivity::class.java)
            startActivity(intent)
        }

        //EMPEZAMOS CON LA FUNCION DE CREAR CONSEJO
        val btnCrearInforme = findViewById<FloatingActionButton>(R.id.btnCrearConsejo)
        btnCrearInforme.setOnClickListener {
            if (userIdActual == -1) {
                Toast.makeText(this, "Usuario no válido para crear informe", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, CouncilActivity::class.java)
            intent.putExtra("user_id", userIdActual)
            startActivity(intent)
        }

        //AQUI LLAMAMOS A NUESTRO RECYCLERVIEW
        recyclerView = findViewById(R.id.rvConsejo)
        searchEditText = findViewById(R.id.editBuscarConsejo)

        recyclerView.layoutManager = LinearLayoutManager(this)

        consejoAdapter = ConsejoAdapter(emptyList()) { }
        recyclerView.adapter = consejoAdapter

        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                buscarConsejos(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                abrirCamara()
            } else {
                Toast.makeText(this, "Se requieren permisos de cámara y almacenamiento", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //ESTE ES METODO PARA ABRIR LA CAMARA
    private fun abrirCamara() {
        val photoFile: File? = try {
            crearArchivoImagen()
        } catch (ex: IOException) {
            Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show()
            null
        }

        photoFile?.also {
            photoUri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                it
            )
            takePictureLauncher.launch(photoUri)
        }
    }

    //NOS CREA EL ARCHIVO TEMPORAL AL MOMENTO DE TOMAR LA FOTO
    @Throws(IOException::class)
    private fun crearArchivoImagen(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    //EMPEZAMOS CON LA FUNCION DE LEER U OBTENER CONSEJOS
    private fun buscarConsejos(query: String) {
        val url = "http://192.168.1.23/api_kickup/r_informe.php"
        val jsonBody = JSONObject().apply {
            put("query", query)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                val dataArray = response.getJSONArray("data")
                Log.d("buscarConsejos", "Cantidad de resultados: ${dataArray.length()}")
                val lista = mutableListOf<Consejos>()
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    Log.d("buscarConsejos", "Consejo #$i: ${item.getString("tittle")}")
                    val consejo = Consejos(
                        id = item.getInt("id"),
                        user_id = item.getInt("user_id"),
                        tittle = item.getString("tittle"),
                        difficulty = item.getString("difficulty"),
                        position = item.getString("position"),
                        goals = item.getString("goals"),
                        csj1 = item.getString("csj1"),
                        csj2 = item.getString("csj2"),
                        csj3 = item.getString("csj3")
                    )
                    lista.add(consejo)
                }

                if (lista.isEmpty()) {
                    Toast.makeText(this, "No se encontraron resultados", Toast.LENGTH_SHORT).show()
                }

                consejoAdapter = ConsejoAdapter(lista) { consejo ->
                    mostrarDetalle(consejo)
                }
                recyclerView.adapter = consejoAdapter
            },
            {
                Toast.makeText(this, "Error al buscar", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(request)
    }


    //DEFINIMOS FUNCION DE MOSTRAR DETALLE
    private fun mostrarDetalle(consejo: Consejos) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_detalle_consejo, null)

        //REFERENCIAS DE LAS VISTAS
        val tvUsuario = dialogView.findViewById<TextView>(R.id.tvDetalleUsuario)
        val tvTitulo = dialogView.findViewById<TextView>(R.id.tvDetalleTitulo)
        val tvDificultad = dialogView.findViewById<TextView>(R.id.tvDetalleDificultad)
        val tvPosicion = dialogView.findViewById<TextView>(R.id.tvDetallePosicion)
        val tvObjetivos = dialogView.findViewById<TextView>(R.id.tvDetalleObjetivos)
        val tvCsj1 = dialogView.findViewById<TextView>(R.id.tvDetalleCsj1)
        val tvCsj2 = dialogView.findViewById<TextView>(R.id.tvDetalleCsj2)
        val tvCsj3 = dialogView.findViewById<TextView>(R.id.tvDetalleCsj3)

        val btnEditar = dialogView.findViewById<Button>(R.id.btnEditar)
        val btnEliminar = dialogView.findViewById<Button>(R.id.btnEliminar)
        val btnCerrar = dialogView.findViewById<Button>(R.id.btnCerrar)

        //ASIGNAMOS LOS VALORES
        tvUsuario.text = "ID Usuario: ${consejo.user_id}"
        tvTitulo.text = "Título: ${consejo.tittle}"
        tvDificultad.text = "Dificultad: ${consejo.difficulty}"
        tvPosicion.text = "Posición: ${consejo.position}"
        tvObjetivos.text = "Objetivos: ${consejo.goals}"
        tvCsj1.text = "Consejo 1: ${consejo.csj1}"
        tvCsj2.text = "Consejo 2: ${consejo.csj2}"
        tvCsj3.text = "Consejo 3: ${consejo.csj3}"

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)

        val dialog = dialogBuilder.create()

        //MOSTRAR BOTON DE EDITAR Y ELIMINAR SOLO SI ES EL USUARIO CREADOR
        if (consejo.user_id == userIdActual) {
            btnEditar.visibility = android.view.View.VISIBLE
            btnEliminar.visibility = android.view.View.VISIBLE

            btnEditar.setOnClickListener {
                dialog.dismiss()
                editarConsejo(consejo)
            }

            btnEliminar.setOnClickListener {
                dialog.dismiss()
                confirmarEliminacion(consejo)
            }
        }

        btnCerrar.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    //DEFINIMOS FUNCION DE EDITAR CONSEJO
    private fun editarConsejo(consejo: Consejos) {
        val intent = Intent(this, CouncilActivity::class.java)

        //PASAR DATOS PARA LA EDICION
        intent.putExtra("edit_consejo_id", consejo.id)
        intent.putExtra("edit_consejo_tittle", consejo.tittle)
        intent.putExtra("edit_consejo_difficulty", consejo.difficulty)
        intent.putExtra("edit_consejo_position", consejo.position)
        intent.putExtra("edit_consejo_goals", consejo.goals)
        intent.putExtra("edit_consejo_csj1", consejo.csj1)
        intent.putExtra("edit_consejo_csj2", consejo.csj2)
        intent.putExtra("edit_consejo_csj3", consejo.csj3)
        intent.putExtra("user_id", consejo.user_id) // para referencia
        startActivity(intent)
    }


    //DEFINIMOS FUNCION DE ELIMINAR CONSEJO
    private fun eliminarConsejo(id: Int) {
        val url = "http://192.168.1.23/api_kickup/d_informe.php"

        val jsonBody = JSONObject().apply {
            put("id", id)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, jsonBody,
            { response ->
                Toast.makeText(this, "Consejo eliminado", Toast.LENGTH_SHORT).show()
                buscarConsejos(searchEditText.text.toString())
            },
            { error ->
                Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }


    //SE ENVIA MENSAJE DE CONFIRMACION PARA ELMIMINAR
    private fun confirmarEliminacion(consejo: Consejos) {
        AlertDialog.Builder(this)
            .setTitle("Eliminar consejo")
            .setMessage("¿Estás seguro de eliminar este consejo?")
            .setPositiveButton("Sí") { _, _ ->
                eliminarConsejo(consejo.id)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                mostrarDetalle(consejo) // volver a mostrar detalles
            }
            .show()
    }
}



