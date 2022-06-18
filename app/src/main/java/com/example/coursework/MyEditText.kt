package com.example.coursework

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection


/**
 * My custom extension of EditText class, allowing to easily configure action buttons
 * while these fields are selected, their bounds, and newline syntax.
 *
 */
class MyEditText : androidx.appcompat.widget.AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    /**
     * Function, which make it simpler to observe text changes in input field.
     *
     * @param afterTextChanged
     * Function, which needs to be executed with the text when it is changed.
     */
    fun afterTextChanged(afterTextChanged: (String) -> Unit) {
        this.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                afterTextChanged.invoke(editable.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    /**
     * Function, allowing to add custom ime Options to text field without overwrites.
     *
     * @param outAttrs
     * Object with ime options we need to add.
     * @return InputConnection object
     */
    override fun onCreateInputConnection(outAttrs: EditorInfo): InputConnection {
        val conn = super.onCreateInputConnection(outAttrs)
        if (conn != null) {
            outAttrs.imeOptions = outAttrs.imeOptions and EditorInfo.IME_FLAG_NO_ENTER_ACTION.inv()
            return conn
        } else {
            throw Error("Input connection is null!")
        }
    }

    /**
     * Function which observes changes in text and make some actions to it.
     *
     * @receiver
     * Adds opportunity to make a new line if two '#' symbols are entered sequentially.
     *
     * @param text
     * whole text.
     * @param start
     * start of changed text position.
     * @param lengthBefore
     * Old length of the text.
     * @param lengthAfter
     * New length of the text.
     */
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