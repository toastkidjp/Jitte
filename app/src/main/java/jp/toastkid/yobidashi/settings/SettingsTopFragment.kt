package jp.toastkid.yobidashi.settings

import android.content.Context
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.text.Html
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.Spinner
import jp.toastkid.yobidashi.BaseFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.appwidget.search.Updater
import jp.toastkid.yobidashi.browser.CookieCleanerCompat
import jp.toastkid.yobidashi.browser.MenuPos
import jp.toastkid.yobidashi.browser.ScreenMode
import jp.toastkid.yobidashi.browser.UserAgent
import jp.toastkid.yobidashi.color_filter.ColorFilter
import jp.toastkid.yobidashi.databinding.FragmentSettingSectionColorFilterBinding
import jp.toastkid.yobidashi.databinding.FragmentSettingsBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.intent.SettingsIntentFactory
import jp.toastkid.yobidashi.main.StartUp
import jp.toastkid.yobidashi.notification.widget.NotificationWidget
import jp.toastkid.yobidashi.search.SearchCategory
import jp.toastkid.yobidashi.search.SearchCategorySpinnerInitializer
import jp.toastkid.yobidashi.settings.background.BackgroundSettingActivity
import jp.toastkid.yobidashi.settings.color.ColorSettingActivity

/**
 * Settings top fragment.
 *
 * @author toastkidjp
 */
class SettingsTopFragment : BaseFragment() {

    /**
     * Data binding object.
     */
    private lateinit var binding: FragmentSettingsBinding

    /**
     * Color filter.
     */
    private lateinit var colorFilter: ColorFilter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this
        binding.moduleBrowser?.let {
            it.fragment = this
            TextInputs.setEmptyAlert(it.homeInputLayout)
        }

        activity?.let {
            colorFilter = ColorFilter(it, binding.root)
        }

        initMenuPos()
        initBrowserExpandable()

        SearchCategorySpinnerInitializer.invoke(binding.searchCategories as Spinner)
        binding.searchCategories.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                preferenceApplier().setDefaultSearchEngine(
                        SearchCategory.values()[binding.searchCategories.selectedItemPosition].name)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        binding.startUpItems?.startUpSelector?.setOnCheckedChangeListener { radioGroup, checkedId ->
            preferenceApplier().startUp = StartUp.findById(checkedId)
        }

        ColorFilterSettingInitializer(
                binding.filterColor as FragmentSettingSectionColorFilterBinding,
                { colorFilter.color(it) }
        ).invoke()
        return binding.root
    }

    private fun initMenuPos() {
        binding.moduleBrowser?.menuPosRadio?.let {
            it.setOnCheckedChangeListener ({ group, checkedId ->
                when (group.checkedRadioButtonId) {
                    R.id.menu_pos_left  -> preferenceApplier().setMenuPos(MenuPos.LEFT)
                    R.id.menu_pos_right -> preferenceApplier().setMenuPos(MenuPos.RIGHT)
                }
            })
            it.check(preferenceApplier().menuPos().id())
        }

    }

    private fun initBrowserExpandable() {
        binding.moduleBrowser?.browserExpand?.screenMode?.setOnCheckedChangeListener ({ group, checkedId ->
            when (group.checkedRadioButtonId) {
                R.id.full_screen  -> preferenceApplier().setBrowserScreenMode(ScreenMode.FULL_SCREEN)
                R.id.expandable   -> preferenceApplier().setBrowserScreenMode(ScreenMode.EXPANDABLE)
                R.id.fixed        -> preferenceApplier().setBrowserScreenMode(ScreenMode.FIXED)
            }
        })
        binding.moduleBrowser?.browserExpand?.screenMode?.check(preferenceApplier().browserScreenMode().id())
    }

    override fun onResume() {
        super.onResume()

        setCurrentValues()
    }

    private fun setCurrentValues() {
        val preferenceApplier = preferenceApplier()
        binding.useNotificationWidgetCheck.isChecked = preferenceApplier.useNotificationWidget()
        binding.moduleBrowser?.let {
            Colors.setColors(it.homeButton, colorPair())
            it.homeInputLayout.editText?.setText(preferenceApplier.homeUrl)
            it.useInternalBrowserCheck.isChecked = preferenceApplier.useInternalBrowser()
            it.retainTabsCheck.isChecked = preferenceApplier.doesRetainTabs()
            it.browserJsCheck.isChecked = preferenceApplier.useJavaScript()
            it.useImageCheck.isChecked = preferenceApplier.doesLoadImage()
            it.saveFormCheck.isChecked = preferenceApplier.doesSaveForm()
            it.userAgentValue.text = UserAgent.valueOf(preferenceApplier.userAgent()).title()
            it.saveViewHistoryCheck.isChecked = preferenceApplier.saveViewHistory
            it.useInversionCheck.isChecked = preferenceApplier.useInversion
            it.adRemoveCheck.isChecked = preferenceApplier.adRemove
        }

        binding.useColorFilterCheck.isChecked = preferenceApplier.useColorFilter()

        binding.enableSearchWithClipCheck.isChecked = preferenceApplier.enableSearchWithClip
        binding.useSuggestionCheck.isChecked = preferenceApplier.isEnableSuggestion
        binding.useHistoryCheck.isChecked = preferenceApplier.isEnableSearchHistory
        binding.useFavoriteCheck.isChecked = preferenceApplier.isEnableFavoriteSearch
        binding.useViewHistoryCheck.isChecked = preferenceApplier.isEnableViewHistory
        binding.wifiOnlyCheck.isChecked = preferenceApplier.wifiOnly

        binding.startUpItems?.startUpSelector?.check(preferenceApplier.startUp.radioButtonId)

        val filterColor = preferenceApplier.filterColor()
        binding.filterColor?.sample?.setBackgroundColor(filterColor)
        binding.filterColor?.alpha?.setProgress(Color.alpha(filterColor))
    }

    /**
     * Call color setting.
     *
     * @param view
     */
    fun colorSettings(view: View) {
        sendLog("nav_color")
        activity?.let {
            startActivity(ColorSettingActivity.makeIntent(it))
        }
    }

    /**
     * Call background setting.
     *
     * @param view
     */
    fun backgroundSettings(view: View) {
        sendLog("nav_bg_set")
        activity?.let {
            startActivity(BackgroundSettingActivity.makeIntent(it))
        }
    }

    /**
     * Clear background setting.
     *
     * @param view
     */
    fun clearBackgroundSettings(view: View) {
        sendLog("nav_bg_reset")
        preferenceApplier().removeBackgroundImagePath()
        Toaster.snackShort(
                binding.root,
                R.string.message_reset_bg_image,
                preferenceApplier().colorPair()
        )
    }

    /**
     * Open search categories spinner.
     *
     * @param v
     */
    fun openSearchCategory(v: View) {
        binding.searchCategories.performClick()
    }

    /**
     * Switch notification widget displaying.
     *
     * @param v
     */
    fun switchSearchWithClip(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.enableSearchWithClip
        preferenceApplier.enableSearchWithClip = newState
        binding.enableSearchWithClipCheck.isChecked = newState

        @StringRes val messageId: Int
                = if (newState) { R.string.message_enable_swc } else { R.string.message_disable_swc }
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    fun switchUseSuggestion(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.isEnableSuggestion
        preferenceApplier.switchEnableSuggestion()
        binding.useSuggestionCheck.isChecked = newState
    }

    fun switchUseSearchHistory(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.isEnableSearchHistory
        preferenceApplier.switchEnableSearchHistory()
        binding.useHistoryCheck.isChecked = newState
    }

    fun switchUseFavoriteSearch(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.isEnableFavoriteSearch
        preferenceApplier.switchEnableFavoriteSearch()
        binding.useFavoriteCheck.isChecked = newState
    }

    fun switchUseViewHistory(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.isEnableViewHistory
        preferenceApplier.switchEnableViewHistory()
        binding.useViewHistoryCheck.isChecked = newState
    }

    /**
     * UserAgent setting.
     *
     * @param v
     */
    fun userAgent(v: View) {
        UserAgent.showSelectionDialog(
                binding.root,
                { userAgent -> binding.moduleBrowser?.userAgentValue?.text = userAgent.title() }
        )
    }

    fun switchAdRemove() {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.adRemove
        preferenceApplier.adRemove = newState
        binding.moduleBrowser?.adRemoveCheck?.isChecked = newState
    }

    /**
     * Switch notification widget displaying.

     * @param v
     */
    fun switchNotificationWidget(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.useNotificationWidget()
        preferenceApplier.setUseNotificationWidget(newState)
        binding.useNotificationWidgetCheck.isChecked = newState

        val activityContext: Context = context ?: return

        @StringRes var messageId: Int = R.string.message_done_showing_notification_widget
        if (newState) {
            NotificationWidget.show(activityContext)
        } else {
            NotificationWidget.hide(activityContext)
            messageId = R.string.message_remove_notification_widget
        }
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /**
     * Switch browser.
     *
     * @param v
     */
    fun switchInternalBrowser(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.useInternalBrowser()
        preferenceApplier.setUseInternalBrowser(newState)
        binding.moduleBrowser?.useInternalBrowserCheck?.isChecked = newState
        @StringRes val messageId: Int
                = if (newState) { R.string.message_use_internal_browser }
                  else { R.string.message_use_chrome }
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /**
     * Switch retaining tabs.
     *
     * @param v
     */
    fun switchRetainTabs(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.doesRetainTabs()
        preferenceApplier.setRetainTabs(newState)
        binding.moduleBrowser?.retainTabsCheck?.isChecked = newState
        @StringRes val messageId: Int = if (newState)
            R.string.message_check_retain_tabs
        else
            R.string.message_check_doesnot_retain_tabs
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /***
     * Commit input.
     * @param view
     */
    fun commitHomeInput(view: View) {
        val input = binding.moduleBrowser?.homeInputLayout?.editText?.text.toString()
        if (TextUtils.isEmpty(input)) {
            Toaster.snackShort(
                    binding.root,
                    R.string.favorite_search_addition_dialog_empty_message,
                    colorPair()
            )
            return
        }
        if (Urls.isInvalidUrl(input)) {
            Toaster.snackShort(binding.root, R.string.message_invalid_url, colorPair())
            return
        }
        preferenceApplier().homeUrl = input

        Toaster.snackShort(
                binding.root,
                getString(R.string.message_commit_home, input),
                colorPair()
        )
    }

    /**
     * Switch content inversion enabling.
     *
     * @param v
     */
    fun switchUseInversion(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.useInversion
        preferenceApplier.useInversion = newState
        binding.moduleBrowser?.useInversionCheck?.isChecked = newState
    }

    /**
     * Switch Wi-Fi only mode.
     *
     * @param v
     */
    fun switchWifiOnly(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.wifiOnly
        preferenceApplier.wifiOnly = newState
        binding.wifiOnlyCheck.isChecked = newState
    }

    /**
     * Switch JavaScript enabling.
     * @param v
     */
    fun switchJsEnabled(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.useJavaScript()
        preferenceApplier.setUseJavaScript(newState)
        binding.moduleBrowser?.browserJsCheck?.isChecked = newState
        @StringRes val messageId: Int = if (newState)
            R.string.message_js_enabled
        else
            R.string.message_js_disabled
        Toaster.snackShort(binding.root, messageId, preferenceApplier.colorPair())
    }

    /**
     * Switch loading images.
     *
     * @param v
     */
    fun switchLoadingImage(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.doesLoadImage()
        preferenceApplier.setLoadImage(newState)
        binding.moduleBrowser?.useImageCheck?.isChecked = newState
    }

    /**
     * Switching saving form data.
     *
     * @param v
     */
    fun switchSaveFormData(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.doesSaveForm()
        preferenceApplier.setSaveForm(newState)
        binding.moduleBrowser?.saveFormCheck?.isChecked = newState
    }

    /**
     * Switch saving view history.
     *
     * @param v
     */
    fun switchViewHistory(v: View) {
        val preferenceApplier = preferenceApplier()
        val newState = !preferenceApplier.saveViewHistory
        preferenceApplier.saveViewHistory = newState
        binding.moduleBrowser?.saveViewHistoryCheck?.isChecked = newState
    }

    /**
     * Clear [WebView] cache.
     *
     * @param snackbarParent for data binding
     */
    fun clearCache(snackbarParent: View) {
        WebView(context).clearCache(true)
        Toaster.snackShort(snackbarParent, R.string.done_clear, colorPair())
    }

    /**
     * Clear [WebView] form data.
     *
     * @param snackbarParent for data binding
     */
    fun clearFormData(snackbarParent: View) {
        WebView(context).clearFormData()
        Toaster.snackShort(snackbarParent, R.string.done_clear, colorPair())
    }

    /**
     * Clear all cookie.
     *
     * @param snackbarParent for data binding
     */
    fun clearCookie(snackbarParent: View) {
        CookieCleanerCompat(snackbarParent)
    }

    /**
     * Call device settings.
     *
     * @param v
     */
    fun deviceSetting(v: View) {
        sendLog("nav_dvc_set")
        startActivity(SettingsIntentFactory.makeLaunch())
    }

    /**
     * Call Wi-Fi settings.
     *
     * @param v
     */
    fun wifi(v: View) {
        sendLog("nav_wifi_set")
        startActivity(SettingsIntentFactory.wifi())
    }

    /**
     * Call Wireless settings.
     *
     * @param v
     */
    fun wireless(v: View) {
        sendLog("nav_wrls_set")
        startActivity(SettingsIntentFactory.wireless())
    }

    /**
     * Call Date-and-Time settings.
     *
     * @param v
     */
    fun dateAndTime(v: View) {
        sendLog("nav_dat")
        startActivity(SettingsIntentFactory.dateAndTime())
    }

    /**
     * Call display settings.
     *
     * @param v
     */
    fun display(v: View) {
        sendLog("nav_dsply")
        startActivity(SettingsIntentFactory.display())
    }

    /**
     * Call all app settings.
     *
     * @param v
     */
    fun allApps(v: View) {
        sendLog("nav_allapps_set")
        startActivity(SettingsIntentFactory.allApps())
    }

    override fun titleId(): Int = R.string.title_settings

    /**
     * Show all menu module.
     */
    fun showAll() {
        binding.displayingModule.visibility = View.VISIBLE
        binding.searchModule.visibility = View.VISIBLE
        binding.moduleBrowser?.root?.visibility = View.VISIBLE
        binding.notificationsModule.visibility = View.VISIBLE
        binding.others.visibility = View.VISIBLE
    }

    /**
     * Show displaying menu module.
     */
    fun showDisplay() {
        binding.displayingModule.visibility = View.VISIBLE
        binding.searchModule.visibility = View.GONE
        binding.moduleBrowser?.root?.visibility = View.GONE
        binding.notificationsModule.visibility = View.GONE
        binding.others.visibility = View.GONE
    }

    /**
     * Show displaying menu module.
     */
    fun showSearch() {
        binding.displayingModule.visibility = View.GONE
        binding.searchModule.visibility = View.VISIBLE
        binding.moduleBrowser?.root?.visibility = View.GONE
        binding.notificationsModule.visibility = View.GONE
        binding.others.visibility = View.GONE
    }

    /**
     * Show browser menu module.
     */
    fun showBrowser() {
        binding.displayingModule.visibility = View.GONE
        binding.searchModule.visibility = View.GONE
        binding.moduleBrowser?.root?.visibility = View.VISIBLE
        binding.notificationsModule.visibility = View.GONE
        binding.others.visibility = View.GONE
    }

    /**
     * Show notification menu module.
     */
    fun showNotifications() {
        binding.displayingModule.visibility = View.GONE
        binding.searchModule.visibility = View.GONE
        binding.moduleBrowser?.root?.visibility = View.GONE
        binding.notificationsModule.visibility = View.VISIBLE
        binding.others.visibility = View.GONE
    }

    /**
     * Show other menu module.
     */
    fun showOthers() {
        binding.displayingModule.visibility = View.GONE
        binding.searchModule.visibility = View.GONE
        binding.moduleBrowser?.root?.visibility = View.GONE
        binding.notificationsModule.visibility = View.GONE
        binding.others.visibility = View.VISIBLE
    }

    /**
     * Switch color filter's visibility.

     * @param v
     */
    fun switchColorFilter(v: View) {
        activity?.let {
            binding.useColorFilterCheck.isChecked = colorFilter.switchState(it)
        }
    }

    /**
     * Clear all settings.

     * @param v
     */
    fun clearSettings(v: View) {
        sendLog("nav_clr_set")
        val preferenceApplier = preferenceApplier()
        val fragmentActivity = activity ?: return
        AlertDialog.Builder(fragmentActivity)
                .setTitle(R.string.title_clear_settings)
                .setMessage(Html.fromHtml(getString(R.string.confirm_clear_all_settings)))
                .setCancelable(true)
                .setNegativeButton(R.string.cancel) { d, i -> d.cancel() }
                .setPositiveButton(R.string.ok) { d, i ->
                    preferenceApplier.clear()
                    colorFilter?.stop()
                    setCurrentValues()
                    Updater.update(fragmentActivity)
                    Toaster.snackShort(binding.root, R.string.done_clear, preferenceApplier.colorPair())
                }
                .show()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.fragment_settings
    }
}
