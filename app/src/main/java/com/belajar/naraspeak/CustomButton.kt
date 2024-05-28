package com.belajar.naraspeak

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat

class CustomButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatButton(context, attrs) {

    private var button: Drawable = ContextCompat.getDrawable(context, R.drawable.button) as Drawable
    private var textColor: Int = 0

    init {
        textColor = ContextCompat.getColor(context, R.color.primary_1)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        background = button
        setTextColor(textColor)
        isAllCaps = false

    }
}