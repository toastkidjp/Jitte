/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.editor

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import jp.toastkid.yobidashi.R

/**
 * Show confirmation of "Paste as".
 * Paste clipped text as Markdown's quotation style.
 *
 * @author toastkidjp
 */
class PasteAsConfirmationDialogFragment : DialogFragment() {

    interface Callback {
        fun onClickPasteAs()
    }

    /**
     * Callback on click positive button.
     */
    private var callback: Callback? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activityContext = context ?: return super.onCreateDialog(savedInstanceState)

        val targetFragment = targetFragment
        if (targetFragment is Callback) {
            callback = targetFragment
        }
        return AlertDialog.Builder(activityContext)
                .setTitle(activityContext.getString(R.string.paste_as_quotation))
                .setMessage(R.string.message_paste_as_dialog)
                .setPositiveButton(R.string.ok) { d, _ ->
                    callback?.onClickPasteAs()
                    d.dismiss()
                }
                .setNegativeButton(R.string.cancel) { d, _ -> d.cancel() }
                .create()
    }

    companion object {

        /**
         * Show this dialog.
         *
         * @param context [Context]
         */
        fun show(context: Context) {
            val dialogFragment = PasteAsConfirmationDialogFragment()

            if (context is FragmentActivity) {
                val supportFragmentManager = context.supportFragmentManager
                val target = supportFragmentManager
                        .findFragmentByTag(EditorFragment::class.java.canonicalName)
                dialogFragment.setTargetFragment(target, 1)
                dialogFragment.show(
                        supportFragmentManager,
                        dialogFragment::class.java.canonicalName
                )
            }
        }
    }
}