package jp.toastkid.yobidashi.libs

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.support.annotation.ColorInt
import android.support.v4.graphics.ColorUtils
import android.widget.EditText
import android.widget.TextView
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * Color utilities.
 *
 * @author toastkidjp
 */
object Colors {

    /**
     * Set specified color to passed EditText.
     *
     * @param editText
     * @param fontColor
     */
    fun setEditTextColor(editText: EditText, @ColorInt fontColor: Int) {
        editText.setTextColor(fontColor)
        editText.setHintTextColor(fontColor)
        editText.highlightColor = ColorUtils.setAlphaComponent(fontColor, 128)
    }

    /**
     * Set background and text color.
     *
     * @param tv
     * @param pair
     */
    fun setColors(tv: TextView, pair: ColorPair) {
        tv.setBackgroundColor(pair.bgColor())
        val fontColor = pair.fontColor()
        tv.setTextColor(fontColor)
        tv.compoundDrawables?.forEach {
            it?.colorFilter = PorterDuffColorFilter(fontColor, PorterDuff.Mode.SRC_IN)
        }
    }
}
