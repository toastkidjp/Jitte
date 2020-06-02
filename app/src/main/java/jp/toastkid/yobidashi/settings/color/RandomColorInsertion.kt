/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.settings.color

import android.content.Context
import android.graphics.Color
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.*

/**
 * @author toastkidjp
 */
class RandomColorInsertion {

    private val random = Random()

    /**
     * Insert random colors.
     *
     * @param context
     */
    operator fun invoke(context: Context): Job {
        val bg = Color.argb(
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX)
        )

        val font = Color.argb(
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX),
                random.nextInt(COLOR_CODE_MAX)
        )

        return CoroutineScope(Dispatchers.IO).launch {
            DatabaseFinder().invoke(context)
                    .savedColorRepository()
                    .add(SavedColor.make(bg, font))
        }
    }

    companion object {
        private const val COLOR_CODE_MAX = 255
    }
}