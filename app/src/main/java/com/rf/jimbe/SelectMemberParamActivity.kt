package com.rf.jimbe

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rf.jimbe.databinding.ActivityMemberListBinding

class SelectMemberParamActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemberListBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Sembunyikan FAB karena trainer hanya memilih member untuk statistik
        binding.fabAdd.visibility = View.GONE

        database = FirebaseDatabase.getInstance().reference

        binding.rvMembers.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        fetchMembers()
    }

    private fun fetchMembers() {
        // Mengambil seluruh data dari node "members" secara realtime
        database.child("members").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val membersList = mutableListOf<Member>()

                if (snapshot.exists()) {
                    for (memberSnapshot in snapshot.children) {
                        val id = memberSnapshot.child("id").value?.toString() ?: ""
                        val nama = memberSnapshot.child("nama_lengkap").value?.toString() ?: ""
                        val phone = memberSnapshot.child("nomor_hp").value?.toString() ?: ""
                        val alamat = memberSnapshot.child("alamat").value?.toString() ?: ""
                        val gender = memberSnapshot.child("gender").value?.toString() ?: ""
                        val expired = memberSnapshot.child("tanggal_berakhir_member").value?.toString() ?: ""

                        val member = Member(
                            id = id,
                            nama_lengkap = nama,
                            nomor_hp = phone,
                            alamat = alamat,
                            gender = gender,
                            tanggal_berakhir_member = expired
                        )
                        membersList.add(member)
                    }
                }
                
                // Kirim data ke RecyclerView Adapter beserta lambda click listener menuju StatisticActivity
                binding.rvMembers.adapter = MemberAdapter(membersList) { selectedMember ->
                    val intent = Intent(this@SelectMemberParamActivity, StatisticActivity::class.java).apply {
                        putExtra("MEMBER_ID", selectedMember.id.ifEmpty { "default_member" })
                        putExtra("MEMBER_NAME", selectedMember.nama_lengkap)
                    }
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@SelectMemberParamActivity, "Gagal mengambil data: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }
}
