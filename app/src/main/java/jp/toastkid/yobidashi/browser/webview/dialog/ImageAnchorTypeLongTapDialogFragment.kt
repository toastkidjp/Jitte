/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.browser.webview.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.clip.Clipboard

/**
 * @author toastkidjp
 */
class ImageAnchorTypeLongTapDialogFragment : DialogFragment() {

    private var onClickImage: ImageDialogCallback? = null

    private var onClickAnchor: AnchorDialogCallback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val title = arguments?.getString(KEY_TITLE) ?: ""

        val url = arguments?.getString(KEY_EXTRA)
                ?: return super.onCreateDialog(savedInstanceState)

        val anchor = arguments?.getString(KEY_ANCHOR)
                ?: return super.onCreateDialog(savedInstanceState)

        val target = targetFragment
        if (target is ImageDialogCallback) {
            onClickImage = target
        }
        if (target is AnchorDialogCallback) {
            onClickAnchor = target
        }

        return AlertDialog.Builder(activityContext)
                .setTitle("URL: $url")
                .setItems(R.array.image_anchor_menu) { _, which ->
                    when (which) {
                        0 -> onClickAnchor?.openNewTab(anchor)
                        1 -> onClickAnchor?.openBackgroundTab(title, anchor)
                        2 -> onClickAnchor?.openCurrent(anchor)
                        3 -> onClickImage?.onClickImageSearch(url)
                        4 -> onClickImage?.onClickSetBackground(url)
                        5 -> onClickImage?.onClickSaveForBackground(url)
                        6 -> onClickImage?.onClickDownloadImage(url)
                        7 -> Clipboard.clip(activityContext, anchor)
                    }
                }
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

    companion object {

        private const val KEY_TITLE = "title"

        private const val KEY_ANCHOR = "anchor"

        private const val KEY_EXTRA = "extra"

        fun make(title: String, extra: String, anchor: String) =
                ImageAnchorTypeLongTapDialogFragment()
                        .also {
                            it.arguments = bundleOf(
                                    KEY_TITLE to title,
                                    KEY_EXTRA to extra,
                                    KEY_ANCHOR to anchor
                            )
                        }
    }
}