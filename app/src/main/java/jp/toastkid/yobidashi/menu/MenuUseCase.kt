/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.menu

import android.content.ActivityNotFoundException
import android.os.Build
import android.view.View
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.barcode.BarcodeReaderFragment
import jp.toastkid.yobidashi.browser.BrowserViewModel
import jp.toastkid.yobidashi.browser.archive.ArchivesFragment
import jp.toastkid.yobidashi.browser.bookmark.BookmarkFragment
import jp.toastkid.yobidashi.browser.history.ViewHistoryFragment
import jp.toastkid.yobidashi.cleaner.ProcessCleanerInvoker
import jp.toastkid.yobidashi.gesture.GestureMemoFragment
import jp.toastkid.yobidashi.launcher.LauncherFragment
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.network.WifiConnectionChecker
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.main.MainActivity
import jp.toastkid.yobidashi.main.content.ContentViewModel
import jp.toastkid.yobidashi.media.image.list.ImageViewerFragment
import jp.toastkid.yobidashi.media.music.popup.MediaPlayerPopup
import jp.toastkid.yobidashi.planning_poker.CardListFragment
import jp.toastkid.yobidashi.rss.RssReaderFragment
import jp.toastkid.yobidashi.search.voice.VoiceSearch
import jp.toastkid.yobidashi.settings.fragment.OverlayColorFilterViewModel
import jp.toastkid.yobidashi.wikipedia.random.RandomWikipedia
import jp.toastkid.yobidashi.wikipedia.today.DateArticleUrlFactory
import timber.log.Timber
import java.util.*

/**
 * TODO clean up duplicated codes.
 * @author toastkidjp
 */
class MenuUseCase(
        private val activitySupplier: () -> FragmentActivity,
        private val menuViewModel: MenuViewModel?
) {

    private val contentViewModel =
            ViewModelProviders.of(activitySupplier()).get(ContentViewModel::class.java)

    private val preferenceApplier = PreferenceApplier(activitySupplier())

    private lateinit var randomWikipedia: RandomWikipedia

    private val mediaPlayerPopup by lazy { MediaPlayerPopup(activitySupplier()) }

    init {
        val activity = activitySupplier()
        menuViewModel?.click?.observe(activity, Observer {
            onMenuClick(it)
        })

        menuViewModel?.longClick?.observe(activity, Observer {
            onMenuLongClick(it)
        })
    }

    fun onMenuClick(menu: Menu) {
        when (menu) {
            Menu.TOP-> {
                contentViewModel.toTop()
            }
            Menu.BOTTOM-> {
                contentViewModel.toBottom()
            }
            Menu.SHARE-> {
                contentViewModel.share()
            }
            Menu.CODE_READER -> {
                contentViewModel.nextFragment(BarcodeReaderFragment::class.java)
            }
            Menu.OVERLAY_COLOR_FILTER-> {
                preferenceApplier.setUseColorFilter(!preferenceApplier.useColorFilter())
                (activitySupplier() as? MainActivity)?.let {
                    ViewModelProviders.of(it).get(OverlayColorFilterViewModel::class.java)
                            .newColor(preferenceApplier.filterColor())
                }
            }
            Menu.MEMORY_CLEANER -> {
                val activity = activitySupplier()
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                    contentViewModel.snackShort(activity.getString(R.string.message_cannot_use_under_l))
                    return
                }
                ProcessCleanerInvoker()(activity.findViewById(R.id.content))
            }
            Menu.PLANNING_POKER-> {
                contentViewModel.nextFragment(CardListFragment::class.java)
            }
            Menu.APP_LAUNCHER-> {
                contentViewModel.nextFragment(LauncherFragment::class.java)
            }
            Menu.RSS_READER -> {
                contentViewModel.nextFragment(RssReaderFragment::class.java)
            }
            Menu.AUDIO -> {
                val parent = extractContentView() ?: return
                mediaPlayerPopup.show(parent)
                menuViewModel?.close()
            }
            Menu.BOOKMARK-> {
                contentViewModel.nextFragment(BookmarkFragment::class.java)
            }
            Menu.VIEW_HISTORY-> {
                contentViewModel.nextFragment(ViewHistoryFragment::class.java)
            }
            Menu.IMAGE_VIEWER -> {
                contentViewModel.nextFragment(ImageViewerFragment::class.java)
            }
            Menu.LOAD_HOME-> {
                ViewModelProviders.of(activitySupplier()).get(BrowserViewModel::class.java)
                        .open(preferenceApplier.homeUrl.toUri())
            }
            Menu.EDITOR-> {
                contentViewModel.openEditorTab()
            }
            Menu.PDF-> {
                contentViewModel.openPdf()
            }
            Menu.WEB_SEARCH -> {
                contentViewModel.webSearch()
            }
            Menu.GESTURE_MEMO -> {
                contentViewModel.nextFragment(GestureMemoFragment::class.java)
            }
            Menu.VOICE_SEARCH-> {
                activitySupplier().also {
                    try {
                        it.startActivityForResult(
                                VoiceSearch.makeIntent(it),
                                VoiceSearch.REQUEST_CODE
                        )
                    } catch (e: ActivityNotFoundException) {
                        Timber.e(e)
                        VoiceSearch.suggestInstallGoogleApp(
                                it.findViewById(R.id.content),
                                preferenceApplier.colorPair()
                        )
                    }
                }
            }
            Menu.WHAT_HAPPENED_TODAY -> {
                val calendar = Calendar.getInstance()
                val url = DateArticleUrlFactory()(
                        activitySupplier(),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                )
                if (Urls.isInvalidUrl(url)) {
                    return
                }

                ViewModelProviders.of(activitySupplier()).get(BrowserViewModel::class.java)
                        .open(url.toUri())
            }
            Menu.RANDOM_WIKIPEDIA -> {
                val activity = activitySupplier()
                if (preferenceApplier.wifiOnly &&
                        WifiConnectionChecker.isNotConnecting(activity)) {
                    Toaster.snackShort(
                            activity.findViewById<View>(R.id.content),
                            activity.getString(R.string.message_wifi_not_connecting),
                            preferenceApplier.colorPair()
                    )
                    return
                }

                if (!::randomWikipedia.isInitialized) {
                    randomWikipedia = RandomWikipedia()
                }
                randomWikipedia
                        .fetchWithAction { title, link ->
                            ViewModelProviders.of(activitySupplier()).get(BrowserViewModel::class.java)
                                    .open(link)
                            val fragmentActivity = activitySupplier()
                            Toaster.snackShort(
                                    fragmentActivity.findViewById<View>(R.id.content),
                                    fragmentActivity.getString(R.string.message_open_random_wikipedia, title),
                                    preferenceApplier.colorPair()
                            )
                        }
            }
            Menu.VIEW_ARCHIVE -> {
                contentViewModel.nextFragment(ArchivesFragment::class.java)
            }
            Menu.FIND_IN_PAGE-> {
                contentViewModel.switchPageSearcher()
            }
        }
    }

    /**
     * Callback method on long clicked menu.
     *
     * @param menu
     * @return true
     */
    fun onMenuLongClick(menu: Menu): Boolean {
        val view = extractContentView() ?: return true
        Toaster.snackLong(
                view,
                menu.titleId,
                R.string.run,
                View.OnClickListener { onMenuClick(menu) },
                preferenceApplier.colorPair()
        )
        return true
    }

    private fun extractContentView(): View? =
            activitySupplier().findViewById(R.id.content)

}