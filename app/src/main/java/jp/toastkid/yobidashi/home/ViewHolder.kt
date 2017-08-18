package jp.toastkid.yobidashi.home

import android.support.annotation.DrawableRes
import android.support.annotation.StringRes
import android.support.v7.widget.RecyclerView
import android.view.View
import jp.toastkid.yobidashi.databinding.ItemHomeMenuBinding
import jp.toastkid.yobidashi.libs.preference.ColorPair

/**
 * Menu's view holder.

 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemHomeMenuBinding) : RecyclerView.ViewHolder(binding.root) {

    fun setText(@StringRes titleId: Int) {
        binding.text.setText(titleId)
    }

    fun setImage(@DrawableRes iconId: Int) {
        binding.image.setImageResource(iconId)
    }

    fun setColorPair(pair: ColorPair) {
        itemView.setBackgroundColor(pair.bgColor())
        binding.text.setTextColor(pair.fontColor())
        binding.image.setColorFilter(pair.fontColor())
    }

    fun setOnClick(onClick: View.OnClickListener) {
        itemView.setOnClickListener(onClick)
    }
}
