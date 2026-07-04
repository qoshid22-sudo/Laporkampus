package com.example.laporkampus.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.laporkampus.R
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddLaporanActivity : AppCompatActivity() {

    private lateinit var ivPreviewFoto: ImageView

    // 📸 Wadah global untuk menyimpan data gambar mentah dari kamera sementara waktu
    private var fotoBitmap: Bitmap? = null

    // 🛠️ Hubungkan koneksi ke database Firebase (Nama tabel: "laporan")
    private val databaseRef = FirebaseDatabase.getInstance().getReference("laporan")

    // JALUR REGISTRASI KAMERA: Menangkap hasil jepretan foto dari kamera bawaan
    private val launcherKamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Mengambil data gambar mini (thumbnail) hasil jepretan
            val gambarBitmap = result.data?.extras?.get("data") as? Bitmap
            if (gambarBitmap != null) {
                fotoBitmap = gambarBitmap // Simpan ke wadah global agar bisa di-convert nanti

                // Tampilkan ImageView dan pasang fotonya ke layar
                ivPreviewFoto.visibility = View.VISIBLE
                ivPreviewFoto.setImageBitmap(gambarBitmap)
                Toast.makeText(this, "Foto berhasil dimuat!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_laporan)

        // Mengenalkan semua komponen XML Baru ke Kotlin
        val etAddJudul = findViewById<EditText>(R.id.etAddJudul)
        val etAddLokasi = findViewById<EditText>(R.id.etAddLokasi)
        val btnAmbilFoto = findViewById<Button>(R.id.btnAmbilFoto)
        val btnKirimLaporan = findViewById<Button>(R.id.btnKirimLaporan)
        ivPreviewFoto = findViewById(R.id.ivPreviewFoto)

        // Logika ketika tombol "Buka Kamera" diklik
        btnAmbilFoto.setOnClickListener {
            val intentKamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            launcherKamera.launch(intentKamera)
        }

        // Logika ketika tombol "Kirim Laporan" diklik
        btnKirimLaporan.setOnClickListener {
            val judul = etAddJudul.text.toString().trim()
            val lokasi = etAddLokasi.text.toString().trim()

            // Reset error terdahulu agar bersih kembali
            etAddJudul.error = null
            etAddLokasi.error = null

            // Validasi input 1: Judul tidak boleh kosong
            if (judul.isEmpty()) {
                etAddJudul.error = "Judul laporan tidak boleh kosong"
                etAddJudul.requestFocus()
                return@setOnClickListener
            }

            // Validasi input 2: Lokasi tidak boleh kosong
            if (lokasi.isEmpty()) {
                etAddLokasi.error = "Lokasi tidak boleh kosong"
                etAddLokasi.requestFocus()
                return@setOnClickListener
            }

            // 🔥 1. PROSES CONVERT GAMBAR MENJADI TEKS STRING BASE64
            var stringFotoBase64 = ""
            if (fotoBitmap != null) {
                val outputStream = ByteArrayOutputStream()
                // Kompres kualitas gambar ke 70% agar ukurannya enteng saat disimpan di database teks
                fotoBitmap!!.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val byteGambar = outputStream.toByteArray()
                // Mengubah byte gambar menjadi deretan teks kode Base64
                stringFotoBase64 = Base64.encodeToString(byteGambar, Base64.DEFAULT)
            }

            // 🔥 2. PROSES KIRIM DATA REAL-TIME KE FIREBASE CLOUD
            // Membuat kunci ID unik otomatis di Firebase agar data tidak saling tumpang tindih
            val laporanId = databaseRef.push().key ?: return@setOnClickListener

            // Mengambil waktu/tanggal otomatis hari ini (Format: "Juni 12")
            val formatTanggal = SimpleDateFormat("MMM dd", Locale("id", "ID"))
            val tanggalSekarang = formatTanggal.format(Date())

            // Menyusun paket data laporan ke dalam bentuk Map (Termasuk teks foto)
            val paketLaporan = mapOf(
                "id" to laporanId,
                "judul" to judul,
                "lokasi" to lokasi,
                "status" to "PENDING",
                "tanggal" to tanggalSekarang,
                "fotoUrl" to stringFotoBase64 // Teks kode gambar ikut nemplok di sini!
            )

            // Mengunggah paket data ke Firebase cloud berdasarkan ID-nya
            databaseRef.child(laporanId).setValue(paketLaporan)
                .addOnSuccessListener {
                    // Jika sukses terunggah ke internet
                    Toast.makeText(this, "Laporan + Foto Berhasil Dikirim ke Cloud!", Toast.LENGTH_SHORT).show()
                    finish() // Menutup halaman dan otomatis kembali ke Dasbor Utama
                }
                .addOnFailureListener { error ->
                    // Jika gagal kirim (misal karena tidak ada kuota/internet mati)
                    Toast.makeText(this, "Gagal Mengirim: ${error.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}