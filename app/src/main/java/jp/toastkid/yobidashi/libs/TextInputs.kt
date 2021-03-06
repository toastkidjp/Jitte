package jp.toastkid.yobidashi.libs

import android.content.Context
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.textfield.TextInputLayout
import jp.toastkid.yobidashi.R

/**
 * [TextInputLayout] utility.
 *
 * @author toastkidjp
 */
object TextInputs  {

    /**
     * EditText's layout params.
     */
    private val LAYOUT_PARAMS = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
    )

    /**
     * Set empty alert.
     *
     * @param inputLayout [TextInputLayout]
     */
    fun setEmptyAlert(inputLayout: TextInputLayout): EditText {
        val input: EditText? = inputLayout.editText

        input?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) = Unit

            override fun afterTextChanged(s: Editable) {
                if (s.toString().isEmpty()) {
                    inputLayout.error = inputLayout.context
                            .getString(R.string.favorite_search_addition_dialog_empty_message)
                    return
                }
                inputLayout.isErrorEnabled = false
            }
        })
        return input ?: EditText(inputLayout.context)
    }

    /**
     * Make [TextInputLayout] instance.
     *
     * @param context [Context] Use for make instance.
     */
    fun make(context: Context): TextInputLayout =
            TextInputLayout(context)
                .also { layout ->
                    layout.addView(
                            EditText(context).also {
                                it.maxLines   = 1
                                it.inputType  = InputType.TYPE_CLASS_TEXT
                                it.imeOptions = EditorInfo.IME_ACTION_SEARCH
                            },
                            0,
                            LAYOUT_PARAMS
                    )
                }
}
