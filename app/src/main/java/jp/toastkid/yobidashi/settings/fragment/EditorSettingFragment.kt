/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.fragment

import android.annotation.SuppressLint
import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.TextView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FragmentSettingSectionEditorBinding
import jp.toastkid.yobidashi.editor.EditorFontSize
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * @author toastkidjp
 */
class EditorSettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingSectionEditorBinding

    private lateinit var preferenceApplier: PreferenceApplier

    /**
     * Initial background color.
     */
    private var initialBgColor: Int = 0

    /**
     * Initial font color.
     */
    private var initialFontColor: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_setting_section_editor, container, false)
        val activityContext = context ?: return super.onCreateView(inflater, container, savedInstanceState)
        preferenceApplier = PreferenceApplier(activityContext)
        binding.also { editorModule ->
            val backgroundColor = preferenceApplier.editorBackgroundColor()
            val fontColor = preferenceApplier.editorFontColor()

            initialBgColor = backgroundColor
            initialFontColor = fontColor

            editorModule.backgroundPalette?.also {
                it.addSVBar(editorModule.backgroundSvbar)
                it.addOpacityBar(editorModule.backgroundOpacitybar)
                it.setOnColorChangedListener { editorModule.ok?.setBackgroundColor(it) }
                it.color = backgroundColor
            }

            editorModule.fontPalette?.also {
                it.addSVBar(editorModule.fontSvbar)
                it.addOpacityBar(editorModule.fontOpacitybar)
                it.setOnColorChangedListener { editorModule.ok?.setTextColor(it) }
                it.color = fontColor
            }
            editorModule.fragment = this
            Colors.setColors(
                    binding.ok as TextView,
                    ColorPair(backgroundColor, fontColor)
            )
            Colors.setColors(
                    binding.prev as TextView,
                    ColorPair(initialBgColor, initialFontColor)
            )

            binding.fontSize.adapter = object : BaseAdapter() {
                override fun getCount(): Int = EditorFontSize.values().size

                override fun getItem(position: Int): EditorFontSize
                        = EditorFontSize.values()[position]

                override fun getItemId(position: Int): Long
                        = EditorFontSize.values()[position].ordinal.toLong()

                @SuppressLint("ViewHolder")
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val item = EditorFontSize.values()[position]
                    val view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.text = item.size.toString()
                    return view
                }
            }
            binding.fontSize.setSelection(
                    EditorFontSize.findIndex(preferenceApplier.editorFontSize())
            )
            binding.fontSize.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    preferenceApplier.setEditorFontSize(EditorFontSize.values()[position].size)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            }
        }
        binding.fragment = this
        return binding.root
    }

    /**
     * OK button's action.
     */
    fun ok() {
        val backgroundColor = binding.backgroundPalette?.color ?: Color.BLACK
        val fontColor = binding.fontPalette?.color ?: Color.WHITE

        preferenceApplier.setEditorBackgroundColor(backgroundColor)
        preferenceApplier.setEditorFontColor(fontColor)

        binding.backgroundPalette?.color = backgroundColor
        binding.fontPalette?.color = fontColor

        val colorPair = ColorPair(backgroundColor, fontColor)
        Colors.setColors(binding.ok as TextView, colorPair)
        Toaster.snackShort(binding.root, R.string.settings_color_done_commit, colorPair)
    }

    /**
     * Reset button's action.
     */
    fun reset() {
        preferenceApplier.setEditorBackgroundColor(initialBgColor)
        preferenceApplier.setEditorFontColor(initialFontColor)

        Colors.setColors(binding.ok as TextView, ColorPair(initialBgColor, initialFontColor))

        Toaster.snackShort(binding.root, R.string.settings_color_done_reset, preferenceApplier.colorPair())
    }

}