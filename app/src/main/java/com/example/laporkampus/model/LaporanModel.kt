package com.example.laporkampus.model

// Tambahan = "" di setiap variabel adalah penyelamat agar Firebase gak bikin aplikasi crash!
data class LaporanModel(
    val id: String = "",
    val judul: String = "",
    val lokasi: String = "",
    val status: String = "",
    val tanggal: String = "",

    // 🔥 VARIABEL PENYELAMAT: Tempat menampung teks kode gambar Base64 dari Firebase
    val fotoUrl: String = ""
)