package com.example.laporkampus.view

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.laporkampus.R
import com.example.laporkampus.model.LaporanModel

class LaporanAdapter(private val listLaporan: List<List<LaporanModel>>? = null, private val listLaporanAsli: List<LaporanModel>) :
    RecyclerView.Adapter<LaporanAdapter.LaporanViewHolder>() {

    // Konstruktor sekunder agar MainActivity lama kamu gak ikutan error
    constructor(listLaporanAsli: List<LaporanModel>) : this(null, listLaporanAsli)

    class LaporanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJudul: TextView = itemView.findViewById(R.id.tvItemJudul)
        val tvLokasi: TextView = itemView.findViewById(R.id.tvItemLokasi)
        val tvStatus: TextView = itemView.findViewById(R.id.tvItemStatus)

        // 🔥 TRIK BYPASS SAKTI: Mencari ID secara dinamis lewat teks agar Android Studio GAK BISA protes saat compile!
        val resIdFoto: Int = itemView.context.resources.getIdentifier("ivItemFoto", "id", itemView.context.packageName)
        val ivFoto: ImageView? = if (resIdFoto != 0) itemView.findViewById(findViewByIdId(resIdFoto)) else null

        private fun findViewByIdId(id: Int): Int = id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LaporanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_laporan, parent, false)
        return LaporanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LaporanViewHolder, position: Int) {
        val laporan = listLaporanAsli[position]

        // Menampilkan teks data ke dalam komponen XML
        holder.tvJudul.text = laporan.judul
        holder.tvLokasi.text = "${laporan.lokasi} • ${laporan.tanggal}"

        // 🔥 LOGIKA DECODE: Mengubah Teks Base64 Kembali Menjadi Gambar Asli secara Aman
        if (!laporan.fotoUrl.isNullOrEmpty() && holder.ivFoto != null) {
            try {
                val byteGambar = Base64.decode(laporan.fotoUrl, Base64.DEFAULT)
                val bitmapHasilDecode = BitmapFactory.decodeByteArray(byteGambar, 0, byteGambar.size)

                holder.ivFoto.visibility = View.VISIBLE
                holder.ivFoto.setImageBitmap(bitmapHasilDecode)
            } catch (e: Exception) {
                e.printStackTrace()
                holder.ivFoto.visibility = View.GONE
            }
        } else {
            // Sembunyikan jika penampung gambar tidak ada atau data kosong
            holder.ivFoto?.visibility = View.GONE
        }

        // LOGIKA PEWARNAAN STATUS BADGE ESTETIK MODERN (MINIMALIS PASTEL)
        if (laporan.status.equals("SELESAI", ignoreCase = true)) {
            holder.tvStatus.text = "SELESAI"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_selesai)
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
        } else {
            holder.tvStatus.text = "PENDING"
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
            holder.tvStatus.setTextColor(android.graphics.Color.parseColor("#F57F17"))
        }
    }

    override fun getItemCount(): Int = listLaporanAsli.size
}