package jp.toastkid.yobidashi.browser.history

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.view.RecyclerViewScroller
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.page_search.PageSearcherViewModel
import jp.toastkid.yobidashi.databinding.FragmentViewHistoryBinding
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import jp.toastkid.yobidashi.search.history.SwipeActionAttachment

/**
 * @author toastkidjp
 */
class ViewHistoryFragment: Fragment(), ClearDialogFragment.Callback, ContentScrollable {

    private lateinit var binding: FragmentViewHistoryBinding

    private lateinit var adapter: ActivityAdapter

    private lateinit var preferenceApplier: PreferenceApplier

    private var contentViewModel: ContentViewModel? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val context = requireContext()
        preferenceApplier = PreferenceApplier(context)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)

        binding.historiesView.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        contentViewModel = ViewModelProvider(requireActivity()).get(ContentViewModel::class.java)

        adapter = ActivityAdapter(
                context,
                DatabaseFinder().invoke(context).viewHistoryRepository(),
                { history -> finishWithResult(Uri.parse(history.url)) },
                { history -> contentViewModel?.snackShort(history.title) }
        )

        binding.historiesView.adapter = adapter
        SwipeActionAttachment().invoke(binding.historiesView)
        
        setHasOptionsMenu(true)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentActivity = activity ?: return
        ViewModelProvider(fragmentActivity).get(PageSearcherViewModel::class.java)
                .find
                .observe(fragmentActivity, Observer {
                    val text = it?.getContentIfNotHandled() ?: return@Observer
                    adapter.filter(text)
                })
    }

    private fun finishWithResult(uri: Uri?) {
        if (uri == null) {
            return
        }

        val browserViewModel =
                ViewModelProvider(requireActivity()).get(BrowserViewModel::class.java)

        popBackStack()
        browserViewModel.open(uri)
    }

    private fun popBackStack() {
        activity?.supportFragmentManager?.popBackStack()
    }

    override fun onResume() {
        super.onResume()

        adapter.refresh {
            if (adapter.itemCount != 0) {
                return@refresh
            }

            contentViewModel?.snackShort(R.string.message_none_search_histories)
            popBackStack()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.view_history, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear -> {
                val clearDialogFragment = ClearDialogFragment()
                clearDialogFragment.setTargetFragment(this, clearDialogFragment.id)
                clearDialogFragment.show(
                        parentFragmentManager,
                        ClearDialogFragment::class.java.simpleName
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClickClear() {
        adapter.clearAll{ contentViewModel?.snackShort(R.string.done_clear)}
        popBackStack()
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.historiesView, adapter.itemCount)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.historiesView, adapter.itemCount)
    }

    override fun onDetach() {
        super.onDetach()
        adapter.dispose()
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_view_history

    }
}