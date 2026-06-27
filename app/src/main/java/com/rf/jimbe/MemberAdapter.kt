package com.rf.jimbe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rf.jimbe.databinding.ItemMemberBinding

class MemberAdapter(
    private val members: List<Member>,
    private val onItemClick: (Member) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.binding.tvName.text = member.nama_lengkap
        holder.binding.tvPhone.text = member.nomor_hp
        holder.binding.tvExpired.text = "Berakhir: ${member.tanggal_berakhir_member}"
        holder.binding.tvGenderBadge.text = if (member.gender.uppercase() == "L") "Pria" else "Wanita"

        // Trigger lambda function saat item diklik
        holder.itemView.setOnClickListener {
            onItemClick(member)
        }
    }

    override fun getItemCount() = members.size
}