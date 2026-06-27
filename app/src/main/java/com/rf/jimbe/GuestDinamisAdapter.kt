package com.rf.jimbe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rf.jimbe.databinding.ItemWorkoutBinding

class GuestDinamisAdapter(private var list: List<ExerciseResponse>) : RecyclerView.Adapter<GuestDinamisAdapter.GuestViewHolder>() {

    class GuestViewHolder(val binding: ItemWorkoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val binding = ItemWorkoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        val item = list[position]

        holder.binding.tvWorkoutName.text = item.name ?: "Tanpa Nama"

        val muscleText = item.target?.replaceFirstChar { it.uppercase() } ?: "Umum"
        holder.binding.tvMuscle.text = "💪 $muscleText"

        val bodyPartText = item.bodyPart?.replaceFirstChar { it.uppercase() } ?: "Full Body"
        holder.binding.tvEquipment.text = "⚙️ $bodyPartText"

        Glide.with(holder.itemView.context)
            .asGif()
            .load(item.gifUrl)
            .placeholder(R.drawable.bg_sage_gradient)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.binding.ivWorkoutImage)

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            MaterialAlertDialogBuilder(context)
                .setTitle(item.name ?: "Detail Latihan")
                .setMessage("Target Otot: $muscleText\nBagian Tubuh: $bodyPartText")
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
