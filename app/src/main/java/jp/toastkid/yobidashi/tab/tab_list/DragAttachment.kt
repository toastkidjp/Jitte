/*
 * Copyright (c) 2018 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.tab.tab_list

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Drag attachment to specified [RecyclerView].
 *
 * @author toastkidjp
 */
object DragAttachment {

    /**
     * Invoke action.
     *
     * @param recyclerView [RecyclerView]
     * @param direction [ItemTouchHelper]'s constant
     */
    operator fun invoke(recyclerView: RecyclerView, direction: Int): ItemTouchHelper {
        val itemTouchHelper = ItemTouchHelper(
                object : ItemTouchHelper.SimpleCallback(direction, 0) {
                    override fun onMove(
                            rv: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder,
                            target: RecyclerView.ViewHolder
                    ): Boolean {
                        val adapter = recyclerView.adapter
                        if (adapter is Adapter) {
                            adapter.swap(viewHolder.adapterPosition, target.adapterPosition)
                        }
                        return true
                    }

                    override fun onSwiped(
                            viewHolder: RecyclerView.ViewHolder,
                            direction: Int
                    ) = Unit
                }
        )
        itemTouchHelper.attachToRecyclerView(recyclerView)
        return itemTouchHelper
    }
}