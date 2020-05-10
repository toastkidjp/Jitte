/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.pdf

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.SeekBar
import androidx.annotation.LayoutRes
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentPdfViewerBinding
import jp.toastkid.yobidashi.databinding.ModulePdfHeaderBinding
import jp.toastkid.yobidashi.libs.EditTextColorSetter
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.main.ContentScrollable
import jp.toastkid.yobidashi.main.HeaderViewModel
import jp.toastkid.yobidashi.main.TabUiFragment

/**
 * @author toastkidjp
 */
class PdfViewerFragment : Fragment(), TabUiFragment, CommonFragmentAction, ContentScrollable {

    /**
     * Data binding object.
     */
    private lateinit var binding: FragmentPdfViewerBinding

    private lateinit var headerBinding: ModulePdfHeaderBinding

    /**
     * Adapter.
     */
    private lateinit var adapter: Adapter

    /**
     * LayoutManager.
     */
    private lateinit var layoutManager: LinearLayoutManager

    private var headerViewModel: HeaderViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding = DataBindingUtil.inflate(
                inflater,
                LAYOUT_ID,
                container,
                false
        )
        headerBinding = DataBindingUtil.inflate(
                inflater,
                APP_BAR_CONTENT_LAYOUT_ID,
                container,
                false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = Adapter(LayoutInflater.from(context), context?.contentResolver)
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        binding.pdfImages.adapter = adapter
        binding.pdfImages.layoutManager = layoutManager
        PagerSnapHelper().attachToRecyclerView(binding.pdfImages)

        headerBinding.seek.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                val progress = p0?.progress ?: 0
                headerBinding.input.setText((progress + 1).toString())
            }

            override fun onStartTrackingTouch(p0: SeekBar?) = Unit

            override fun onStopTrackingTouch(p0: SeekBar?) = Unit

        })
        headerBinding.input.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) = Unit

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(inputText: CharSequence?, p1: Int, p2: Int, p3: Int) {
                inputText?.let {
                    val newIndex = try {
                        Integer.parseInt(it.toString()) - 1
                    } catch (e: NumberFormatException) {
                        -1
                    }

                    if (newIndex == -1) {
                        return@let
                    }

                    scrollTo(newIndex)
                }
            }

        })

        (context as? FragmentActivity)?.let {
            headerViewModel = ViewModelProvider(it).get(HeaderViewModel::class.java)
        }

        arguments?.let { arguments ->
            arguments.getParcelable<Uri>(KEY_URI)?.also { load(it) }
            arguments.getInt(KEY_SCROLL_Y).also { scrollTo(it) }
        }
    }

    /**
     * Load PDF content from [Uri].
     *
     * @param uri
     */
    private fun load(uri: Uri) {
        adapter.load(uri)
        binding.pdfImages.scheduleLayoutAnimation()
        headerBinding.seek.max = adapter.itemCount - 1
    }

    /**
     * Scroll to specified position.
     *
     * @param position
     */
    fun scrollTo(position: Int) {
        layoutManager.scrollToPosition(getSafeIndex(position))
    }

    /**
     * Get safe index.
     *
     * @param index
     */
    private fun getSafeIndex(index: Int): Int =
            if (index < 0 || adapter.itemCount < index) 0 else index

    /**
     * Animate root view.
     *
     * @param animation
     */
    fun animate(animation: Animation) {
        binding.root.startAnimation(animation)
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.pdfImages, adapter.itemCount)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.pdfImages, adapter.itemCount)
    }

    override fun onResume() {
        super.onResume()
        headerViewModel?.replace(headerBinding.root)
        applyColor(PreferenceApplier(requireContext()).colorPair())
    }

    /**
     * Apply color to views.
     *
     * @param colorPair
     */
    private fun applyColor(colorPair: ColorPair) {
        headerBinding.header.setBackgroundColor(colorPair.bgColor())
        headerBinding.seek.progressDrawable.colorFilter =
                PorterDuffColorFilter(colorPair.fontColor(), PorterDuff.Mode.SRC_IN)
        EditTextColorSetter().invoke(headerBinding.input, colorPair.fontColor())
    }

    fun setInitialArguments(uri: Uri?, scrolled: Int) {
        arguments = bundleOf(KEY_URI to uri, KEY_SCROLL_Y to scrolled)
    }

    companion object {

        @LayoutRes
        private const val LAYOUT_ID = R.layout.fragment_pdf_viewer

        @LayoutRes
        private const val APP_BAR_CONTENT_LAYOUT_ID = R.layout.module_pdf_header

        private const val KEY_URI = "uri"

        private const val KEY_SCROLL_Y = "scrollY"

    }
}