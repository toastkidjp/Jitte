package jp.toastkid.yobidashi.browser.history


import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.view.View
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemViewHistoryBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import timber.log.Timber
import java.io.File

/**
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemViewHistoryBinding)
    : RecyclerView.ViewHolder(binding.root) {

    fun setText(text: String, url: String, time: String) {
        binding.title.text = text
        binding.url.text   = url
        binding.time.text  = time
    }

    fun setImage(faviconPath: String): Disposable {
        if (faviconPath.isEmpty()) {
            setDefaultIcon()
            return Disposables.empty()
        }
        return Single.create<File>{ e -> e.onSuccess(File(faviconPath))}
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { ImageLoader.loadBitmap(binding.root.context, Uri.fromFile(it)) as Bitmap }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { binding.icon.setImageBitmap(it) },
                        { e ->
                            Timber.e(e)
                            setDefaultIcon()
                        }
                )
    }

    private fun setDefaultIcon() {
        binding.icon.setImageResource(R.drawable.ic_history_black)
    }

    fun setOnClickBookmark(history: ViewHistory) {
        binding.bookmark.setOnClickListener {
            val context = binding.root.context
            if (context is FragmentActivity) {
                AddBookmarkDialogFragment.make(history.title, history.url).show(
                        context.supportFragmentManager,
                        AddBookmarkDialogFragment::class.java.simpleName
                )
            }
        }
    }

    fun setOnClickAdd(history: ViewHistory, onClickAdd: (ViewHistory) -> Unit) {
        binding.delete.setOnClickListener { onClickAdd(history) }
    }

    fun switchDividerVisibility(visible: Boolean) {
        binding.divider.visibility = if (visible) { View.VISIBLE } else { View.GONE }
    }

}
