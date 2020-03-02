/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image

import android.net.Uri
import android.text.TextUtils
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ItemImageThumbnailsBinding
import jp.toastkid.yobidashi.libs.BitmapScaling
import jp.toastkid.yobidashi.libs.ImageLoader
import timber.log.Timber
import java.io.File

/**
 * Extended of [RecyclerView.ViewHolder].
 *
 * @param binding Binding object.
 *
 * @author toastkidjp
 */
internal class ViewHolder(private val binding: ItemImageThumbnailsBinding)
    : RecyclerView.ViewHolder(binding.root) {

    private val width = binding.image.layoutParams.width.toDouble()

    private val height = binding.image.layoutParams.height.toDouble()

    /**
     * Apply file content.
     *
     * @param image image
     */
    fun applyContent(image: Image, onClick: (Image) -> Unit) {
        setImageTo(this.binding.image, image.path)
        this.binding.text.text = image.name
        this.binding.root.setOnClickListener {
            onClick(image)
        }
    }

    private fun setImageTo(iv: ImageView, imagePath: String) {
        if (TextUtils.isEmpty(imagePath)) {
            iv.setImageResource(R.drawable.ic_image_search)
            return
        }

        Maybe.fromCallable {
            ImageLoader.loadBitmap(
                    iv.context,
                    Uri.parse(File(imagePath).toURI().toString())
            )
        }
                .subscribeOn(Schedulers.io())
                .map { BitmapScaling(it, 300.0, 300.0) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        iv::setImageBitmap,
                        Timber::e
                )
    }

}