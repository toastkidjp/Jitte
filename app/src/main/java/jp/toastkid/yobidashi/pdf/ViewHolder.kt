package jp.toastkid.yobidashi.pdf

import android.graphics.Bitmap
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import jp.toastkid.yobidashi.databinding.ItemPdfContentBinding

/**
 * PDF viewer's view holder
 *
 * @param binding [ItemPdfContentBinding]
 * @author toastkidjp
 */
class ViewHolder(val binding: ItemPdfContentBinding) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Set image bitmap.
     *
     * @param image [Bitmap]
     */
    fun setImage(image: Bitmap) {
        binding.image.setImageBitmap(image)
    }

    /**
     * Set indicator.
     *
     * @param current [Int]
     * @param max [Int]
     */
    fun setIndicator(current: Int, max: Int) {
        binding.pageIndicator.text = "$current / $max"
    }

    /**
     * Set long tap action.
     *
     * @param onLongClickListener [View.OnLongClickListener]
     */
    fun setOnLongTap(onLongClickListener: View.OnLongClickListener) {
        binding.image.setOnLongClickListener(onLongClickListener)
    }
}