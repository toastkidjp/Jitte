package jp.toastkid.yobidashi.tab.tab_list

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.tab.TabThumbnails

/**
 * WebTab list adapter.
 * Initialize with context and so on...
 *
 * @param context
 * @param callback WebTab list model
 *
 * @author toastkidjp
 */
internal class Adapter(
        private val context: Context,
        private val callback: TabListDialogFragment.Callback
) : RecyclerView.Adapter<ViewHolder>() {

    /**
     * For getting Data binding object.
     */
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * For snackbar and view color.
     */
    private val colorPair: ColorPair = PreferenceApplier(context).colorPair()

    private val tabThumbnails = TabThumbnails { context }

    /**
     * Current index.
     */
    private var index = -1

    private var viewModel: TabListViewModel? = null

    init {
        if (context is FragmentActivity) {
            viewModel = ViewModelProviders.of(context)
                    .get(TabListViewModel::class.java)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                DataBindingUtil.inflate(inflater, R.layout.item_tab_list, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tab = callback.getTabByIndexFromTabList(position) ?: return
        holder.itemView.setOnClickListener {
            callback.replaceTabFromTabList(tab)
            callback.onCloseOnly()
        }

        holder.itemView.setOnLongClickListener {
            viewModel?.sendStartDrag(holder)
            Toaster.snackShort(
                    holder.itemView,
                    "Start to move tab \"${tab.title()}\".",
                    colorPair
            )
            return@setOnLongClickListener true
        }

        holder.setImagePath(tabThumbnails.assignNewFile(tab.thumbnailPath()).absolutePath)
        holder.setTitle(tab.title())
        holder.setCloseAction(View.OnClickListener { closeAt(callback.tabIndexOfFromTabList(tab)) })
        holder.setColor(colorPair)
        holder.setBackgroundColor(
                if (index == position) {
                    ColorUtils.setAlphaComponent(colorPair.bgColor(), 128)
                } else {
                    Color.TRANSPARENT
                }
        )
    }

    /**
     * Close tab at index.
     * @param position
     */
    private fun closeAt(position: Int) {
        callback.closeTabFromTabList(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount(): Int = callback.getTabAdapterSizeFromTabList()

    /**
     * Set current index.
     */
    fun setCurrentIndex(newIndex: Int) {
        index = newIndex
    }

    fun swap(from: Int, to: Int) {
        callback.swapTabsFromTabList(from, to)
        notifyItemMoved(from, to)
    }
}
