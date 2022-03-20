package com.example.coursework

import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo

import android.view.inputmethod.InputConnection


class MyEditText : androidx.appcompat.widget.AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val conn = super.onCreateInputConnection(outAttrs)
        if (conn != null) {
            outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION.inv()
            return conn
        } else {
            throw Error("Input connection is null!")
        }
    }

    override fun onTextChanged(
        text: CharSequence?,
        start: Int,
        lengthBefore: Int,
        lengthAfter: Int
    ) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter)
        if (text?.contains("##") == true) {
            val newline = System.getProperty("line.separator")
            if (newline != null) {
                this.setText(text.toString().replace("##", newline))
            }
            text.lastIndex.let {
                this.setSelection(it)
            }
        }
    }
}