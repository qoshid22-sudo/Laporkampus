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
import com.google.firebase.auth.FirebaseAuth // 🔥 Import library Firebase Auth

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // 1. Mengenalkan komponen XML Register ke Kotlin
        val etRegisterNama = findViewById<EditText>(R.id.etRegisterNama)
        val etRegisterEmail = findViewById<EditText>(R.id.etRegisterEmail)
        val etRegisterPassword = findViewById<TextInputEditText>(R.id.etRegisterPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvToLogin = findViewById<TextView>(R.id.tvToLogin)

        val tilRegisterPassword = etRegisterPassword.parent.parent as? TextInputLayout

        // 2. Logika ketika teks "Masuk di sini" diklik (Kembali ke LoginActivity via Intent)
        tvToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        // 3. Logika ketika tombol "Daftar Sekarang" diklik + Validasi Input
        btnRegister.setOnClickListener {
            val nama = etRegisterNama.text.toString().trim()
            val email = etRegisterEmail.text.toString().trim()
            val password = etRegisterPassword.text.toString().trim()

            // Reset semua error
            etRegisterNama.error = null
            etRegisterEmail.error = null
            tilRegisterPassword?.error = null
            tilRegisterPassword?.isErrorEnabled = false

            // Validasi 1: Nama Lengkap
            if (nama.isEmpty()) {
                etRegisterNama.error = "Nama lengkap tidak boleh kosong"
                etRegisterNama.requestFocus()
                return@setOnClickListener
            }

            // Validasi 2: Email Kosong
            if (email.isEmpty()) {
                etRegisterEmail.error = "Email tidak boleh kosong"
                etRegisterEmail.requestFocus()
                return@setOnClickListener
            }

            // Validasi 3: Memeriksa format email umum/pribadi (Bisa Gmail, Yahoo, dll)
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etRegisterEmail.error = "Format email tidak valid (Contoh: nama@gmail.com)"
                etRegisterEmail.requestFocus()
                return@setOnClickListener
            }

            // Validasi 4: Password Kosong
            if (password.isEmpty()) {
                tilRegisterPassword?.error = "Password tidak boleh kosong"
                etRegisterPassword.requestFocus()
                return@setOnClickListener
            }

            // Validasi 5: Panjang Password
            if (password.length < 8) {
                tilRegisterPassword?.error = "Password minimal harus 8 karakter"
                etRegisterPassword.requestFocus()
                return@setOnClickListener
            }

            // 🔥 4. PROSES DAFTAR KE FIREBASE AUTHENTICATION (ONLINE) 🔥
            // Nonaktifkan tombol daftar biar user gak klik berkali-kali pas proses upload data
            btnRegister.isEnabled = false

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    // Aktifkan kembali tombolnya setelah proses selesai
                    btnRegister.isEnabled = true

                    if (task.isSuccessful) {
                        // Kunci Sesi User ke Memori Internal HP (Jika Sukses Terdaftar di Firebase)
                        val sharedPref = getSharedPreferences("SesiLapor", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putBoolean("isLoggedIn", true) // Status login diset TRUE
                        editor.putString("namaUser", nama)     // Simpan nama user offline
                        editor.putString("emailUser", email)   // Simpan email user offline
                        editor.apply()

                        Toast.makeText(this, "Pendaftaran Berhasil! Selamat Datang $nama", Toast.LENGTH_LONG).show()

                        // 🔥 5. TERBANG LANGSUNG KE MAINACTIVITY (DASBOR)
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish() // Menutup RegisterActivity total
                    } else {
                        // Jika proses daftar gagal (misalnya karena kuota internet mati atau email sudah pernah didaftarkan)
                        Toast.makeText(this, "Gagal Daftar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}