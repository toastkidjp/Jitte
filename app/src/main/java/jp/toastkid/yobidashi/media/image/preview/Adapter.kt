/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.preview

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.chrisbanes.photoview.PhotoView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.media.image.Image

/**
 * @author toastkidjp
 */
class Adapter : RecyclerView.Adapter<ViewHolder>() {

    private val images = mutableListOf<Image>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = PhotoView(parent.context)
        view.layoutParams = layoutParams.also {
            val horizontalMargin =
                    parent.resources.getDimensionPixelSize(R.dimen.image_viewer_item_horizontal_margin)
            it.leftMargin = horizontalMargin
            it.rightMargin = horizontalMargin
        }
        view.maximumScale = 15f
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setImage(images[position].path)
    }

    override fun getItemCount(): Int = images.size

    fun setImages(images: List<Image>) {
        this.images.addAll(images)
    }

    fun getPath(position: Int): String? {
        return images.get(position).path
    }

    companion object {
        private val layoutParams = ViewGroup.MarginLayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}