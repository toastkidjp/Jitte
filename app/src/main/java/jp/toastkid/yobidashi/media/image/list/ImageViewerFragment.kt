/*
 * Copyright (c) 2019 toastkidjp.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompany this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html.
 */
package jp.toastkid.yobidashi.media.image.list

import android.Manifest
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.CommonFragmentAction
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.browser.page_search.PageSearcherViewModel
import jp.toastkid.yobidashi.databinding.FragmentImageViewerBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.main.ContentScrollable
import jp.toastkid.yobidashi.media.image.setting.ExcludingSettingFragment
import timber.log.Timber

/**
 * @author toastkidjp
 */
class ImageViewerFragment : Fragment(), CommonFragmentAction, ContentScrollable {

    private lateinit var binding: FragmentImageViewerBinding

    private lateinit var bucketLoader: BucketLoader

    private lateinit var imageLoader: ImageLoader

    private lateinit var preferenceApplier: PreferenceApplier

    private var adapter: Adapter? = null

    private var currentBucket: String? = null

    private val parentExtractor = ParentExtractor()

    private val disposables = CompositeDisposable()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, LAYOUT_ID, container, false)
        binding.fragment = this
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        preferenceApplier = PreferenceApplier(context)

        val viewModelProvider = ViewModelProviders.of(this)
        val viewModel =
                viewModelProvider.get(ImageViewerFragmentViewModel::class.java)

        adapter = Adapter(fragmentManager, viewModel)

        viewModel.onClick.observe(this, Observer {
            Completable.fromAction { loadImages(it) }
                    .subscribeOn(Schedulers.io())
                    .subscribe()
                    .addTo(disposables)
        })

        viewModel.onLongClick.observe(this, Observer {
            preferenceApplier.addExcludeItem(it)
            loadImages()
        })

        viewModel.refresh.observe(this, Observer {
            attemptLoad()
        })

        observePageSearcherViewModel()

        val contentResolver = context.contentResolver ?: return
        bucketLoader = BucketLoader(contentResolver)
        imageLoader = ImageLoader(contentResolver)

        binding.images.adapter = adapter
        binding.images.layoutManager =
                GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
    }

    private fun observePageSearcherViewModel() {
        val activity = activity ?: return
        ViewModelProviders.of(activity).get(PageSearcherViewModel::class.java)
                .also { viewModel ->
                    viewModel.find.observe(activity, Observer {
                        Timber.i("observe $it")
                        filterByName(it)
                    })
                }
    }

    fun reset() {
        MediaControllerCompat.getMediaController(requireActivity()).transportControls.stop()
    }

    override fun pressBack(): Boolean {
        if (adapter?.isBucketMode() == true) {
            activity?.supportFragmentManager?.popBackStack()
        } else {
            loadImages()
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        attemptLoad()
    }

    private fun attemptLoad() {
        RxPermissions(requireActivity())
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .observeOn(Schedulers.io())
                .subscribe(
                        {
                            if (it) {
                                loadImages()
                                return@subscribe
                            }

                            Toaster.snackShort(
                                    binding.root,
                                    R.string.message_audio_file_is_not_found,
                                    PreferenceApplier(binding.root.context).colorPair()
                            )
                            activity?.supportFragmentManager?.popBackStack()
                        },
                        Timber::e
                )
                .addTo(disposables)
    }

    private fun loadImages(bucket: String? = null) {
        adapter?.clear()

        val excludedItemFilter = ExcludingItemFilter(preferenceApplier.excludedItems())

        val sort = preferenceApplier.imageViewerSort()

        if (bucket.isNullOrBlank()) {
            bucketLoader(sort)
                    .filter { excludedItemFilter(parentExtractor(it.path)) }
        } else {
            imageLoader(sort, bucket).filter { excludedItemFilter(it.path) }
        }
                .forEach { adapter?.add(it) }
        currentBucket = bucket
        refreshContent()
    }

    private fun filterByName(keyword: String?) {
        if (keyword.isNullOrBlank()) {
            loadImages()
            return
        }

        adapter?.clear()

        val excludedItemFilter = ExcludingItemFilter(preferenceApplier.excludedItems())

        imageLoader.filterBy(keyword)
                .filter { excludedItemFilter(it.path) }
                .forEach { adapter?.add(it) }
        currentBucket = null
        refreshContent()
    }

    private fun refreshContent() {
        activity?.runOnUiThread {
            adapter?.notifyDataSetChanged()
            RecyclerViewScroller.toTop(binding.images, adapter?.itemCount ?: 0)
        }
    }

    override fun toTop() {
        RecyclerViewScroller.toTop(binding.images, adapter?.itemCount ?: 0)
    }

    override fun toBottom() {
        RecyclerViewScroller.toBottom(binding.images, adapter?.itemCount ?: 0)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater?.inflate(R.menu.image_viewer, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.excluding_items_setting -> {
                val fragment = ExcludingSettingFragment()
                fragment.setTargetFragment(this, 1)
                fragment.show(fragmentManager, "setting")
            }
            R.id.sort_by_date -> {
                preferenceApplier.setImageViewerSort(Sort.DATE)
                loadImages(currentBucket)
            }
            R.id.sort_by_name -> {
                preferenceApplier.setImageViewerSort(Sort.NAME)
                loadImages(currentBucket)
            }
            R.id.sort_by_count -> {
                preferenceApplier.setImageViewerSort(Sort.ITEM_COUNT)
                loadImages(currentBucket)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    companion object {

        @LayoutRes
        private val LAYOUT_ID = R.layout.fragment_image_viewer
    }
}