package com.example.kickup.Activities

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.kickup.R
import android.widget.Button

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        //CONFIGURA EL BOTÓN PARA QUE ME LLEVE DE LOGIN (LogoutActivity)
        val boton = findViewById<Button>(R.id.btn_pre_logout)
        boton.setOnClickListener {
            val intent = Intent(this, LogoutActivity::class.java)
            startActivity(intent)
        }

        //CONFIGURA EL BOTÓN PARA QUE ME LLEVE DE REGISTRO (RegistrateActivity)
        val botons = findViewById<Button>(R.id.btn_registrate)
        botons.setOnClickListener {
            val intent = Intent(this, RegistrateActivity::class.java)
            startActivity(intent)
        }
    }
}