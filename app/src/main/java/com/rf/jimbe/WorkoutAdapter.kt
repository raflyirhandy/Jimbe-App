package com.rf.jimbe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // <- Setelah sync, ini dijamin tidak akan merah lagi
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rf.jimbe.databinding.ItemWorkoutBinding

class WorkoutAdapter(private var list: List<ExerciseResponse>) : RecyclerView.Adapter<WorkoutAdapter.WorkoutViewHolder>() {

    class WorkoutViewHolder(val binding: ItemWorkoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkoutViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WorkoutViewHolder, position: Int) {
        val item = list[position]

        // Menampilkan data teks ke komponen layout kartu item
        holder.binding.tvWorkoutName.text = item.name ?: "Tanpa Nama"

        val muscleText = item.target?.replaceFirstChar { it.uppercase() } ?: "Umum"
        holder.binding.tvMuscle.text = "💪 $muscleText"

        val bodyPartText = item.bodyPart?.replaceFirstChar { it.uppercase() } ?: "Full Body"
        holder.binding.tvEquipment.text = "⚙️ $bodyPartText"

        // Panggil Glide untuk render animasi GIF bergerak
        Glide.with(holder.itemView.context)
            .asGif()
            .load(item.gifUrl)
            .placeholder(R.drawable.bg_sage_gradient) // Sementara pakai ganjalan background yang sudah kamu punya
            .error(android.R.drawable.stat_notify_error)
            .into(holder.binding.ivWorkoutImage)

        // Logika Klik: Dialog pop-up
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            MaterialAlertDialogBuilder(context)
                .setTitle(item.name ?: "Detail Latihan")
                .setMessage("Target Otot: $muscleText\nBagian Tubuh: $bodyPartText\n\nSilakan ikuti instruksi gerakan ini dengan benar di bawah bimbingan trainer Anda.")
                .setPositiveButton("Paham") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    override fun getItemCount(): Int = list.size

    fun setData(newList: List<ExerciseResponse>) {
        this.list = newList
        notifyDataSetChanged()
    }
}