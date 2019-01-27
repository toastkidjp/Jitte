/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.search.clip

import android.support.annotation.ColorInt
import android.support.v4.content.ContextCompat
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearchClipboardBinding
import jp.toastkid.yobidashi.libs.Urls
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.facade.BaseModule

/**
 * @author toastkidjp
 */
class ClipboardModule(
        private val binding: ModuleSearchClipboardBinding,
        onClick: (String) -> Unit

) : BaseModule(binding.root) {

    @ColorInt
    private val linkColor = ContextCompat.getColor(binding.root.context, R.color.link_blue)

    @ColorInt
    private val textColor = ContextCompat.getColor(binding.root.context, R.color.black)

    init {
        binding.root.setOnClickListener {
            val activityContext = it.context
            onClick(binding.text.text.toString())
            Clipboard.clip(activityContext, "")
        }
    }

    fun switch() {
        val primary = Clipboard.getPrimary(binding.root.context)?.toString()
        if (primary == null || primary.isBlank()) {
            hide()
            return
        }

        show()
        if (Urls.isValidUrl(primary)) {
            setLink(primary)
        } else {
            setSearch(primary)
        }
    }

    private fun setSearch(query: String) {
        binding.image.setImageResource(R.drawable.ic_search)
        binding.text.setText(query)
        binding.text.setTextColor(textColor)
    }

    private fun setLink(link: String) {
        binding.image.setImageResource(R.drawable.ic_web_black)
        binding.text.setText(link)
        binding.text.setTextColor(linkColor)
    }

}