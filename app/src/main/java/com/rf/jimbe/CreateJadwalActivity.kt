package com.rf.jimbe

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream

class CreateJadwalActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private var base64ImageString: String = ""

    // Deklarasi view
    private lateinit var etNamaLatihan: EditText
    private lateinit var etDeskripsiLatihan: EditText
    private lateinit var etTanggal: EditText
    private lateinit var etJam: EditText
    private lateinit var etKuota: EditText
    private lateinit var ivPreviewImage: ImageView
    private lateinit var btnSelectImage: Button
    private lateinit var btnSimpanKelas: Button

    private var isEditMode = false
    private var editKelasId = ""
    private var editSisaKuota = 0

    // Launcher untuk membuka galeri dan memilih gambar
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            ivPreviewImage.visibility = View.VISIBLE
            Glide.with(this).load(uri).into(ivPreviewImage)
            convertUriToBase64(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_jadwal)

        database = FirebaseDatabase.getInstance().reference

        etNamaLatihan = findViewById(R.id.etNamaLatihan)
        etDeskripsiLatihan = findViewById(R.id.etDeskripsiLatihan)
        etTanggal = findViewById(R.id.etTanggal)
        etJam = findViewById(R.id.etJam)
        etKuota = findViewById(R.id.etKuota)
        ivPreviewImage = findViewById(R.id.ivPreviewImage)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnSimpanKelas = findViewById(R.id.btnSimpanKelas)

        // Cek mode EDIT
        val intentKelasId = intent.getStringExtra("EXTRA_KELAS_ID")
        if (intentKelasId != null) {
            isEditMode = true
            editKelasId = intentKelasId
            btnSimpanKelas.text = "Update Kelas"
            
            // Isi form dengan data lama
            etNamaLatihan.setText(intent.getStringExtra("EXTRA_NAMA"))
            etDeskripsiLatihan.setText(intent.getStringExtra("EXTRA_DESKRIPSI"))
            etTanggal.setText(intent.getStringExtra("EXTRA_TANGGAL"))
            etJam.setText(intent.getStringExtra("EXTRA_JAM"))
            val maxKuota = intent.getIntExtra("EXTRA_KUOTA", 0)
            etKuota.setText(maxKuota.toString())
            editSisaKuota = intent.getIntExtra("EXTRA_SISA_KUOTA", maxKuota)
            base64ImageString = intent.getStringExtra("EXTRA_IMAGE") ?: ""
            
            if (base64ImageString.isNotEmpty()) {
                ivPreviewImage.visibility = View.VISIBLE
                if (base64ImageString.startsWith("http")) {
                    Glide.with(this).load(base64ImageString).into(ivPreviewImage)
                } else {
                    try {
                        val decodedBytes = Base64.decode(base64ImageString, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        ivPreviewImage.setImageBitmap(bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        btnSelectImage.setOnClickListener {
            // Membuka galeri khusus untuk gambar
            getContent.launch("image/*")
        }

        btnSimpanKelas.setOnClickListener {
            val nama = etNamaLatihan.text.toString().trim()
            val deskripsi = etDeskripsiLatihan.text.toString().trim()
            val tanggal = etTanggal.text.toString().trim()
            val jam = etJam.text.toString().trim()
            val kuotaStr = etKuota.text.toString().trim()

            if (nama.isEmpty() || tanggal.isEmpty() || jam.isEmpty() || kuotaStr.isEmpty()) {
                Toast.makeText(this, "Mohon lengkapi semua data wajib!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val kuota = kuotaStr.toIntOrNull() ?: 20
            
            // Generate atau pakai ID lama
            val kelasId = if (isEditMode) editKelasId else (database.child("kelas_latihan").push().key ?: return@setOnClickListener)
            
            val kuotaTersisaFix = if (isEditMode) editSisaKuota else kuota

            val kelas = KelasLatihan(
                id = kelasId,
                namaLatihan = nama,
                deskripsi = deskripsi,
                tanggal = tanggal,
                jam = jam,
                kuotaMaksimal = kuota,
                kuotaTersisa = kuotaTersisaFix,
                imageUrl = base64ImageString
            )

            // Simpan atau update ke Firebase
            database.child("kelas_latihan").child(kelasId).setValue(kelas)
                .addOnSuccessListener {
                    val msg = if (isEditMode) "Kelas berhasil diupdate!" else "Kelas berhasil dibuat!"
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Gagal menyimpan kelas: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun convertUriToBase64(uri: Uri) {
        try {
            val imageStream: InputStream? = contentResolver.openInputStream(uri)
            val selectedImage = BitmapFactory.decodeStream(imageStream)
            
            // Kompresi atau Resize Gambar agar Base64 tidak terlalu besar dan bikin nge-lag
            val scaledBitmap = Bitmap.createScaledBitmap(selectedImage, 600, (600 * selectedImage.height / selectedImage.width), true)
            
            val baos = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
            val b = baos.toByteArray()
            base64ImageString = Base64.encodeToString(b, Base64.DEFAULT)
            
            Toast.makeText(this, "Gambar berhasil disiapkan!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        }
    }
}
