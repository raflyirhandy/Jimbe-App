package com.rf.jimbe

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        val btnToLogin = findViewById<MaterialButton>(R.id.btnToLogin)
        val btnToRegister = findViewById<MaterialButton>(R.id.btnToRegister)
        val btnAsGuest = findViewById<MaterialButton>(R.id.btnAsGuest)

        btnToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        btnToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        btnAsGuest.setOnClickListener {
            // Jika tombol Guest diklik, lempar langsung ke halaman Guest Dashboard API
            startActivity(Intent(this, GuestDashboardActivity::class.java))
        }
    }
}