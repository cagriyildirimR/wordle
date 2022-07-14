package com.example.wordle

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

const val TITLE_RATIO = 0.1f
const val BAR_RATIO = 0.9f

class DistributionView(val ctx: Context, attrs: AttributeSet) : View(ctx, attrs) {
    var title: String = "0"
    private val titleRect = Rect()
    var countText = "0"
    private val countRect = Rect()
    var percentage = 0.0f
    private var titleX = 0f
    private var titleY = 0f
    private var barRect = Rect()
    private var countX = 0f


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 48f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.apply {
            color = ctx.getColor(R.color.black)
        }
        canvas.drawText(title, titleX, titleY, paint)
        paint.apply {
            color =
                if (countText == "0") ctx.getColor(R.color.dark_gray) else ctx.getColor(R.color.green)
            style = Paint.Style.FILL
        }
        canvas.drawRect(barRect, paint)

        paint.apply {
            color = ctx.getColor(R.color.white)
        }
        canvas.drawText(countText, countX, titleY, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        paint.getTextBounds(title, 0, 1, titleRect)
        val titleWidth = measuredWidth * 0.1f
        titleX = (titleWidth - titleRect.width()) / 2
        titleY = (measuredHeight.toFloat() + titleRect.height()) / 2

        var w = (measuredWidth - titleWidth) * percentage + titleWidth
        w = if (w < 2 * titleWidth) 2 * titleWidth else w

        barRect = Rect(titleWidth.toInt(), 0, w.toInt(), measuredHeight)

        paint.getTextBounds(countText, 0, countText.length, countRect)
        countX = w - countRect.width() - titleX
    }

}