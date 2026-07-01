package com.rf.jimbe

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rf.jimbe.databinding.ActivityBmiBinding
import java.util.Locale
import kotlin.math.roundToInt

class BmiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBmiBinding
    private val database = FirebaseDatabase.getInstance().reference
    private var selectedGender = "Laki-Laki"
    private var selectedGoal = "Maintain"
    private var calculatedTdee = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.parseColor("#FF8A5C")
        binding = ActivityBmiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActivitySpinner()
        setupClickListeners()
        loadExistingBmiData()
    }

    private fun setupActivitySpinner() {
        val aktivitasOptions = arrayOf(
            "Sangat Ringan (Tidak Ada Latihan)",
            "Ringan (Latihan Ringan 1-3 hari/minggu)",
            "Sedang (Latihan Sedang 3-5 hari/minggu)",
            "Berat (Latihan Berat 6-7 hari/minggu)",
            "Sangat Berat (Latihan Ekstrim/Fisik)"
        )
        val adapter = ArrayAdapter(this, R.layout.item_spinner, aktivitasOptions)
        adapter.setDropDownViewResource(R.layout.item_spinner)
        binding.spAktivitas.adapter = adapter
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.cardMale.setOnClickListener { setGender("Laki-Laki") }
        binding.cardFemale.setOnClickListener { setGender("Perempuan") }

        binding.btnCalculate.setOnClickListener { performCalculation() }

        binding.btnGoalCutting.setOnClickListener { setGoal("Cutting") }
        binding.btnGoalMaintain.setOnClickListener { setGoal("Maintain") }
        binding.btnGoalBulking.setOnClickListener { setGoal("Bulking") }

        binding.btnSaveResults.setOnClickListener { saveResults() }
    }

    private fun setGender(gender: String) {
        selectedGender = gender
        if (gender == "Laki-Laki") {
            binding.cardMale.setCardBackgroundColor(Color.parseColor("#2E1C16"))
            binding.cardMale.strokeWidth = dpToPx(1)
            binding.tvMaleText.setTextColor(Color.parseColor("#FF8A5C"))
            binding.tvMaleText.setTypeface(null, android.graphics.Typeface.BOLD)

            binding.cardFemale.setCardBackgroundColor(Color.parseColor("#334155"))
            binding.cardFemale.strokeWidth = 0
            binding.tvFemaleText.setTextColor(Color.parseColor("#FFFFFF"))
            binding.tvFemaleText.setTypeface(null, android.graphics.Typeface.NORMAL)
        } else {
            binding.cardFemale.setCardBackgroundColor(Color.parseColor("#2E1C16"))
            binding.cardFemale.strokeWidth = dpToPx(1)
            binding.tvFemaleText.setTextColor(Color.parseColor("#FF8A5C"))
            binding.tvFemaleText.setTypeface(null, android.graphics.Typeface.BOLD)

            binding.cardMale.setCardBackgroundColor(Color.parseColor("#334155"))
            binding.cardMale.strokeWidth = 0
            binding.tvMaleText.setTextColor(Color.parseColor("#FFFFFF"))
            binding.tvMaleText.setTypeface(null, android.graphics.Typeface.NORMAL)
        }
    }

    private fun setGoal(goal: String) {
        selectedGoal = goal
        updateGoalTabs()
        updateNutritionTargets()
    }

    private fun updateGoalTabs() {
        val activeBg = Color.parseColor("#FF8A5C")
        val activeText = Color.parseColor("#FFFFFF")
        val inactiveBg = Color.TRANSPARENT
        val inactiveText = Color.parseColor("#94A3B8")

        binding.btnGoalCutting.setBackgroundColor(if (selectedGoal == "Cutting") activeBg else inactiveBg)
        binding.btnGoalCutting.setTextColor(if (selectedGoal == "Cutting") activeText else inactiveText)

        binding.btnGoalMaintain.setBackgroundColor(if (selectedGoal == "Maintain") activeBg else inactiveBg)
        binding.btnGoalMaintain.setTextColor(if (selectedGoal == "Maintain") activeText else inactiveText)

        binding.btnGoalBulking.setBackgroundColor(if (selectedGoal == "Bulking") activeBg else inactiveBg)
        binding.btnGoalBulking.setTextColor(if (selectedGoal == "Bulking") activeText else inactiveText)
    }

    private fun performCalculation() {
        val tinggiStr = binding.etTinggi.text.toString().trim()
        val beratStr = binding.etBerat.text.toString().trim()
        val umurStr = binding.etUmur.text.toString().trim()

        if (tinggiStr.isEmpty() || beratStr.isEmpty() || umurStr.isEmpty()) {
            Toast.makeText(this, "Mohon isi tinggi, berat, dan umur Anda!", Toast.LENGTH_SHORT).show()
            return
        }

        val tinggiVal = tinggiStr.toDoubleOrNull()
        val beratVal = beratStr.toDoubleOrNull()
        val umurVal = umurStr.toIntOrNull()

        if (tinggiVal == null || beratVal == null || umurVal == null || tinggiVal <= 0 || beratVal <= 0 || umurVal <= 0) {
            Toast.makeText(this, "Nilai yang Anda masukkan tidak valid!", Toast.LENGTH_SHORT).show()
            return
        }

        calculateAndDisplay(tinggiVal, beratVal, umurVal)
    }

    private fun calculateAndDisplay(height: Double, weight: Double, age: Int) {
        val heightM = height / 100.0
        val bmi = weight / (heightM * heightM)
        val formattedBmi = String.format(Locale.US, "%.1f", bmi).toFloat()

        binding.tvBmiScore.text = formattedBmi.toString()

        val (status, colorStr, desc) = when {
            bmi < 18.5 -> Triple(
                "Berat Kurang (Underweight)",
                "#3B82F6",
                "Berat badan Anda kurang dari ideal. Tingkatkan asupan kalori bernutrisi dan konsultasikan dengan trainer."
            )
            bmi in 18.5..24.99 -> Triple(
                "Ideal (Normal)",
                "#10B981",
                "Luar biasa! Berat badan Anda berada di rentang ideal. Pertahankan pola makan seimbang dan rutinitas latihan Anda saat ini."
            )
            bmi in 25.0..29.99 -> Triple(
                "Kelebihan Berat (Overweight)",
                "#F59E0B",
                "Berat badan Anda melebihi batas ideal. Kurangi kalori harian sedikit demi sedikit dan tingkatkan latihan cardio."
            )
            else -> Triple(
                "Obesitas (Obese)",
                "#EF4444",
                "Berat badan Anda berada dalam kategori obesitas. Sangat disarankan untuk berkonsultasi dengan dokter dan trainer untuk program diet."
            )
        }

        binding.tvBmiCategory.text = status
        binding.tvBmiCategory.setBackgroundColor(Color.parseColor(colorStr))
        binding.tvBmiDescription.text = desc

        val minIdeal = 18.5 * heightM * heightM
        val maxIdeal = 24.9 * heightM * heightM
        binding.tvIdealRange.text = String.format(Locale.getDefault(), "RENTANG BERAT IDEAL (TINGGI %.0fCM)\n%.1f kg - %.1f kg", height, minIdeal, maxIdeal)

        val bmr = if (selectedGender == "Laki-Laki") {
            (10 * weight) + (6.25 * height) - (5 * age) + 5
        } else {
            (10 * weight) + (6.25 * height) - (5 * age) - 161
        }
        val bmrInt = bmr.roundToInt()
        binding.tvBmr.text = "$bmrInt kcal"

        val activityIndex = binding.spAktivitas.selectedItemPosition
        val multiplier = when (activityIndex) {
            0 -> 1.2
            1 -> 1.375
            2 -> 1.55
            3 -> 1.725
            4 -> 1.9
            else -> 1.2
        }
        val tdee = bmr * multiplier
        calculatedTdee = tdee.roundToInt()
        binding.tvTdee.text = "$calculatedTdee kcal"

        updateGoalTabs()
        updateNutritionTargets()
        binding.layoutResults.visibility = View.VISIBLE
    }

    private fun updateNutritionTargets() {
        val calorieTarget = when (selectedGoal) {
            "Cutting" -> (calculatedTdee - 500).coerceAtLeast(1200)
            "Bulking" -> calculatedTdee + 500
            else -> calculatedTdee
        }

        binding.tvTargetCalories.text = "$calorieTarget kcal / hari"

        val carbsG = (calorieTarget * 0.55 / 4).roundToInt()
        val proteinG = (calorieTarget * 0.21 / 4).roundToInt()
        val fatG = (calorieTarget * 0.24 / 9).roundToInt()

        binding.tvCarbsText.text = "${carbsG}g (55%)"
        binding.tvProteinText.text = "${proteinG}g (21%)"
        binding.tvFatText.text = "${fatG}g (24%)"
    }

    private fun loadExistingBmiData() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid
        database.child("members").child(uid).child("bmi_data")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val tinggi = snapshot.child("tinggi").value?.toString() ?: ""
                        val berat = snapshot.child("berat").value?.toString() ?: ""
                        val umur = snapshot.child("umur").value?.toString() ?: ""
                        val gender = snapshot.child("gender").value?.toString() ?: "Laki-Laki"
                        val aktivitasIdx = snapshot.child("aktivitasIdx").value?.toString()?.toIntOrNull() ?: 1
                        val goal = snapshot.child("goal").value?.toString() ?: "Maintain"

                        binding.etTinggi.setText(tinggi)
                        binding.etBerat.setText(berat)
                        binding.etUmur.setText(umur)
                        binding.spAktivitas.setSelection(aktivitasIdx)

                        setGender(gender)
                        selectedGoal = goal

                        val tinggiVal = tinggi.toDoubleOrNull()
                        val beratVal = berat.toDoubleOrNull()
                        val umurVal = umur.toIntOrNull()

                        if (tinggiVal != null && beratVal != null && umurVal != null) {
                            calculateAndDisplay(tinggiVal, beratVal, umurVal)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun saveResults() {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return
        val uid = currentUser.uid

        val tinggiStr = binding.etTinggi.text.toString().trim()
        val beratStr = binding.etBerat.text.toString().trim()
        val umurStr = binding.etUmur.text.toString().trim()

        val tinggiVal = tinggiStr.toDoubleOrNull() ?: return
        val beratVal = beratStr.toDoubleOrNull() ?: return
        val umurVal = umurStr.toIntOrNull() ?: return

        val heightM = tinggiVal / 100.0
        val bmi = beratVal / (heightM * heightM)
        val formattedBmi = String.format(Locale.US, "%.1f", bmi).toFloat()

        val bmiStatus = when {
            bmi < 18.5 -> "Berat Kurang (Underweight)"
            bmi in 18.5..24.99 -> "Normal"
            bmi in 25.0..29.99 -> "Kelebihan Berat (Overweight)"
            else -> "Obesitas (Obese)"
        }

        val bmr = if (selectedGender == "Laki-Laki") {
            (10 * beratVal) + (6.25 * tinggiVal) - (5 * umurVal) + 5
        } else {
            (10 * beratVal) + (6.25 * tinggiVal) - (5 * umurVal) - 161
        }

        val activityIndex = binding.spAktivitas.selectedItemPosition
        val multiplier = when (activityIndex) {
            0 -> 1.2
            1 -> 1.375
            2 -> 1.55
            3 -> 1.725
            4 -> 1.9
            else -> 1.2
        }
        val tdee = bmr * multiplier
        val calorieTarget = when (selectedGoal) {
            "Cutting" -> (tdee - 500).roundToInt().coerceAtLeast(1200)
            "Bulking" -> (tdee + 500).roundToInt()
            else -> tdee.roundToInt()
        }

        val carbsG = (calorieTarget * 0.55 / 4).roundToInt()
        val proteinG = (calorieTarget * 0.21 / 4).roundToInt()
        val fatG = (calorieTarget * 0.24 / 9).roundToInt()

        val currentDate = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(java.util.Date())

        val bmiData = mapOf(
            "tinggi" to tinggiVal,
            "berat" to beratVal,
            "umur" to umurVal,
            "gender" to selectedGender,
            "aktivitasIdx" to activityIndex,
            "score" to formattedBmi,
            "status" to bmiStatus,
            "bmr" to bmr.roundToInt(),
            "tdee" to tdee.roundToInt(),
            "goal" to selectedGoal,
            "targetKalori" to calorieTarget,
            "carbs" to carbsG,
            "protein" to proteinG,
            "fat" to fatG,
            "tanggal" to currentDate
        )

        database.child("members").child(uid).child("bmi_data")
            .setValue(bmiData)
            .addOnSuccessListener {
                Toast.makeText(this, "Berhasil menyimpan data BMI & Nutrisi!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).roundToInt()
    }
}
