package com.example.kickup.Activities

import android.os.Bundle

import android.view.View
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity
import com.example.kickup.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MajorActivity : AppCompatActivity() {

    private lateinit var formularioContainer: ScrollView
    private lateinit var btnCrear: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_major)

        formularioContainer = findViewById(R.id.formularioContainer)
        btnCrear = findViewById(R.id.btnCrear)

        //boton crear
        btnCrear.setOnClickListener {
            if (formularioContainer.visibility == View.GONE) {
                formularioContainer.visibility = View.VISIBLE
            } else {
                formularioContainer.visibility = View.GONE
            }
        }
    }
}

