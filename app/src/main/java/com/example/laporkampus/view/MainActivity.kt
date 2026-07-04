package com.example.laporkampus.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton // 🔥 Import baru untuk tombol Logout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.laporkampus.R
import com.example.laporkampus.model.LaporanModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    // 🛠️ Sambungkan kabel ke tabel "laporan" di Firebase Cloud
    private val databaseRef = FirebaseDatabase.getInstance().getReference("laporan")

    private lateinit var adapter: LaporanAdapter
    private val listLaporanAsli = ArrayList<LaporanModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🔥 1. PASANG SATPAM SESI: Cek memori HP sebelum melahirkan tampilan dasbor
        val sharedPref = getSharedPreferences("SesiLapor", Context.MODE_PRIVATE)
        val sudahLogin = sharedPref.getBoolean("isLoggedIn", false)

        // Jika FALSE, usir langsung balik ke halaman Login
        if (!sudahLogin) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 🔥 2. KENALKAN KOMPONEN XML DARI DASHBOARD BARU
        // Menggunakan ID baru dari activity_main.xml yang sudah dirombak
        val tvWelcomeUser = findViewById<TextView>(R.id.tvWelcomeUser)
        val tvTotalProses = findViewById<TextView>(R.id.tvTotalProses) // Nama baru dari R.id.tvTotalPending
        val tvTotalSelesai = findViewById<TextView>(R.id.tvTotalSelesai)
        val btnLogout = findViewById<ImageButton>(R.id.btnLogout)
        val fabAddLaporan = findViewById<FloatingActionButton>(R.id.fabAddLaporan) // Nama baru dari R.id.fabTambahLapor

        // 🔥 3. PASANG NAMA USER DI HEADER
        val namaUser = sharedPref.getString("namaUser", "Mahasiswa")
        tvWelcomeUser.text = "Halo, $namaUser!"

        // Inisialisasi RecyclerView
        val rvLaporan = findViewById<RecyclerView>(R.id.rvLaporan)
        rvLaporan.layoutManager = LinearLayoutManager(this)
        adapter = LaporanAdapter(listLaporanAsli)
        rvLaporan.adapter = adapter

        // 📡 4. FUNGSI MAGIC: Membaca data Real-Time & Hitung Otomatis
        databaseRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listLaporanAsli.clear()
                var jumlahSelesai = 0
                var jumlahProses = 0

                for (dataItem in snapshot.children) {
                    val laporan = dataItem.getValue(LaporanModel::class.java)
                    if (laporan != null) {
                        listLaporanAsli.add(laporan)

                        // LOGIKA MENGHITUNG SESUAI ID BARU
                        if (laporan.status.equals("SELESAI", ignoreCase = true)) {
                            jumlahSelesai++
                        } else {
                            // Anggap status lain (PROSES, PENDING, TERKIRIM) sebagai "Diproses"
                            jumlahProses++
                        }
                    }
                }

                // 🔥 PASANG ANGKA KE CARD VIEW YANG BARU
                tvTotalSelesai.text = jumlahSelesai.toString()
                tvTotalProses.text = jumlahProses.toString() // ID baru

                listLaporanAsli.reverse()
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // 5. Logika Tombol Melayang (+ ID BARU)
        fabAddLaporan.setOnClickListener {
            val intent = Intent(this, AddLaporanActivity::class.java)
            startActivity(intent)
        }

        // 6. Logika Tombol Logout
        btnLogout.setOnClickListener {
            // Hapus Sesi
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()

            // Balik ke Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}