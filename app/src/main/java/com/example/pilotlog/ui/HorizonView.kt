package com.example.pilotlog.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class HorizonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var pitch: Float = 0f
    private var roll: Float = 0f

    private val skyPaint = Paint().apply {
        color = Color.parseColor("#0099CC")
        style = Paint.Style.FILL
    }

    private val groundPaint = Paint().apply {
        color = Color.parseColor("#8B4513")
        style = Paint.Style.FILL
    }

    private val linePaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
        isAntiAlias = true
    }

    private val aircraftPaint = Paint().apply {
        color = Color.YELLOW
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    fun updateAttitude(pitch: Float, roll: Float) {
        this.pitch = pitch
        this.roll = roll
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2
        val centerY = height / 2

        canvas.save()
        canvas.rotate(-roll, centerX, centerY)

        val pixelsPerDegree = height / 60f
        val pitchOffset = pitch * pixelsPerDegree

        canvas.drawRect(
            -width, -height * 2 + pitchOffset + centerY,
            width * 2, centerY + pitchOffset,
            skyPaint
        )

        canvas.drawRect(
            -width, centerY + pitchOffset,
            width * 2, height * 2 + pitchOffset + centerY,
            groundPaint
        )

        canvas.drawLine(
            -width, centerY + pitchOffset,
            width * 2, centerY + pitchOffset,
            linePaint
        )

        drawPitchLadder(canvas, centerX, centerY, pitchOffset, pixelsPerDegree)

        canvas.restore()

        drawAircraftSymbol(canvas, centerX, centerY)
        
        drawBankScale(canvas, centerX, centerY, width, height)
    }

    private fun drawPitchLadder(canvas: Canvas, cx: Float, cy: Float, offset: Float, ppd: Float) {
        val step = 10
        val range = 4
        
        for (i in -range..range) {
            if (i == 0) continue
            val deg = i * step
            val y = cy + offset - (deg * ppd)
            val width = if (i % 2 == 0) 200f else 100f
            
            canvas.drawLine(cx - width / 2, y, cx + width / 2, y, linePaint)
            canvas.drawText(deg.toString(), cx + width / 2 + 40, y + 15, textPaint)
        }
    }

    private fun drawAircraftSymbol(canvas: Canvas, cx: Float, cy: Float) {
        canvas.drawLine(cx - 100, cy, cx - 30, cy, aircraftPaint)
        canvas.drawLine(cx + 30, cy, cx + 100, cy, aircraftPaint)
        canvas.drawCircle(cx, cy, 10f, aircraftPaint)
        canvas.drawLine(cx, cy - 30, cx, cy, aircraftPaint)
    }
    
    private fun drawBankScale(canvas: Canvas, cx: Float, cy: Float, w: Float, h: Float) {
        val radius = w * 0.4f
        
        val pointerPath = Path().apply {
            moveTo(cx, 50f)
            lineTo(cx - 20, 20f)
            lineTo(cx + 20, 20f)
            close()
        }
        canvas.drawPath(pointerPath, aircraftPaint)
    }
}
