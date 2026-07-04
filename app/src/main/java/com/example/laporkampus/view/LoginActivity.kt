package com.example.laporkampus.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.laporkampus.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Mengenalkan komponen XML ke dalam Kotlin dengan tipe data yang sinkron
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvToRegister = findViewById<TextView>(R.id.tvToRegister)

        // Ambil penampung layout password untuk manajemen error tanda mata yang rapi
        val tilPassword = etPassword.parent.parent as? TextInputLayout

        // 2. Logika perpindahan halaman ke Register via Intent
        tvToRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 3. Logika Validasi Input saat tombol Masuk diklik
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Reset error terdahulu agar bersih kembali
            etEmail.error = null
            tilPassword?.error = null
            tilPassword?.isErrorEnabled = false

            // Validasi 1: Memeriksa apakah email kosong
            if (email.isEmpty()) {
                etEmail.error = "Email tidak boleh kosong"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // Validasi 2: Memeriksa format email umum/pribadi (Bisa Gmail, Yahoo, dll)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Format email tidak valid (Contoh: nama@gmail.com)"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            // Validasi 3: Memeriksa apakah password kosong
            if (password.isEmpty()) {
                tilPassword?.error = "Password tidak boleh kosong"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // Validasi 4: Memeriksa panjang password minimal 8 karakter
            if (password.length < 8) {
                tilPassword?.error = "Password minimal harus 8 karakter"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            // 🔥 4. PROSES AUTO-LOGIN: Kunci Sesi User ke Memori Internal HP saat Sukses Login
            val sharedPref = getSharedPreferences("SesiLapor", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putBoolean("isLoggedIn", true) // Status dikunci menjadi TRUE
            editor.putString("emailUser", email)   // Menyimpan email user secara offline
            editor.apply()

            Toast.makeText(this, "Login Berhasil! Selamat Datang", Toast.LENGTH_SHORT).show()

            // 🔥 5. TERBANG LANGSUNG KE MAINACTIVITY (DASBOR) + BERSIHKAN RIWAYAT BACK
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Menutup LoginActivity total
        }
    }
}