package jp.toastkid.yobidashi.search.url_suggestion

import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.browser.bookmark.model.BookmarkRepository
import jp.toastkid.yobidashi.browser.history.ViewHistoryRepository
import jp.toastkid.yobidashi.databinding.ModuleUrlSuggestionBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.view.RightSwipeActionAttachment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * @author toastkidjp
 */
class UrlSuggestionModule(
        private val binding: ModuleUrlSuggestionBinding,
        browseCallback: (String) -> Unit,
        browseBackgroundCallback: (String) -> Unit
) {

    /**
     * Adapter.
     */
    private val adapter = Adapter(
            LayoutInflater.from(binding.root.context),
            this::removeAt,
            browseCallback,
            browseBackgroundCallback
            )

    var enable = true

    /**
     * Use for disposing.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * Bookmark's database repository.
     */
    private val bookmarkRepository: BookmarkRepository =
            DatabaseFinder().invoke(binding.root.context).bookmarkRepository()

    /**
     * Database repository.
     */
    private val viewHistoryRepository: ViewHistoryRepository =
            DatabaseFinder().invoke(binding.root.context).viewHistoryRepository()

    init {
        binding.urlSuggestions.adapter = adapter
        binding.urlSuggestions.layoutManager =
                LinearLayoutManager(binding.root.context, RecyclerView.VERTICAL, false)
        RightSwipeActionAttachment()(
                binding.urlSuggestions,
                this::removeAt
        )
    }

    /**
     * Remove item.
     *
     * @param index
     */
    private fun removeAt(index: Int) {
        adapter.removeAt(viewHistoryRepository, index).addTo(disposables)
    }

    /**
     * Query to database.
     *
     * @param q query string
     */
    fun query(q: CharSequence) {
        adapter.clear()
        if (q.isEmpty()) {
            adapter.notifyDataSetChanged()
            hide()
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.IO) {
                bookmarkRepository.search("%$q%", ITEM_LIMIT).forEach { adapter.add(it) }
            }

            withContext(Dispatchers.IO) {
                viewHistoryRepository.search("%$q%", ITEM_LIMIT).forEach { adapter.add(it) }
            }

            if (adapter.isNotEmpty()) {
                show()
            } else {
                hide()
            }
            adapter.notifyDataSetChanged()
        }
    }

    /**
     * Show this module.
     */
    fun show() {
        if (!binding.root.isVisible && enable) {
            runOnMainThread { binding.root.isVisible = true }
                    .addTo(disposables)
        }
    }

    /**
     * Hide this module.
     */
    fun hide() {
        if (binding.root.isVisible) {
            runOnMainThread { binding.root.isVisible = false }
                    .addTo(disposables)
        }
    }

    private fun runOnMainThread(action: () -> Unit) =
            Completable.fromAction { action() }
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {},
                            Timber::e
                    )

    /**
     * Clear disposables.
     */
    fun dispose() {
        disposables.clear()
    }

    companion object {

        /**
         * Item limit.
         */
        private const val ITEM_LIMIT = 3
    }
}