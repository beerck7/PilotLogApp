package com.example.pilotlog.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class SpiderChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paintWeb = Paint().apply {
        color = Color.parseColor("#3338BDF8")
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val paintData = Paint().apply {
        color = Color.parseColor("#CCA855F7")
        style = Paint.Style.FILL_AND_STROKE
        strokeWidth = 4f
        isAntiAlias = true
        alpha = 150
    }

    private val paintText = Paint().apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val webPath = Path()
    private val dataPath = Path()

    private var dataPoints: List<Float> = listOf(0.5f, 0.5f, 0.5f, 0.5f, 0.5f)
    private var labels: List<String> = listOf("Endurance", "Experience", "Versatility", "Precision", "Night Ops")

    fun setData(data: List<Float>, newLabels: List<String>) {
        dataPoints = data
        labels = newLabels
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) * 0.8f
        val sides = dataPoints.size
        val angleStep = (2 * Math.PI / sides).toFloat()

        for (i in 1..4) {
            val r = radius * (i / 4f)
            webPath.reset()
            for (j in 0 until sides) {
                val angle = j * angleStep - Math.PI.toFloat() / 2
                val x = centerX + r * cos(angle)
                val y = centerY + r * sin(angle)
                if (j == 0) webPath.moveTo(x, y) else webPath.lineTo(x, y)
            }
            webPath.close()
            canvas.drawPath(webPath, paintWeb)
        }

        for (j in 0 until sides) {
            val angle = j * angleStep - Math.PI.toFloat() / 2
            val x = centerX + radius * cos(angle)
            val y = centerY + radius * sin(angle)
            canvas.drawLine(centerX, centerY, x, y, paintWeb)
            
            val labelX = centerX + (radius + 40) * cos(angle)
            val labelY = centerY + (radius + 40) * sin(angle) + 10
            canvas.drawText(labels[j], labelX, labelY, paintText)
        }

        if (dataPoints.isNotEmpty()) {
            dataPath.reset()
            for (j in 0 until sides) {
                val angle = j * angleStep - Math.PI.toFloat() / 2
                val value = dataPoints[j]
                val r = radius * value
                val x = centerX + r * cos(angle)
                val y = centerY + r * sin(angle)
                if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()
            canvas.drawPath(dataPath, paintData)
        }
    }
}

