package com.bangkit.naraspeak.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.bangkit.naraspeak.R

class CustomButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatButton(context, attrs) {

    private var button: Drawable = ContextCompat.getDrawable(context, R.drawable.button) as Drawable
    private var outlineButton: Drawable =
        ContextCompat.getDrawable(context, R.drawable.button_outline) as Drawable
    private var textColor: Int = 0
    private var textOutline: Int = 0

    init {
        textColor = ContextCompat.getColor(context, R.color.primary_1)
        textOutline = ContextCompat.getColor(context, R.color.accent_black)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        background = button
        setTextColor(textColor)

        isAllCaps = false
        height = 40

        if ((id == R.id.btn_login_google) || (id == R.id.btn_register_google)) {
            background = outlineButton
            setTextColor(textOutline)
        }

    }
}