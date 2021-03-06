package jp.toastkid.lib.view

import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils

/**
 * Color setter for [EditText].
 *
 * @author toastkidjp
 */
class EditTextColorSetter {

    /**
     * Set specified color to passed EditText.
     *
     * @param editText nullable
     * @param fontColor color int
     */
    operator fun invoke(editText: EditText?, @ColorInt fontColor: Int) {
        editText?.also {
            it.setTextColor(fontColor)
            it.setHintTextColor(ColorUtils.setAlphaComponent(fontColor, 196))
            it.highlightColor = ColorUtils.setAlphaComponent(fontColor, 128)
        }
    }

}
