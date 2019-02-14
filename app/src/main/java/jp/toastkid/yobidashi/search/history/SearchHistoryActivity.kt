package jp.toastkid.yobidashi.search.history

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.BaseActivity
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivitySearchHistoryBinding
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.db.DbInitializer
import jp.toastkid.yobidashi.search.SearchAction

/**
 * Search history list activity.
 *
 * @author toastkidjp
 */
class SearchHistoryActivity : BaseActivity(),
        SearchHistoryClearDialogFragment.OnClickSearchHistoryClearCallback {

    private lateinit var binding: ActivitySearchHistoryBinding

    private lateinit var adapter: ActivityAdapter

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        val relation = DbInitializer.init(this).relationOfSearchHistory()

        binding.historiesView.layoutManager =
                LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = ActivityAdapter(
                this,
                relation,
                { history -> SearchAction(this, history.category as String, history.query as String).invoke()},
                { history -> Toaster.snackShort(binding.root, history.query as String, colorPair()) }
        )
        binding.historiesView.adapter = adapter

        SwipeActionAttachment.invoke(binding.historiesView)

        initToolbar(binding.toolbar)
        binding.toolbar.inflateMenu(R.menu.search_history)
        binding.historiesView.scheduleLayoutAnimation()
    }

    override fun onResume() {
        super.onResume()

        if (adapter.itemCount == 0) {
            Toaster.tShort(this, getString(R.string.message_none_search_histories))
            finish()
            return
        }

        applyColorToToolbar(binding.toolbar)

        ImageLoader.setImageToImageView(binding.background, backgroundImagePath)
    }

    override fun clickMenu(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.close -> finish()
            R.id.clear -> SearchHistoryClearDialogFragment().show(
                    supportFragmentManager,
                    SearchHistoryClearDialogFragment::class.java.simpleName
            )
        }
        return true
    }

    override fun onClickSearchHistoryClear() {
        adapter.clearAll { Toaster.snackShort(binding.root, R.string.done_clear, colorPair()) }
                .addTo(disposables)
        finish()
    }

    override fun titleId(): Int = R.string.title_search_history

    companion object {

        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_search_history

        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, SearchHistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}