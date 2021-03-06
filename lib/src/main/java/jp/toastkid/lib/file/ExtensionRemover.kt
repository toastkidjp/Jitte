/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.lib.file

/**
 * @author toastkidjp
 */
class ExtensionRemover {

    /**
     * Remove extension from passed text.
     *
     * @param fileName
     * @return string
     */
    operator fun invoke(fileName: String): String {
        val endIndex = fileName.lastIndexOf(DOT)
        return if (endIndex == -1) fileName else fileName.substring(0, endIndex)
    }

    companion object {
        private const val DOT = "."
    }
}