package com.rf.jimbe

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.rf.jimbe.databinding.ActivityWorkoutKatalogBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WorkoutKatalogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkoutKatalogBinding
    private lateinit var workoutAdapter: WorkoutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkoutKatalogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView seperti biasa
        workoutAdapter = WorkoutAdapter(arrayListOf())
        binding.rvWorkout.layoutManager = LinearLayoutManager(this)
        binding.rvWorkout.adapter = workoutAdapter

        // MULAI NEMBAK API DARI SINI
        ApiClient.instance.getWorkoutKatalog(20).enqueue(object : Callback<ExerciseWrapper> {
            override fun onResponse(call: Call<ExerciseWrapper>, response: Response<ExerciseWrapper>) {
                if (response.isSuccessful && response.body() != null) {
                    val listData = response.body()!!.data

                    // Kirim data asli dari API ke adapter
                    workoutAdapter.setData(listData)
                    workoutAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@WorkoutKatalogActivity, "Gagal mengambil katalog latihan", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ExerciseWrapper>, t: Throwable) {
                Toast.makeText(this@WorkoutKatalogActivity, "Eror internet: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}