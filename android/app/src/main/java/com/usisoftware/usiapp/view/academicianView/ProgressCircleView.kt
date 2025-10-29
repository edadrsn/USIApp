package com.usisoftware.usiapp.view.academicianView

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
import android.view.animation.DecelerateInterpolator

// Özel bir View: Daire şeklinde ilerleme göstergesi (Progress Circle)
class ProgressCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    // 0.0 - 1.0 arası ilerleme yüzdesi
    var percentage: Float = 0.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

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

    // Arka plan dairesi
    private val basePaint = Paint().apply {
        color = Color.parseColor("#E0E0E0") // Açık gri
        style = Paint.Style.STROKE
        strokeWidth = 30f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    // İlerleme dairesi
    private val overlayPaint = Paint().apply {
        color = Color.parseColor("#3F51B5") // Mavi
        style = Paint.Style.STROKE
        strokeWidth = 30f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }

    // Ana yüzde metni
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 50f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    // Alt metin (pay/payda)
    private val subTextPaint = Paint().apply {
        color = Color.GRAY
        textSize = 33f
        typeface = Typeface.DEFAULT
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    // View boyutu değiştiğinde orantılı boyutları ayarla
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val minDimen = w.coerceAtMost(h).toFloat()

        // Çizgi kalınlığını orantılı yap
        val stroke = minDimen * 0.12f
        basePaint.strokeWidth = stroke
        overlayPaint.strokeWidth = stroke

        // Yazı boyutlarını orantılı yap
        textPaint.textSize = minDimen * 0.18f
        subTextPaint.textSize = minDimen * 0.11f
    }

    // Çizim işlemleri
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val radius = (width.coerceAtMost(height) / 2f) - (basePaint.strokeWidth / 2f)
        val centerX = width / 2f
        val centerY = height / 2f

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

        // Yüzde metni
        val percentText = "%${(percentage * 100).toInt()}"
        val textY = centerY - (textPaint.descent() + textPaint.ascent()) / 2 - (height * 0.02f)
        canvas.drawText(percentText, centerX, textY, textPaint)

        // Pay/payda yazısı
        if (denominator > 0) {
            val fractionText = "$numerator/$denominator"
            val subTextY = textY + textPaint.textSize * 0.8f
            canvas.drawText(fractionText, centerX, subTextY, subTextPaint)
        }
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

    // İlerleme rengini değiştir
    fun setProgressColor(hexColor: String) {
        try {
            overlayPaint.color = Color.parseColor(hexColor)
            invalidate()
        } catch (e: IllegalArgumentException) {
            Log.e("ProgressCircleView", "Geçersiz progressColor: $hexColor")
        }
    }

    // Yüzdeyi animasyonla değiştir
    fun animatePercentage(target: Float, duration: Long = 1000) {
        val animator = ValueAnimator.ofFloat(0f, target.coerceIn(0f, 1f))
        animator.duration = duration
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            percentage = animation.animatedValue as Float
        }
        animator.start()
    }
}
