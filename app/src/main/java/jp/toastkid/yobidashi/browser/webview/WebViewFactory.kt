package jp.toastkid.yobidashi.browser.webview

import android.content.Context
import android.os.Handler
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import android.text.TextUtils
import android.view.MotionEvent
import android.webkit.WebView
import io.reactivex.disposables.CompositeDisposable
import jp.toastkid.yobidashi.browser.BrowserFragment
import jp.toastkid.yobidashi.browser.webview.dialog.AnchorTypeLongTapDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.ElseCaseLongTapDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.ImageAnchorTypeLongTapDialogFragment
import jp.toastkid.yobidashi.browser.webview.dialog.ImageTypeLongTapDialogFragment
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * [WebView] factory.
 *
 * @author toastkidjp
 */
internal object WebViewFactory {

    /**
     * [CompositeDisposable].
     */
    private val disposables = CompositeDisposable()

    /**
     * Use for only extract anchor URL.
     */
    private val handler = Handler(Handler.Callback { message ->
        message?.data?.get("url")?.toString()?.let { anchor = it }
        true
    })

    /**
     * Extracted anchor URL.
     */
    private var anchor: String = ""

    /**
     * Make new [WebView].
     *
     * @param context [Context]
     */
    fun make(context: Context): CustomWebView {
        val webView = CustomWebView(context)
        webView.setOnTouchListener { _, motionEvent ->
            when (motionEvent.getAction()) {
                MotionEvent.ACTION_UP -> webView.enablePullToRefresh = false
            }
            false
        }

        val preferenceApplier = PreferenceApplier(context)

        webView.setOnLongClickListener {
            val hitResult = webView.hitTestResult
            when (hitResult.type) {
                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    webView.requestFocusNodeHref(handler.obtainMessage())
                    if (context is FragmentActivity) {
                        if (TextUtils.isEmpty(anchor)) {
                            handler.postDelayed({ showImageAnchorDialog(url, context) }, 300L)
                            return@setOnLongClickListener true
                        }
                        showImageAnchorDialog(url, context)
                    }
                    false
                }
                WebView.HitTestResult.IMAGE_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    if (context is FragmentActivity) {
                        showDialogFragment(
                                ImageTypeLongTapDialogFragment.make(url),
                                context.supportFragmentManager
                        )
                    }
                    true
                }
                WebView.HitTestResult.SRC_ANCHOR_TYPE -> {
                    val url = hitResult.extra
                    if (url.isEmpty()) {
                        return@setOnLongClickListener false
                    }
                    if (context is FragmentActivity) {
                        showDialogFragment(
                                AnchorTypeLongTapDialogFragment.make(url),
                                context.supportFragmentManager
                        )
                    }
                    false
                }
                else -> {
                    val extra = hitResult.extra
                    if (extra == null || extra.isEmpty()) {
                        return@setOnLongClickListener false
                    }

                    if (context is FragmentActivity) {
                        ElseCaseLongTapDialogFragment
                                .make(preferenceApplier.getDefaultSearchEngine(), extra)
                                .show(
                                        context.supportFragmentManager,
                                        ElseCaseLongTapDialogFragment::class.java.simpleName
                                )
                    }
                    false
                }
            }
        }
        val settings = webView.settings
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.javaScriptCanOpenWindowsAutomatically = false
        return webView
    }

    /**
     * Show image anchor type dialog.
     *
     * @param url URL string
     * @param fragmentActivity [FragmentActivity]
     */
    private fun showImageAnchorDialog(url: String, fragmentActivity: FragmentActivity) {
        val dialogFragment = ImageAnchorTypeLongTapDialogFragment.make(url, anchor)
        showDialogFragment(
                dialogFragment,
                fragmentActivity.supportFragmentManager
        )
    }

    /**
     * Show dialog fragment.
     *
     * @param dialogFragment [DialogFragment]
     * @param supportFragmentManager [FragmentManager]
     */
    private fun showDialogFragment(
            dialogFragment: DialogFragment,
            supportFragmentManager: FragmentManager?
    ) {
        dialogFragment.setTargetFragment(
                supportFragmentManager?.findFragmentByTag(BrowserFragment::class.java.simpleName),
                1
        )
        dialogFragment.show(
                supportFragmentManager,
                dialogFragment::class.java.simpleName
        )
    }

    /**
     * Dispose subscriptions.
     */
    fun dispose() {
        disposables.clear()
    }
}
