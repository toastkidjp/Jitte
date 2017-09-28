package jp.toastkid.yobidashi.color_filter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.annotation.ColorInt
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.view.View
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.preference.ColorPair
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * Overlay color filter facade.
 *
 * Initialize with two parent object.
 * @param activity Service's parent activity
 * @param parent   Snackbar's parent
 * @author toastkidjp
 */
class ColorFilter(private val activity: Activity, private val parent: View) {

    /**
     * Snackbar's color.
     */
    private val colorPair: ColorPair = PreferenceApplier(activity).colorPair()

    /**
     * Start color filter.
     *
     * @return
     */
    fun start() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(activity)) {
            snackShort(R.string.message_cannot_draw_overlay)
            return
        }

        snackShort(R.string.message_enable_color_filter)
        ColorFilterService.start(activity)
    }

    /**
     * Stop color filter.
     *
     * @return
     */
    fun stop(): Boolean {
        snackShort(R.string.message_stop_color_filter)
        ColorFilterService.stop(activity)
        return true
    }

    /**
     * Show short time {@link Snackbar}.
     *
     * @param messageId Message resource ID
     */
    private fun snackShort(@StringRes messageId: Int) {
        Toaster.snackShort(parent, messageId, colorPair)
    }

    /**
     * Set color.
     *
     * @param Color value int.
     */
    fun color(@ColorInt color: Int) {
        ColorFilterService.color(color)
    }

    /**
     * Switch color filter's state.
     *
     * @param fragment
     * @param requestCode
     */
    fun switchState(fragment: Fragment, requestCode: Int): Boolean {
        val preferenceApplier = PreferenceApplier(fragment.activity)
        val newState = !preferenceApplier.useColorFilter()
        if (!newState) {
            stop()
            preferenceApplier.setUseColorFilter(newState)
            return newState
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !Settings.canDrawOverlays(fragment.activity)) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + fragment.activity.packageName))
            fragment.startActivityForResult(intent, requestCode)
            return !newState
        }
        start()
        preferenceApplier.setUseColorFilter(newState)
        return newState
    }
}
