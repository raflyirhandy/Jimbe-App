package com.rf.jimbe

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class LineChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataPoints = listOf<Float>()

    // Paint untuk garis kurva (Warna Salmon Red Coral Sesuai Gambar Baru)
    private val linePaint = Paint().apply {
        color = Color.parseColor("#E95757")
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    // Paint untuk isi gradasi di bawah garis
    private val areaPaint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    fun setData(points: List<Float>) {
        this.dataPoints = points
        invalidate() // Gambar ulang view saat data berubah
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (dataPoints.size < 2) return

        val maxVal = dataPoints.maxOrNull() ?: 1f
        val minVal = dataPoints.minOrNull() ?: 0f
        val diff = if (maxVal == minVal) 1f else maxVal - minVal

        // Tambahkan padding biar grafik tidak mentok ke bingkai card
        val padding = 30f
        val chartWidth = width - (padding * 2)
        val chartHeight = height - (padding * 2)

        val stepX = chartWidth / (dataPoints.size - 1)

        val linePath = Path()
        val areaPath = Path()

        for (i in dataPoints.indices) {
            val x = padding + (i * stepX)
            // Rumus membalikkan koordinat Y Android (karena 0 dimulai dari atas)
            val normalizedY = (dataPoints[i] - minVal) / diff
            val y = padding + (chartHeight - (normalizedY * chartHeight))

            if (i == 0) {
                linePath.moveTo(x, y)
                areaPath.moveTo(x, height.toFloat() - padding)
                areaPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                areaPath.lineTo(x, y)
            }

            if (i == dataPoints.size - 1) {
                areaPath.lineTo(x, height.toFloat() - padding)
                areaPath.close()
            }
        }

        // Bikin efek gradasi warna Salmon Red Coral transparan
        areaPaint.shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            Color.parseColor("#E95757"), // Warna atas (Salmon Red Soft)
            Color.parseColor("#00FFFFFF"), // Warna bawah (Transparan penuh menyatu dengan card putih)
            Shader.TileMode.CLAMP
        )

        // Gambar area gradasi dulu baru garis utamanya
        canvas.drawPath(areaPath, areaPaint)
        canvas.drawPath(linePath, linePaint)
    }
}