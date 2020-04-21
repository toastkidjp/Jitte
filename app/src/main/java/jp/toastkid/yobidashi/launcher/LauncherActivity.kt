package jp.toastkid.yobidashi.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityLauncherBinding
import jp.toastkid.yobidashi.libs.EditTextColorSetter
import jp.toastkid.yobidashi.libs.ImageLoader
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.view.RecyclerViewScroller
import jp.toastkid.yobidashi.libs.view.ToolbarColorApplier
import jp.toastkid.yobidashi.settings.SettingsActivity

/**
 * App Launcher.
 *
 * @author toastkidjp
 */
class LauncherActivity : AppCompatActivity() {

    /**
     * Binding object.
     */
    private val binding: ActivityLauncherBinding by lazy {
        DataBindingUtil.setContentView<ActivityLauncherBinding>(this, LAYOUT_ID)
    }

    /**
     * Adapter.
     */
    private val adapter by lazy {
        Adapter(this, binding.toolbar)
    }

    private lateinit var preferenceApplier: PreferenceApplier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)
        
        binding.toolbar.also { toolbar ->
            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.setTitle(R.string.title_apps_launcher)
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.inflateMenu(R.menu.launcher)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }
        }

        binding.appItemsView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        binding.appItemsView.adapter = adapter
        binding.appItemsView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                if (!binding.filter.hasFocus()) {
                    return false
                }
                Inputs.hideKeyboard(binding.filter)
                binding.appItemsView.requestFocus()
                return false
            }
        }
        initInput(adapter)
    }

    /**
     * Initialize input.
     *
     * @param adapter [Adapter]
     */
    private fun initInput(adapter: Adapter) {
        binding.filter.addTextChangedListener(object : TextWatcher {
            var prev: String = ""
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (TextUtils.equals(prev, s)) {
                    return
                }
                prev = s.toString()
                adapter.filter(s.toString())
                binding.appItemsView.scheduleLayoutAnimation()
            }

            override fun afterTextChanged(s: Editable) = Unit
        })
    }

    override fun onResume() {
        super.onResume()
        val colorPair = preferenceApplier.colorPair()
        ToolbarColorApplier()(window, binding.toolbar, colorPair)
        val fontColor = colorPair.fontColor()
        EditTextColorSetter().invoke(binding.filter, fontColor)
        binding.inputBorder.setBackgroundColor(fontColor)
        ImageLoader.setImageToImageView(binding.background, preferenceApplier.backgroundImagePath)
    }

    private fun clickMenu(item: MenuItem): Boolean {
        @IdRes val itemId: Int = item.itemId

        val itemCount = binding.appItemsView.adapter?.itemCount ?: 0

        when (itemId) {
            R.id.setting -> {
                startActivity(SettingsActivity.makeIntent(this))
                return true
            }
            R.id.to_top -> {
                RecyclerViewScroller.toTop(binding.appItemsView, itemCount)
                return true
            }
            R.id.to_bottom -> {
                RecyclerViewScroller.toBottom(binding.appItemsView, itemCount)
                return true
            }
            R.id.menu_exit -> {
                moveTaskToBack(true)
                return true
            }
            R.id.menu_close -> {
                finish()
                return true
            }
            else -> return true
        }
    }

    override fun onPause() {
        super.onPause()
        Inputs.hideKeyboard(binding.filter)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.dispose()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.activity_launcher

        /**
         * Make launcher intent.
         *
         * @param context
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, LauncherActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}