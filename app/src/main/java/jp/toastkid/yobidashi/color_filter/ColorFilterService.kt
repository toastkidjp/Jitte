package jp.toastkid.yobidashi.color_filter

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.support.v4.app.JobIntentService
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import jp.toastkid.yobidashi.R

/**
 * Overlay Color filter.
 *
 * @author toastkidjp
 */
class ColorFilterService : JobIntentService() {

    override fun onHandleWork(intent: Intent) {

        if (running) {
            handler.post {
                windowManager?.removeView(filterView)
                running = false
            }
            return
        }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                getWindowType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        )

        if (filterView == null) {
            filterView = LayoutInflater.from(this).inflate(R.layout.color_filter, null)
        }

        handler.post {
            windowManager?.addView(filterView, layoutParams)
            running = true
        }
    }

    private fun getWindowType(): Int =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY

    companion object {

        /** Job ID. */
        private const val JOB_ID = 0x001

        /** Color filter view.  */
        private var filterView: View? = null

        /** Window manager.  */
        private var windowManager: WindowManager? = null

        /** Use for switching view visibility. */
        private val handler = Handler(Looper.getMainLooper())

        /** Flag of running state. */
        private var running: Boolean = false

        /**
         * Draw filter.
         */
        internal fun start(context: Context) {
            running = false
            enqueue(context)
        }

        /**
         * Remove filter.
         */
        internal fun stop(context: Context) {
            running = true
            enqueue(context)
        }

        private fun enqueue(context: Context) {
            enqueueWork(
                    context,
                    ColorFilterService::class.java,
                    JOB_ID,
                    Intent(context, ColorFilterService::class.java)
            )
        }
    }
}
