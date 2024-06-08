package com.bangkit.naraspeak.ui.customview

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import com.bangkit.naraspeak.R
import com.bangkit.naraspeak.helper.isValidEmail

class CustomEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs), View.OnTouchListener {

    init {
        setBackgroundResource(R.drawable.custom_text)
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                error = null

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                when {
                    inputType and InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                        if (!isValidEmail(s.toString())) {
                            error = "Please enter a correct E-mail address format"
                            setBackgroundResource(R.drawable.custom_text_error)
                        } else {
                            error = null
                            setBackgroundResource(R.drawable.custom_text)

                        }
                    }

                    inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD == InputType.TYPE_TEXT_VARIATION_PASSWORD -> {
                        if ((s?.length?:0) < 8) {
                            setError("Please enter more than 8 characters", null)
                            setBackgroundResource(R.drawable.custom_text_error)
                        } else {
                            error = null
                            setBackgroundResource(R.drawable.custom_text)
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean = true

}