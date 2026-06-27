package com.rf.jimbe

import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase

class KelasLatihanAdapter(
    private val listKelas: List<KelasLatihan>,
    private val isTrainer: Boolean = false,
    private val onCardClick: (KelasLatihan) -> Unit
) : RecyclerView.Adapter<KelasLatihanAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNamaKelas: TextView = view.findViewById(R.id.tvNamaKelas)
        val tvTanggalWaktu: TextView = view.findViewById(R.id.tvTanggalWaktu)
        val tvKuota: TextView = view.findViewById(R.id.tvKuota)
        val ivBackground: ImageView = view.findViewById(R.id.ivBackground)
        
        val llTrainerActions: LinearLayout = view.findViewById(R.id.llTrainerActions)
        val btnEditKelas: MaterialButton = view.findViewById(R.id.btnEditKelas)
        val btnDeleteKelas: MaterialButton = view.findViewById(R.id.btnDeleteKelas)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_kelas_latihan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val kelas = listKelas[position]

        holder.tvNamaKelas.text = kelas.namaLatihan
        holder.tvTanggalWaktu.text = "${kelas.tanggal} - ${kelas.jam}"
        holder.tvKuota.text = "Sisa Kuota: ${kelas.kuotaTersisa}/${kelas.kuotaMaksimal}"

        // Load image using Glide if available
        if (kelas.imageUrl.isNotEmpty()) {
            if (kelas.imageUrl.startsWith("http")) {
                Glide.with(holder.itemView.context)
                    .load(kelas.imageUrl)
                    .into(holder.ivBackground)
            } else {
                try {
                    val decodedBytes = Base64.decode(kelas.imageUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    holder.ivBackground.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            holder.ivBackground.setImageResource(R.drawable.bg_sage_gradient) // default fallback
        }
        
        if (isTrainer) {
            holder.llTrainerActions.visibility = View.VISIBLE
            
            holder.btnDeleteKelas.setOnClickListener {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Hapus Kelas")
                    .setMessage("Apakah Anda yakin ingin menghapus kelas ini secara permanen?")
                    .setPositiveButton("Hapus") { _, _ ->
                        FirebaseDatabase.getInstance().reference
                            .child("kelas_latihan")
                            .child(kelas.id)
                            .removeValue()
                            .addOnSuccessListener {
                                Toast.makeText(holder.itemView.context, "Kelas dihapus", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .setNegativeButton("Batal", null)
                    .show()
            }
            
            holder.btnEditKelas.setOnClickListener {
                val intent = Intent(holder.itemView.context, CreateJadwalActivity::class.java).apply {
                    putExtra("EXTRA_KELAS_ID", kelas.id)
                    putExtra("EXTRA_NAMA", kelas.namaLatihan)
                    putExtra("EXTRA_DESKRIPSI", kelas.deskripsi)
                    putExtra("EXTRA_TANGGAL", kelas.tanggal)
                    putExtra("EXTRA_JAM", kelas.jam)
                    putExtra("EXTRA_KUOTA", kelas.kuotaMaksimal)
                    putExtra("EXTRA_SISA_KUOTA", kelas.kuotaTersisa)
                    putExtra("EXTRA_IMAGE", kelas.imageUrl)
                }
                holder.itemView.context.startActivity(intent)
            }
            
            // Klik pada kartu memicu onCardClick (untuk melihat peserta)
            holder.itemView.setOnClickListener { onCardClick(kelas) }
            
        } else {
            holder.llTrainerActions.visibility = View.GONE
            holder.itemView.setOnClickListener {
                onCardClick(kelas)
            }
        }
    }

    override fun getItemCount(): Int = listKelas.size
}
