package jp.toastkid.yobidashi.search.history

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import androidx.core.view.updateMargins
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemSearchHistoryBinding
import jp.toastkid.yobidashi.libs.view.SwipeViewHolder
import jp.toastkid.yobidashi.search.favorite.FavoriteSearchInsertion

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemSearchHistoryBinding)
    : RecyclerView.ViewHolder(binding.root), SwipeViewHolder {

    private val buttonMargin = binding.root.context.resources
            .getDimensionPixelSize(R.dimen.button_margin)

    fun setText(text: String) {
        binding.searchHistoryText.text = text
    }

    fun setImageRes(@DrawableRes iconId: Int) {
        binding.searchHistoryImage.setImageResource(iconId)
    }

    fun setOnClickAdd(history: SearchHistory, onClickAdd: (SearchHistory) -> Unit) {
        binding.searchHistoryAdd.setOnClickListener ({ _ ->
            onClickAdd(history)
        })
    }

    fun setOnClickDelete(onClick: () -> Unit) {
        binding.delete.setOnClickListener {
            onClick()
        }
    }

    fun switchDividerVisibility(visible: Boolean) {
        binding.divider.visibility = if (visible) { View.VISIBLE } else { View.GONE }
    }

    fun setFavorite(category: String, query: String) {
        binding.searchHistoryBookmark.setOnClickListener { v ->
            FavoriteSearchInsertion(v.context, category, query).invoke()
        }
    }

    fun setAddIcon(@DrawableRes addIcon: Int) {
        binding.searchHistoryAdd.setImageResource(addIcon)
    }

    fun hideAddButton() {
        binding.searchHistoryAdd.visibility = View.GONE
    }

    override fun getFrontView(): View = binding.front

    override fun isButtonVisible(): Boolean = binding.delete.isVisible

    override fun showButton() {
        binding.delete.visibility = View.VISIBLE
        updateRightMargin(buttonMargin)
    }

    override fun hideButton() {
        binding.delete.visibility = View.INVISIBLE
        updateRightMargin(0)
    }

    private fun updateRightMargin(margin: Int) {
        val marginLayoutParams = binding.front.layoutParams as ViewGroup.MarginLayoutParams
        marginLayoutParams.rightMargin = margin
        binding.front.layoutParams = marginLayoutParams
        marginLayoutParams.updateMargins()
    }
}
