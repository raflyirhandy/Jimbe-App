package com.rf.jimbe

import com.google.gson.annotations.SerializedName

data class ExerciseResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("bodyPart") val bodyPart: String,
    @SerializedName("target") val target: String,
    @SerializedName("gifUrl") val gifUrl: String
)

data class ExerciseWrapper(
    @SerializedName("data") val data: List<ExerciseResponse>
)

data class MessageModel(
    val senderId: String = "",
    val message: String = "",
    val timestamp: Long = 0
)

data class Member(
    val id: String = "",
    val nama_lengkap: String = "",
    val nomor_hp: String = "",
    val alamat: String = "",
    val gender: String = "",
    val role: String = "member",
    val status_member: String = "non-active",
    val tanggal_berakhir_member: String = ""
)

data class KelasLatihan(
    val id: String = "",
    val namaLatihan: String = "",
    val deskripsi: String = "",
    val tanggal: String = "",
    val jam: String = "",
    val kuotaMaksimal: Int = 0,
    val kuotaTersisa: Int = 0,
    val imageUrl: String = ""
)