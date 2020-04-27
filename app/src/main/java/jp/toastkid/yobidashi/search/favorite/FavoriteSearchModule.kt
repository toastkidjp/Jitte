package jp.toastkid.yobidashi.search.favorite

import android.os.Handler
import android.os.Looper
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.databinding.ModuleSearchFavoriteBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.libs.view.RightSwipeActionAttachment
import timber.log.Timber

/**
 * Search history module.
 *
 * @param binding Data binding object
 * @param searchCallback
 * @param onTouch
 * @param onClickAdd
 * @author toastkidjp
 */
class FavoriteSearchModule(
        private val binding: ModuleSearchFavoriteBinding,
        searchCallback: (FavoriteSearch) -> Unit,
        onTouch: () -> Unit,
        onClickAdd: (FavoriteSearch) -> Unit
) {

    /**
     * RecyclerView's moduleAdapter.
     */
    private val moduleAdapter: ModuleAdapter

    /**
     * Database repository.
     */
    private val repository: FavoriteSearchRepository

    var enable: Boolean = true

    /**
     * Last subscription.
     */
    private var disposable: Disposable? = null

    /**
     * Use for disposing.
     */
    private val disposables: CompositeDisposable = CompositeDisposable()

    /**
     * For executing on UI thread.
     */
    private val uiThreadHandler = Handler(Looper.getMainLooper())

    init {

        binding.module = this

        val context = binding.root.context
        repository = DatabaseFinder().invoke(context).favoriteSearchRepository()

        binding.searchFavorites.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        moduleAdapter = ModuleAdapter(
                context,
                repository,
                searchCallback,
                { visible -> if (visible) { show() } else { hide() } },
                { history -> onClickAdd(history) },
                5
        )
        binding.searchFavorites.adapter = moduleAdapter
        binding.searchFavorites.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                onTouch()
                return false
            }
        }
        uiThreadHandler.post {
            RightSwipeActionAttachment()(binding.searchFavorites) {
                moduleAdapter.removeAt(it).addTo(disposables)
            }
        }
    }

    /**
     * Query table with passed word.
     * @param s
     */
    fun query(s: CharSequence) {
        disposable?.dispose()
        disposable = moduleAdapter.query(s)
    }

    /**
     * Confirm clear search history.
     */
    fun confirmClear() {
        val activityContext = binding.root.context
        if (activityContext is FragmentActivity) {
            ClearFavoriteSearchDialogFragment().show(
                    activityContext.supportFragmentManager,
                    ClearFavoriteSearchDialogFragment::class.java.simpleName
                    )
        }
    }

    fun clear() {
        moduleAdapter.clear()
        hide()
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
     * Dispose last subscription.
     */
    fun dispose() {
        disposable?.dispose()
        disposables.clear()
    }

}
