package com.rf.jimbe

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.rf.jimbe.databinding.ActivityGuestDashboardBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuestDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGuestDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGuestDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Panggil fungsi fetch data
        fetchWorkoutCatalog()
    }

    private fun fetchWorkoutCatalog() {
        // Setup RecyclerView LayoutManager
        binding.rvWorkoutKatalog.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val workoutList = withContext(Dispatchers.IO) {
                    val call = ApiClient.instance.getWorkoutKatalog(20)
                    val response = call.execute()
                    if (response.isSuccessful) {
                        response.body()?.data ?: emptyList()
                    } else {
                        throw Exception("Gagal mendapat response: ${response.code()}")
                    }
                }

                binding.rvWorkoutKatalog.adapter = GuestDinamisAdapter(workoutList)
                Toast.makeText(this@GuestDashboardActivity, "Berhasil menarik data", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                Toast.makeText(this@GuestDashboardActivity, "Eror Request: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}