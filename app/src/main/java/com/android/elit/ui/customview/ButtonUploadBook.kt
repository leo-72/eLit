package com.android.elit.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import com.android.elit.R

class ButtonUploadBook : AppCompatButton {
    private lateinit var enabledBg: Drawable
    private lateinit var disabledBg: Drawable
    private var txtColor: Int = 1

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        background = if (isEnabled) enabledBg else disabledBg

        setTextColor(ContextCompat.getColor(context, R.color.white))
        textSize = 14f
        gravity = Gravity.CENTER
        text = if (isEnabled) {
            context.getString(R.string.tv_upload_book)
        } else {
            context.getString(R.string.tv_upload_book)
        }
    }

    private fun init() {
        txtColor = ContextCompat.getColor(context, R.color.white)
        enabledBg = ContextCompat.getDrawable(context, R.drawable.bg_btn_enable) as Drawable
        disabledBg = ContextCompat.getDrawable(context, R.drawable.bg_btn_disable) as Drawable
    }
}