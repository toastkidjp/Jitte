/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.popup

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.media.session.PlaybackState
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.PopupMediaPlayerBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.media.Adapter
import jp.toastkid.yobidashi.media.MediaPlayerService
import timber.log.Timber

/**
 * @author toastkidjp
 */
class MediaPlayerPopup(private val context: Context) {

    /**
     * Popup window.
     */
    private val popupWindow = PopupWindow(context)

    /**
     * View binding.
     */
    private val binding: PopupMediaPlayerBinding

    private var adapter: Adapter? = null

    private lateinit var mediaBrowser: MediaBrowserCompat

    private val heightPixels = context.resources.displayMetrics.heightPixels

    private val headerHeight = context.resources.getDimensionPixelSize(R.dimen.floating_preview_header_height)

    private val swipeLimit = heightPixels - headerHeight

    private val subscriptionCallback = object : MediaBrowserCompat.SubscriptionCallback() {

        override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
        ) {
            adapter?.clear()

            attemptMediaController()?.also {
                it.transportControls?.prepare()
            }
            children.forEach { adapter?.add(it) }

            attemptExtractActivity()?.runOnUiThread { adapter?.notifyDataSetChanged() }
        }
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            attemptExtractActivity()?.also {
                MediaControllerCompat.setMediaController(
                        it,
                        MediaControllerCompat(context, mediaBrowser.sessionToken)
                )
            }

            mediaBrowser.subscribe(mediaBrowser.root, subscriptionCallback)
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            when (state?.state) {
                PlaybackStateCompat.STATE_PLAYING -> {
                    //binding.play.setImageResource(R.drawable.ic_pause)
                }
                else -> {
                    //binding.play.setImageResource(R.drawable.ic_play_media)
                }
            }
        }
    }

    private val disposables = CompositeDisposable()

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.popup_media_player, null, false)
        binding.popup = this

        val preferenceApplier = PreferenceApplier(context)
        adapter = Adapter(LayoutInflater.from(context), preferenceApplier) {
            attemptMediaController()
                    ?.transportControls
                    ?.playFromUri(it.description.mediaUri, bundleOf())
        }

        binding.mediaList.adapter = adapter
        binding.mediaList.layoutManager =
                LinearLayoutManager(context, RecyclerView.VERTICAL, false)

        val stopBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_stop)
        val playBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_play)

        val colorPair = preferenceApplier.colorPair()

        binding.playSwitch.setOnClickListener {
            val mediaController = attemptMediaController() ?: return@setOnClickListener
            val icon = if (mediaController.playbackState.state == PlaybackState.STATE_PLAYING) {
                mediaController.transportControls.pause()
                playBitmap
            } else if (mediaController.playbackState.state == PlaybackState.STATE_PAUSED) {
                mediaController.transportControls.play()
                stopBitmap
            } else {
                playBitmap
            }
            binding.playSwitch.setImageBitmap(icon)
            binding.playSwitch.setColorFilter(colorPair.fontColor())
        }

        binding.control.setBackgroundColor(colorPair.bgColor())
        binding.reset.setColorFilter(colorPair.fontColor())
        binding.playSwitch.setColorFilter(colorPair.fontColor())

        popupWindow.contentView = binding.root

        popupWindow.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(context, R.color.transparent)))

        popupWindow.isClippingEnabled = false

        popupWindow.width = WindowManager.LayoutParams.MATCH_PARENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT

        setSlidingListener()

        mediaBrowser = MediaBrowserCompat(
                context,
                ComponentName(context, MediaPlayerService::class.java),
                connectionCallback,
                null
        )
        /*if (context is FragmentActivity) {
            popupViewModel =
                    ViewModelProviders.of(context).get(BarcodeReaderResultPopupViewModel::class.java)
        }*/
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setSlidingListener() {
        val slidingTouchListener = SlidingTouchListener(binding.contentPanel)
        slidingTouchListener.setCallback(object : SlidingTouchListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                if (y > swipeLimit) {
                    return
                }
                popupWindow.update(-1, -y.toInt(), -1, -1)
            }
        })
        binding.header.setOnTouchListener(slidingTouchListener)
    }

    fun show(parent: View) {
        if (popupWindow.isShowing) {
            popupWindow.dismiss()
            return
        }

        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, -600)

        val attemptExtractActivity = attemptExtractActivity() ?: return
        RxPermissions(attemptExtractActivity)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it) {
                                mediaBrowser.connect()
                                return@subscribe
                            }

                            Toaster.snackShort(
                                    parent,
                                    R.string.message_audio_file_is_not_found,
                                    PreferenceApplier(binding.root.context).colorPair()
                            )
                            popupWindow.dismiss()
                        },
                        Timber::e
                )
                .addTo(disposables)
    }

    fun hide() {
        popupWindow.dismiss()

        attemptMediaController()?.also {
            it.unregisterCallback(controllerCallback)
            mediaBrowser.disconnect()
        }

        disposables.clear()
    }

    fun reset() {
        attemptMediaController()?.transportControls?.stop()
    }

    private fun attemptMediaController() =
            attemptExtractActivity()?.let { MediaControllerCompat.getMediaController(it) }

    private fun attemptExtractActivity() = (context as? Activity)

    companion object {
        private const val DURATION_MS = 200L
    }
}