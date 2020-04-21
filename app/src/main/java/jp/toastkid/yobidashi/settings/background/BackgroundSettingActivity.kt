package jp.toastkid.yobidashi.settings.background

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityBackgroundSettingBinding
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.FilesDir
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier

/**
 * Background settings.
 *
 * @author toastkidjp
 */
class BackgroundSettingActivity : AppCompatActivity(), ClearImagesDialogFragment.Callback {

    /**
     * Data Binding object.
     */
    private var binding: ActivityBackgroundSettingBinding? = null

    /**
     * ModuleAdapter.
     */
    private var adapter: Adapter? = null

    /**
     * Wrapper of FilesDir.
     */
    private lateinit var filesDir: FilesDir

    private lateinit var preferenceApplier: PreferenceApplier

    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView<ActivityBackgroundSettingBinding>(this, LAYOUT_ID)
        binding?.let {
            it.activity = this
        }

        binding?.toolbar?.also { toolbar ->
            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.setTitle(titleId())
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.inflateMenu(R.menu.background_setting_menu)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }
        }

        filesDir = FilesDir(this, BACKGROUND_DIR)

        initImagesView()
    }

    /**
     * Initialize images RecyclerView.
     */
    private fun initImagesView() {
        binding?.let {
            it.imagesView.layoutManager = GridLayoutManager(
                    this,
                    2,
                    LinearLayoutManager.HORIZONTAL,
                    false
            )
            adapter = Adapter(preferenceApplier, filesDir)
            it.imagesView.adapter = adapter
            if (adapter?.itemCount == 0) {
                Toaster.withAction(
                        it.imagesView,
                        getString(R.string.message_snackbar_suggestion_select_background_image),
                        R.string.select,
                        View.OnClickListener { launchAdding() },
                        preferenceApplier.colorPair()
                        )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.toolbar?.let { ToolbarColorApplier()(window, it, preferenceApplier.colorPair()) }
    }

    private fun clickMenu(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.background_settings_toolbar_menu_add -> {
                binding?.fab?.let { launchAdding() }
                true
            }
            R.id.background_settings_toolbar_menu_clear -> {
                clearImages()
                true
            }
            R.id.menu_exit -> {
                moveTaskToBack(true)
                true
            }
            R.id.menu_close -> {
                finish()
                true
            }
            else -> true
        }
    }

    /**
     * Launch Adding action.
     */
    fun launchAdding() {
        startActivityForResult(IntentFactory.makePickImage(), IMAGE_READ_REQUEST)
    }

    /**
     * Clear all images.
     */
    private fun clearImages() {
        ClearImagesDialogFragment().show(
                supportFragmentManager,
                ClearImagesDialogFragment::class.java.simpleName
        )
    }

    override fun onClickClearImages() {
        filesDir.clean()
        Toaster.snackShort(
                binding?.fabParent as View,
                getString(R.string.message_success_image_removal),
                preferenceApplier.colorPair()
        )
        adapter?.notifyDataSetChanged()
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ) {

        if (requestCode == IMAGE_READ_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null) {
            LoadedAction(data, binding?.fabParent as View, preferenceApplier.colorPair(), { adapter?.notifyDataSetChanged() })
                    .invoke()
                    .addTo(disposables)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    @StringRes
    private fun titleId(): Int = R.string.title_background_image_setting

    companion object {

        /**
         * Background image dir.
         */
        const val BACKGROUND_DIR: String = "background_dir"

        /**
         * Request code.
         */
        private const val IMAGE_READ_REQUEST: Int = 136

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_background_setting

        /**
         * Make launcher intent.
         * @param context Context
         *
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent =
                Intent(context, BackgroundSettingActivity::class.java)
                        .apply { addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP) }
    }

}