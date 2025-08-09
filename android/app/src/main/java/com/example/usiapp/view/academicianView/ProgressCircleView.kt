package com.example.usiapp.view.academicianView

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View

// Özel bir View: Daire şeklinde ilerleme göstergesi (Progress Circle)
class ProgressCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // Gösterilecek yüzde (0.0 - 1.0 arası tutuluyor)
    var percentage: Float = 0.0f
        set(value) {
            field = value.coerceIn(0f, 1f) // 0 ile 1 aralığına sıkıştır
            invalidate() // Görünümü yenile
        }

    // Pay / payda değerleri
    var numerator: Int = 0
        set(value) {
            field = value
            invalidate()
        }
    var denominator: Int = 0
        set(value) {
            field = value
            invalidate()
        }

    // Arka plandaki (boş) çemberin boyama ayarları
    private val basePaint = Paint().apply {
        color = Color.parseColor("#FFFFFF") // Açık renk
        style = Paint.Style.STROKE // Sadece kenar çizilir
        strokeWidth = 30f
        isAntiAlias = true // Kenarları yumuşat
    }

    // Dolan kısmın boyama ayarları
    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#FFFFFF") // Koyu renk
        style = Paint.Style.STROKE
        strokeWidth = 30f
        isAntiAlias = true
    }

    // Ortadaki yüzde yazısının boyama ayarları
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 50f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        textAlign = Paint.Align.CENTER // Ortadan hizala
    }

    // Pay/payda için küçük yazı
    private val subTextPaint = Paint().apply {
        color = Color.GRAY
        textSize = 33f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    // Arka dairenin rengini değiştir
    fun setBaseColor(hexColor: String) {
        try {
            basePaint.color = Color.parseColor(hexColor)
            invalidate()
        } catch (e: IllegalArgumentException) {
            Log.e("ProgressCircleView", "Geçersiz baseColor: $hexColor")
        }
    }

    // Dolan kısmın rengini değiştir
    fun setProgressColor(hexColor: String) {
        try {
            overlayPaint.color = Color.parseColor(hexColor)
            invalidate()
        } catch (e: IllegalArgumentException) {
            Log.e("ProgressCircleView", "Geçersiz progressColor: $hexColor")
        }
    }

    // Çizim işlemleri burada yapılır
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Yarıçapı hesapla (stroke kalınlığını düş)
        val radius = (width.coerceAtMost(height) / 2f) - basePaint.strokeWidth
        val centerX = width / 2f
        val centerY = height / 2f

        // Çemberin alanı (dikdörtgen koordinatları)
        val rectF = RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Arka daire (boş progress)
        canvas.drawArc(rectF, 0f, 360f, false, basePaint)

        // Dolan kısım (yüzdeye göre)
        canvas.drawArc(rectF, -90f, 360f * percentage, false, overlayPaint)

        // Yüzde metnini hesapla ve tam ortaya yaz
        val percentText = "%${(percentage * 100).toInt()}"
        val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2 - 15
        canvas.drawText(percentText, centerX, textY, textPaint)

        // Pay/payda yazısı (alt kısım)
        if (denominator > 0) {
            val fractionText = "$numerator/$denominator"
            val subTextY = textY + 50 // biraz aşağı kaydır
            canvas.drawText(fractionText, centerX, subTextY, subTextPaint)
        }
    }

    // Belirli bir süre içinde yüzdeyi 0'dan hedef değere animasyonla getir
    fun animatePercentage(target: Float, duration: Long = 1000) {
        val animator = ValueAnimator.ofFloat(0f, target.coerceIn(0f, 1f))
        animator.duration = duration
        animator.addUpdateListener { animation ->
            percentage = animation.animatedValue as Float
        }
        animator.start()
    }
}
