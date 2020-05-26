/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import jp.toastkid.yobidashi.settings.color.ColorSettingFragment
import jp.toastkid.yobidashi.settings.fragment.BrowserSettingFragment
import jp.toastkid.yobidashi.settings.fragment.ColorFilterSettingFragment
import jp.toastkid.yobidashi.settings.fragment.DisplayingSettingFragment
import jp.toastkid.yobidashi.settings.fragment.EditorSettingFragment
import jp.toastkid.yobidashi.settings.fragment.NotificationSettingFragment
import jp.toastkid.yobidashi.settings.fragment.OtherSettingFragment
import jp.toastkid.yobidashi.settings.fragment.SearchSettingFragment
import jp.toastkid.yobidashi.settings.fragment.TitleIdSupplier

/**
 * Setting fragments pager adapter.
 *
 * @param fragment [Fragment]
 * @param titleResolver Title string resource resolve consumer.
 * @author toastkidjp
 */
class PagerAdapter(
        private val fragment: Fragment,
        private val titleResolver: (Int) -> String
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 8

    override fun createFragment(position: Int): Fragment = obtainFragment(
            when (position) {
                0 -> DisplayingSettingFragment::class.java
                1 -> ColorSettingFragment::class.java
                2 -> SearchSettingFragment::class.java
                3 -> BrowserSettingFragment::class.java
                4 -> EditorSettingFragment::class.java
                5 -> ColorFilterSettingFragment::class.java
                6 -> NotificationSettingFragment::class.java
                7 -> OtherSettingFragment::class.java
                else -> OtherSettingFragment::class.java
            }
    )

    private fun obtainFragment(fragmentClass: Class<out Fragment>) =
            fragment.childFragmentManager.findFragmentByTag(fragmentClass.canonicalName)
                    ?: fragmentClass.newInstance()

    fun getPageTitle(position: Int): CharSequence? {
        val item = createFragment(position)
        return if (item is TitleIdSupplier) titleResolver(item.titleId()) else ""
    }

}