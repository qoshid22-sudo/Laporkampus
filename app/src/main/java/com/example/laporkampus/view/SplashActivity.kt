package com.example.laporkampus.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.laporkampus.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🔥 FIX: Sekarang memakai layout putih polos khusus splash, bukan layout login lagi
        setContentView(R.layout.activity_splash)

        // 🔥 FIX: Set waktu tunggu pas 2 detik (2000 ms) sebelum ngecek sesi login
        Handler(Looper.getMainLooper()).postDelayed({

            // Proses pengecekan status login user
            val sharedPref = getSharedPreferences("SesiLapor", Context.MODE_PRIVATE)
            val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

            if (isLoggedIn) {
                // Jika sudah daftar/login: Langsung masuk dasbor
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                // Jika belum login: Masuk ke halaman Login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }

            // Tutup SplashActivity agar tidak bisa di-back
            finish()

        }, 2000) // 2000 milidetik = 2 detik
    }
}