package com.rf.jimbe

data class JadwalLatihan(
    val idJadwal: String = "",
    val namaKelas: String = "",
    val deskripsiLatihan: String = "",
    val tanggal: String = "",
    val waktu: String = "",
    val kuotaMaksimal: Int = 15,
    val sisaKuota: Int = 15,
    val daftarPesertaUid: List<String> = emptyList() // Menyimpan UID member yang sudah daftar
)