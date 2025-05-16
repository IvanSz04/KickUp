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

        val boton = findViewById<Button>(R.id.btn_pre_logout)

        boton.setOnClickListener {
            val intent = Intent(this, LogoutActivity::class.java)
            startActivity(intent)

        }
    }
}