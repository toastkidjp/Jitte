package jp.toastkid.yobidashi.main

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.ads.MobileAds
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import jp.toastkid.article_viewer.article.detail.ContentViewerFragment
import jp.toastkid.lib.AppBarViewModel
import jp.toastkid.lib.BrowserViewModel
import jp.toastkid.lib.ContentScrollable
import jp.toastkid.lib.ContentViewModel
import jp.toastkid.lib.FileExtractorFromUri
import jp.toastkid.lib.TabListViewModel
import jp.toastkid.lib.Urls
import jp.toastkid.lib.permission.RuntimePermissions
import jp.toastkid.lib.preference.ColorPair
import jp.toastkid.lib.preference.PreferenceApplier
import jp.toastkid.lib.tab.TabUiFragment
import jp.toastkid.lib.view.ToolbarColorApplier
import jp.toastkid.search.SearchCategory
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.about.AboutThisAppFragment
import jp.toastkid.yobidashi.barcode.BarcodeReaderFragment
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.LoadingViewModel
import jp.toastkid.yobidashi.browser.ScreenMode
import jp.toastkid.yobidashi.browser.bookmark.BookmarkFragment
import jp.toastkid.yobidashi.browser.floating.FloatingPreview
import jp.toastkid.yobidashi.browser.page_search.PageSearcherModule
import jp.toastkid.yobidashi.browser.webview.GlobalWebViewPool
import jp.toastkid.yobidashi.databinding.ActivityMainBinding
import jp.toastkid.yobidashi.editor.EditorFragment
import jp.toastkid.yobidashi.launcher.LauncherFragment
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.clip.ClippingUrlOpener
import jp.toastkid.yobidashi.libs.image.BackgroundImageLoaderUseCase
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.menu.MenuBinder
import jp.toastkid.yobidashi.menu.MenuUseCase
import jp.toastkid.yobidashi.menu.MenuViewModel
import jp.toastkid.yobidashi.pdf.PdfViewerFragment
import jp.toastkid.yobidashi.search.SearchAction
import jp.toastkid.yobidashi.search.SearchFragment
import jp.toastkid.yobidashi.search.clip.SearchWithClip
import jp.toastkid.yobidashi.search.favorite.AddingFavoriteSearchService
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.SettingFragment
import jp.toastkid.yobidashi.settings.fragment.OverlayColorFilterViewModel
import jp.toastkid.yobidashi.tab.TabAdapter
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.tab_list.TabListClearDialogFragment
import jp.toastkid.yobidashi.tab.tab_list.TabListDialogFragment
import jp.toastkid.yobidashi.tab.tab_list.TabListService
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Main of this calendar app.
 *
 * @author toastkidjp
 */
class MainActivity : AppCompatActivity(),
        TabListClearDialogFragment.Callback,
        TabListDialogFragment.Callback
{

    /**
     * Data binding object.
     */
    private lateinit var binding: ActivityMainBinding

    private val backgroundImageLoaderUseCase by lazy { BackgroundImageLoaderUseCase() }

    /**
     * Disposables.
     */
    private val disposables: Job by lazy { Job() }

    /**
     * Find-in-page module.
     */
    private lateinit var pageSearchPresenter: PageSearcherModule

    private lateinit var tabReplacingUseCase: TabReplacingUseCase

    /**
     * Menu's view model.
     */
    private var menuViewModel: MenuViewModel? = null

    private var contentViewModel: ContentViewModel? = null

    private var tabListViewModel: TabListViewModel? = null

    private var browserViewModel: BrowserViewModel? = null

    private var floatingPreview: FloatingPreview? = null

    private lateinit var tabs: TabAdapter

    /**
     * Search-with-clip object.
     */
    private lateinit var searchWithClip: SearchWithClip

    /**
     * Runtime permission.
     */
    private var runtimePermissions: RuntimePermissions? = null

    /**
     * Preferences wrapper.
     */
    private lateinit var preferenceApplier: PreferenceApplier

    private lateinit var menuUseCase: MenuUseCase

    private var tabListService: TabListService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoActionBar)
        setContentView(LAYOUT_ID)

        CoroutineScope(Dispatchers.IO).launch(disposables) {
            MobileAds.initialize(this@MainActivity) {}
        }

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)

        setSupportActionBar(binding.toolbar)

        runtimePermissions = RuntimePermissions(this)

        val colorPair = preferenceApplier.colorPair()

        pageSearchPresenter = PageSearcherModule(binding.sip)

        initializeHeaderViewModel()

        initializeMenuViewModel()

        initializeContentViewModel()

        val activityViewModelProvider = ViewModelProvider(this)
        browserViewModel = activityViewModelProvider.get(BrowserViewModel::class.java)
        browserViewModel?.preview?.observe(this, Observer {
            Inputs.hideKeyboard(binding.content)

            if (floatingPreview == null) {
                floatingPreview = FloatingPreview(this)
            }
            floatingPreview?.show(binding.root, it.toString())
        })
        browserViewModel?.open?.observe(this, Observer(::openNewWebTab))
        browserViewModel?.openBackground?.observe(this, Observer {
            tabs.openBackgroundTab(it.toString(), it.toString())
            Toaster.snackShort(
                    binding.content,
                    getString(R.string.message_tab_open_background, it.toString()),
                    preferenceApplier.colorPair()
            )
        })
        browserViewModel?.openBackgroundWithTitle?.observe(this, Observer {
            tabs.openBackgroundTab(it.first, it.second.toString())
            Toaster.snackShort(
                    binding.content,
                    getString(R.string.message_tab_open_background, it.first),
                    preferenceApplier.colorPair()
            )
        })

        invokeSearchWithClip(colorPair)

        activityViewModelProvider.get(LoadingViewModel::class.java)
                .onPageFinished
                .observe(
                        this,
                        Observer {
                            tabs.updateWebTab(it)
                            if (tabs.currentTabId() == it.first) {
                                refreshThumbnail()
                            }
                        }
                )

        activityViewModelProvider.get(OverlayColorFilterViewModel::class.java)
                .newColor
                .observe(this, Observer {
                    updateColorFilter()
                })

        tabListViewModel = activityViewModelProvider.get(TabListViewModel::class.java)
        tabListViewModel
                ?.saveEditorTab
                ?.observe(
                        this,
                        Observer {
                            val currentTab = tabs.currentTab() as? EditorTab ?: return@Observer
                            currentTab.setFileInformation(it)
                            tabs.saveTabList()
                        }
                )

        tabs = TabAdapter({ this }, this::onEmptyTabs)

        tabReplacingUseCase = TabReplacingUseCase(
                tabs,
                ::obtainFragment,
                { fragment, animation -> replaceFragment(fragment, animation) },
                ::refreshThumbnail,
                { runOnUiThread(it) },
                disposables
        )

        processShortcut(intent)

        supportFragmentManager.addOnBackStackChangedListener {
            val findFragment = findFragment()

            if (findFragment !is TabUiFragment && supportFragmentManager.backStackEntryCount == 0) {
                finish()
            }
        }
    }

    private fun invokeSearchWithClip(colorPair: ColorPair) {
        searchWithClip = SearchWithClip(
                applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager,
                binding.content,
                colorPair,
                browserViewModel
        )
        searchWithClip.invoke()
    }

    private fun obtainFragment(fragmentClass: Class<out Fragment>) =
            supportFragmentManager.findFragmentByTag(fragmentClass.canonicalName)
                    ?: fragmentClass.newInstance()

    private fun initializeHeaderViewModel() {
        val headerViewModel = ViewModelProvider(this).get(AppBarViewModel::class.java)
        headerViewModel.content.observe(this, Observer { view ->
            if (view == null) {
                return@Observer
            }
            binding.toolbarContent.removeAllViews()

            if (view.parent != null) {
                (view.parent as? ViewGroup)?.removeAllViews()
            }

            if (view.layoutParams != null) {
                binding.toolbar.layoutParams.height = view.layoutParams.height
            }
            binding.toolbarContent.addView(view, 0)
        })

        headerViewModel.visibility.observe(this, Observer { isVisible ->
            if (isVisible) showToolbar() else hideToolbar()
        })
    }

    private fun initializeMenuViewModel() {
        menuViewModel = ViewModelProvider(this).get(MenuViewModel::class.java)

        MenuBinder(this, menuViewModel, binding.menuStub, binding.menuSwitch)

        menuUseCase = MenuUseCase({ this }, menuViewModel)
    }

    private fun initializeContentViewModel() {
        contentViewModel = ViewModelProvider(this).get(ContentViewModel::class.java)
        contentViewModel?.fragmentClass?.observe(this, Observer {
            replaceFragment(obtainFragment(it), withAnimation = true, withSlideIn = true)
        })
        contentViewModel?.fragment?.observe(this, Observer {
            replaceFragment(it, withAnimation = true, withSlideIn = false)
        })
        contentViewModel?.snackbar?.observe(this, Observer {
            val snackbarEvent = it.getContentIfNotHandled() ?: return@Observer
            if (snackbarEvent.actionLabel == null) {
                Toaster.snackShort(
                        binding.content,
                        snackbarEvent.message,
                        preferenceApplier.colorPair()
                )
                return@Observer
            }

            Toaster.withAction(
                    binding.content,
                    snackbarEvent.message,
                    snackbarEvent.actionLabel ?: "",
                    View.OnClickListener { snackbarEvent.action() },
                    preferenceApplier.colorPair()
            )
        })
        contentViewModel?.snackbarRes?.observe(this, Observer {
            Toaster.snackShort(binding.content, it, preferenceApplier.colorPair())
        })
        contentViewModel?.toTop?.observe(this, Observer {
            (findFragment() as? ContentScrollable)?.toTop()
        })
        contentViewModel?.toBottom?.observe(this, Observer {
            (findFragment() as? ContentScrollable)?.toBottom()
        })
        contentViewModel?.share?.observe(this, Observer {
            if (it.hasBeenHandled) {
                return@Observer
            }
            it.getContentIfNotHandled()
            (findFragment() as? CommonFragmentAction)?.share()
        })
        contentViewModel?.webSearch?.observe(this, Observer {
            when (val fragment = findFragment()) {
                is BrowserFragment ->
                    fragment.search()
                else ->
                    contentViewModel?.nextFragment(SearchFragment::class.java)
            }
        })
        contentViewModel?.openPdf?.observe(this, Observer {
            openPdfTabFromStorage()
        })
        contentViewModel?.openEditorTab?.observe(this, Observer {
            openEditorTab()
        })
        contentViewModel?.switchPageSearcher?.observe(this, Observer {
            pageSearchPresenter.switch()
        })
        contentViewModel?.switchTabList?.observe(this, Observer {
            switchTabList()
        })
        contentViewModel?.refresh?.observe(this, Observer {
            refresh()
        })
        contentViewModel?.newArticle?.observe(this, Observer {
            val titleAndOnBackground = it?.getContentIfNotHandled() ?: return@Observer
            tabs.openNewArticleTab(titleAndOnBackground.first, titleAndOnBackground.second)
            if (titleAndOnBackground.second) {
                contentViewModel?.snackShort(
                        getString(R.string.message_tab_open_background, titleAndOnBackground.first)
                )
            } else {
                replaceToCurrentTab()
            }
        })
    }

    override fun onNewIntent(passedIntent: Intent) {
        super.onNewIntent(passedIntent)
        processShortcut(passedIntent)
    }

    /**
     * Process intent shortcut.
     *
     * @param calledIntent
     */
    private fun processShortcut(calledIntent: Intent) {
        if (calledIntent.getBooleanExtra("random_wikipedia", false)) {
            RandomWikipedia().fetchWithAction { title, uri ->
                openNewWebTab(uri)
                Toaster.snackShort(
                        binding.content,
                        getString(R.string.message_open_random_wikipedia, title),
                        preferenceApplier.colorPair()
                )
            }
            return
        }

        when (calledIntent.action) {
            Intent.ACTION_VIEW -> {
                val uri = calledIntent.data ?: return
                when (uri.scheme) {
                    "content" -> openEditorTab(FileExtractorFromUri(this, uri))
                    else -> openNewWebTab(uri)
                }
                return
            }
            Intent.ACTION_SEND -> {
                calledIntent.extras?.getCharSequence(Intent.EXTRA_TEXT)?.also {
                    val query = it.toString()
                    if (Urls.isInvalidUrl(query)) {
                        search(preferenceApplier.getDefaultSearchEngine()
                                ?: SearchCategory.getDefaultCategoryName(), query)
                        return
                    }
                    openNewWebTab(query.toUri())
                }
                return
            }
            Intent.ACTION_WEB_SEARCH -> {
                val category = if (calledIntent.hasExtra(AddingFavoriteSearchService.EXTRA_KEY_CATEGORY)) {
                    calledIntent.getStringExtra(AddingFavoriteSearchService.EXTRA_KEY_CATEGORY)
                } else {
                    preferenceApplier.getDefaultSearchEngine() ?: SearchCategory.getDefaultCategoryName()
                }
                search(category, calledIntent.getStringExtra(SearchManager.QUERY))
                return
            }
            BOOKMARK -> {
                replaceFragment(obtainFragment(BookmarkFragment::class.java))
            }
            APP_LAUNCHER -> {
                replaceFragment(obtainFragment(LauncherFragment::class.java))
            }
            BARCODE_READER -> {
                replaceFragment(obtainFragment(BarcodeReaderFragment::class.java))
            }
            SEARCH -> {
                replaceFragment(obtainFragment(SearchFragment::class.java))
            }
            SETTING -> {
                replaceFragment(obtainFragment(SettingFragment::class.java))
            }
            else -> {
                if (tabs.isEmpty()) {
                    openNewTab()
                    return
                }

                // Add for re-creating activity.
                val currentFragment = supportFragmentManager.findFragmentById(R.id.content)
                if (currentFragment is TabUiFragment || currentFragment == null) {
                    replaceToCurrentTab(false)
                }
            }
        }
    }

    private fun search(category: String?, query: String?) {
        if (category.isNullOrEmpty() || query.isNullOrEmpty()) {
            return
        }

        SearchAction(this, category, query).invoke()
    }

    private fun openNewWebTab(uri: Uri) {
        tabs.openNewWebTab(uri.toString())
        replaceToCurrentTab(true)
    }

    /**
     * Replace with passed fragment.
     *
     * @param fragment {@link BaseFragment} instance
     */
    private fun replaceFragment(
            fragment: Fragment,
            withAnimation: Boolean = true,
            withSlideIn: Boolean = false
    ) {
        val currentFragment = findFragment()
        if (currentFragment == fragment) {
            return
        }

        val fragments = supportFragmentManager.fragments
        if (fragments.size != 0 && fragments.contains(fragment)) {
            fragments.remove(fragment)
        }

        val transaction = supportFragmentManager.beginTransaction()
        if (withAnimation) {
            transaction.setCustomAnimations(
                    if (withSlideIn) R.anim.slide_in_right else R.anim.slide_up,
                    0,
                    0,
                    if (withSlideIn) android.R.anim.slide_out_right else R.anim.slide_down
            )
        }

        transaction.replace(R.id.content, fragment, fragment::class.java.canonicalName)

        if (fragment !is TabUiFragment) {
            transaction.addToBackStack(fragment::class.java.canonicalName)
        }
        transaction.commitAllowingStateLoss()
    }

    /**
     * Replace visibilities for current tab.
     *
     * @param withAnimation for suppress redundant animation.
     */
    private fun replaceToCurrentTab(withAnimation: Boolean = true) {
        tabReplacingUseCase.invoke(withAnimation)
    }

    private fun refreshThumbnail() {
        CoroutineScope(Dispatchers.Default).launch(disposables) {
            runOnUiThread {
                val findFragment = findFragment()
                if (findFragment !is TabUiFragment) {
                    return@runOnUiThread
                }
                tabs.saveNewThumbnail(binding.content)
            }
        }
    }

    override fun onBackPressed() {
        if (tabListService?.onBackPressed() == true) {
            return
        }

        if (binding.menuStub.root?.isVisible == true) {
            menuViewModel?.close()
            return
        }

        if (pageSearchPresenter.isVisible()) {
            pageSearchPresenter.hide()
            return
        }

        if (floatingPreview?.isVisible() == true) {
            floatingPreview?.hide()
            return
        }

        val currentFragment = findFragment()
        if (currentFragment is CommonFragmentAction && currentFragment.pressBack()) {
            return
        }

        if (currentFragment is BrowserFragment
                || currentFragment is PdfViewerFragment
                || currentFragment is ContentViewerFragment
        ) {
            tabs.closeTab(tabs.index())

            if (tabs.isEmpty()) {
                onEmptyTabs()
                return
            }
            replaceToCurrentTab(true)
            return
        }

        val fragment = findFragment()
        if (fragment !is EditorFragment) {
            supportFragmentManager.popBackStackImmediate()
            return
        }

        confirmExit()
    }

    private fun findFragment() = supportFragmentManager.findFragmentById(R.id.content)

    /**
     * Show confirm exit.
     */
    private fun confirmExit() {
        CloseDialogFragment()
                .show(supportFragmentManager, CloseDialogFragment::class.java.simpleName)
    }

    override fun onResume() {
        super.onResume()
        refresh()
        menuViewModel?.onResume()
        floatingPreview?.onResume()

        tabs.setCount()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        ClippingUrlOpener(binding.content) { browserViewModel?.open(it) }
    }

    /**
     * Refresh toolbar and background.
     */
    private fun refresh() {
        val colorPair = preferenceApplier.colorPair()
        ToolbarColorApplier()(window, binding.toolbar, colorPair)
        binding.toolbar.backgroundTint = ColorStateList.valueOf(colorPair.bgColor())

        backgroundImageLoaderUseCase.invoke(binding.background, preferenceApplier.backgroundImagePath)

        updateColorFilter()
    }

    private fun updateColorFilter() {
        binding.foreground.foreground =
                if (preferenceApplier.useColorFilter()) ColorDrawable(preferenceApplier.filterColor(ContextCompat.getColor(this, R.color.default_color_filter)))
                else null
    }

    /**
     * Open PDF from storage.
     */
    private fun openPdfTabFromStorage() {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            runtimePermissions
                    ?.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ?.receiveAsFlow()
                    ?.collect { permission ->
                        if (!permission.granted) {
                            return@collect
                        }

                        startActivityForResult(
                                IntentFactory.makeOpenDocument("application/pdf"),
                                REQUEST_CODE_OPEN_PDF
                        )
                    }
        }
    }

    /**
     * Open Editor tab.
     */
    private fun openEditorTab(path: String? = null) {
        CoroutineScope(Dispatchers.Main).launch(disposables) {
            runtimePermissions
                    ?.request(Manifest.permission.READ_EXTERNAL_STORAGE)
                    ?.receiveAsFlow()
                    ?.collect { permission ->
                        if (!permission.granted) {
                            return@collect
                        }

                        tabs.openNewEditorTab(path)
                        replaceToCurrentTab()
                    }
        }
    }

    private fun hideToolbar() {
        when (ScreenMode.find(preferenceApplier.browserScreenMode())) {
            ScreenMode.FIXED -> Unit
            ScreenMode.FULL_SCREEN -> {
                binding.toolbar.visibility = View.GONE
            }
            ScreenMode.EXPANDABLE -> {
                binding.toolbar.animate()?.let {
                    it.cancel()
                    it.translationY(-resources.getDimension(R.dimen.toolbar_height))
                            .setDuration(HEADER_HIDING_DURATION)
                            .withStartAction { binding.content.requestLayout() }
                            .withEndAction   {
                                binding.toolbar.visibility = View.GONE
                            }
                            .start()
                }
            }
        }
    }

    private fun showToolbar() {
        when (ScreenMode.find(preferenceApplier.browserScreenMode())) {
            ScreenMode.FIXED -> {
                binding.toolbar.visibility = View.VISIBLE
            }
            ScreenMode.FULL_SCREEN -> Unit
            ScreenMode.EXPANDABLE -> binding.toolbar.animate()?.let {
                it.cancel()
                it.translationY(0f)
                        .setDuration(HEADER_HIDING_DURATION)
                        .withStartAction {
                            binding.toolbar.visibility = View.VISIBLE
                        }
                        .withEndAction   { binding.content.requestLayout() }
                        .start()
            }
        }
    }

    /**
     * Switch tab list visibility.
     */
    private fun switchTabList() {
        if (tabListService == null) {
            tabListService = TabListService(supportFragmentManager, this::refreshThumbnail)
        }
        tabListService?.switch()
    }

    /**
     * Action on empty tabs.
     */
    private fun onEmptyTabs() {
        tabListService?.dismiss()
        openNewTab()
    }

    override fun onClickClear() {
        tabs.clear()
        onEmptyTabs()
    }

    override fun onCloseOnly() {
        tabListService?.dismiss()
    }

    override fun onCloseTabListDialogFragment(lastTabId: String) {
        if (lastTabId != tabs.currentTabId()) {
            replaceToCurrentTab()
        }
    }

    override fun onOpenEditor() = openEditorTab()

    override fun onOpenPdf() = openPdfTabFromStorage()

    override fun openNewTabFromTabList() {
        openNewTab()
    }

    private fun openNewTab() {
        when (StartUp.findByName(preferenceApplier.startUp)) {
            StartUp.SEARCH -> {
                replaceFragment(obtainFragment(SearchFragment::class.java))
            }
            StartUp.BROWSER -> {
                tabs.openNewWebTab()
                replaceToCurrentTab(true)
            }
            StartUp.BOOKMARK -> {
                replaceFragment(obtainFragment(BookmarkFragment::class.java))
            }
        }
    }

    override fun tabIndexFromTabList() = tabs.index()

    override fun currentTabIdFromTabList() = tabs.currentTabId()

    override fun replaceTabFromTabList(tab: Tab) {
        tabs.replace(tab)
        (obtainFragment(BrowserFragment::class.java) as? BrowserFragment)?.stopSwipeRefresherLoading()
    }

    override fun getTabByIndexFromTabList(position: Int): Tab? = tabs.getTabByIndex(position)

    override fun closeTabFromTabList(position: Int) {
        tabs.closeTab(position)
        (obtainFragment(BrowserFragment::class.java) as? BrowserFragment)?.stopSwipeRefresherLoading()
    }

    override fun getTabAdapterSizeFromTabList(): Int = tabs.size()

    override fun swapTabsFromTabList(from: Int, to: Int) = tabs.swap(from, to)

    override fun tabIndexOfFromTabList(tab: Tab): Int = tabs.indexOf(tab)

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.main_fab_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.open_tabs -> {
            switchTabList()
            true
        }
        R.id.setting -> {
            replaceFragment(obtainFragment(SettingFragment::class.java))
            true
        }
        R.id.reset_menu_position -> {
            menuViewModel?.resetPosition()
            true
        }
        R.id.about_this_app -> {
            replaceFragment(obtainFragment(AboutThisAppFragment::class.java))
            true
        }
        R.id.menu_exit -> {
            finish()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            return
        }
        when (requestCode) {
            IntentIntegrator.REQUEST_CODE -> {
                val result: IntentResult? =
                        IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
                if (result?.contents == null) {
                    Toaster.snackShort(binding.content, "Cancelled", preferenceApplier.colorPair())
                    return
                }
                Toaster.snackLong(
                        binding.content,
                        "Scanned: ${result.contents}",
                        R.string.clip,
                        View.OnClickListener { Clipboard.clip(this, result.contents) },
                        preferenceApplier.colorPair()
                )
            }
            REQUEST_CODE_OPEN_PDF -> {
                val uri = data.data ?: return
                val takeFlags: Int = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver?.takePersistableUriPermission(uri, takeFlags)

                tabs.openNewPdfTab(uri)
                replaceToCurrentTab(true)
                tabListService?.dismiss()
            }
            VoiceSearch.REQUEST_CODE -> {
                VoiceSearch.processResult(this, data)
            }
        }
    }

    /**
     * Workaround appcompat-1.1.0 bug.
     * @link https://issuetracker.google.com/issues/141132133
     */
    override fun applyOverrideConfiguration(overrideConfiguration: Configuration) =
            when (Build.VERSION.SDK_INT) {
                in 21..22 -> Unit
                else -> super.applyOverrideConfiguration(overrideConfiguration)
            }

    override fun onPause() {
        super.onPause()
        floatingPreview?.onPause()
    }

    override fun onStop() {
        super.onStop()
        tabs.saveTabList()
    }

    override fun onDestroy() {
        tabs.dispose()
        disposables.cancel()
        searchWithClip.dispose()
        pageSearchPresenter.dispose()
        floatingPreview?.dispose()
        GlobalWebViewPool.dispose()
        super.onDestroy()
    }

    companion object {

        /**
         * Header hiding duration.
         */
        private const val HEADER_HIDING_DURATION = 75L

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_main

        /**
         * Request code of opening PDF.
         */
        private const val REQUEST_CODE_OPEN_PDF: Int = 7

    }

}
