package jp.toastkid.yobidashi.barcode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.objects.FirebaseVisionObject
import com.google.firebase.ml.vision.objects.FirebaseVisionObjectDetectorOptions
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.SourceData
import com.journeyapps.barcodescanner.camera.PreviewCallback
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.barcode.detection.Adapter
import jp.toastkid.yobidashi.barcode.detection.Detection
import jp.toastkid.yobidashi.databinding.ActivityBarcodeReaderBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Toaster
import jp.toastkid.yobidashi.libs.clip.Clipboard
import jp.toastkid.yobidashi.libs.intent.IntentFactory
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.libs.storage.ExternalFileAssignment
import jp.toastkid.yobidashi.libs.view.DraggableTouchListener
import jp.toastkid.yobidashi.search.SearchAction
import timber.log.Timber
import java.io.FileOutputStream

/**
 * Barcode reader activity.
 * TODO Clean up code.
 * @author toastkidjp
 */
class BarcodeReaderActivity : AppCompatActivity() {

    /**
     * Data Binding object.
     */
    private var binding: ActivityBarcodeReaderBinding? = null

    /**
     * Animation of slide up bottom.
     */
    private val slideUpBottom by lazy { AnimationUtils.loadAnimation(this, R.anim.slide_up) }

    private lateinit var preferenceApplier: PreferenceApplier

    private val objectDetector = FirebaseVision.getInstance()
            .getOnDeviceObjectDetector(
                    FirebaseVisionObjectDetectorOptions.Builder()
                            .setDetectorMode(FirebaseVisionObjectDetectorOptions.SINGLE_IMAGE_MODE)
                            .enableMultipleObjects()
                            .enableClassification()
                            .build()
            )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)

        preferenceApplier = PreferenceApplier(this)

        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        binding?.activity = this
        binding?.toolbar?.let { toolbar ->
            setTitle(R.string.title_camera)
            toolbar.inflateMenu(R.menu.camera)
            toolbar.inflateMenu(R.menu.settings_toolbar_menu)
            toolbar.setOnMenuItemClickListener{ clickMenu(it) }

            toolbar.setNavigationIcon(R.drawable.ic_back)
            toolbar.setNavigationOnClickListener { finish() }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), 1)
            return
        }

        initializeFab()
        startDecode()
    }

    fun clickMenu(item: MenuItem) = when (item.itemId) {
        R.id.reset_fab_position -> {
            binding?.camera?.also {
                it.translationX = 0f
                it.translationY = 0f
                preferenceApplier.clearCameraFabPosition()
            }
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

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeFab() {
        val draggableTouchListener = DraggableTouchListener()
        draggableTouchListener.setCallback(object : DraggableTouchListener.OnNewPosition {
            override fun onNewPosition(x: Float, y: Float) {
                preferenceApplier.setNewCameraFabPosition(x, y)
            }
        })
        binding?.camera?.setOnTouchListener(draggableTouchListener)

        binding?.camera?.also {
            val position = preferenceApplier.cameraFabPosition() ?: return@also
            it.animate()
                    .x(position.first)
                    .y(position.second)
                    .setDuration(10)
                    .start()
        }
    }

    /**
     * Start decode.
     */
    private fun startDecode() {
        binding?.barcodeView?.decodeContinuous(object : BarcodeCallback {

            override fun barcodeResult(barcodeResult: BarcodeResult) {
                val text = barcodeResult.text
                if (TextUtils.equals(text, getResultText())) {
                    return
                }
                @Suppress("UsePropertyAccessSyntax")
                binding?.result?.setText(text)
                binding?.result?.isVisible = true
                binding?.detections?.isVisible = false
                showResult()
            }

            override fun possibleResultPoints(list: List<ResultPoint>) = Unit
        })
    }

    /**
     * Copy result text to clipboard.
     */
    fun clip(snackbarParent: View) {
        getResultText()?.let {
            Clipboard.clip(this, it)
            Toaster.snackShort(snackbarParent, it, preferenceApplier.colorPair())
        }
    }

    /**
     * Share result text.
     */
    fun share() {
        getResultText()?.let { startActivity(IntentFactory.makeShare(it)) }
    }

    /**
     * Open result text with browser.
     */
    fun open() {
        getResultText()?.let {
            SearchAction(this, preferenceApplier.getDefaultSearchEngine(), it).invoke()
        }
    }

    fun camera() {
        val barcodeView = binding?.barcodeView ?: return

        barcodeView.barcodeView?.cameraInstance?.requestPreview(object : PreviewCallback {
            override fun onPreview(sourceData: SourceData?) {
                val output = ExternalFileAssignment().assignFile(
                        this@BarcodeReaderActivity,
                        "shoot_${System.currentTimeMillis()}.png"
                )

                sourceData?.cropRect = getRect()
                sourceData?.bitmap?.let {
                    it.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(output))
                    detectObject(FirebaseVisionImage.fromBitmap(it))
                }

                Toaster.snackShort(
                        barcodeView,
                        "Camera saved: ${output.absolutePath}",
                        preferenceApplier.colorPair()
                )
            }

            private fun getRect(): Rect {
                val rect = Rect()
                windowManager.defaultDisplay.getRectSize(rect)
                return rect
            }

            override fun onPreviewError(e: Exception?) {
                Timber.e(e)
            }

        })
    }

    private fun detectObject(image: FirebaseVisionImage) {
        objectDetector.processImage(image)
                .addOnSuccessListener { detectedObjects ->
                    if (detectedObjects.isEmpty()) {
                        return@addOnSuccessListener
                    }

                    val recyclerView = binding?.detections
                    recyclerView?.layoutManager =
                            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                    val adapter = Adapter(this)
                    recyclerView?.adapter = adapter
                    detectedObjects.map {
                        val category = when (it.classificationCategory) {
                            FirebaseVisionObject.CATEGORY_FASHION_GOOD -> "Fashion"
                            FirebaseVisionObject.CATEGORY_FOOD -> "Food"
                            FirebaseVisionObject.CATEGORY_HOME_GOOD -> "Home goods"
                            FirebaseVisionObject.CATEGORY_PLACE -> "Place"
                            FirebaseVisionObject.CATEGORY_PLANT -> "Plant"
                            else -> ""
                        }
                        makeDetection(image.bitmap, it.boundingBox, category)
                    }
                            .filter { it.category.isNotBlank() }
                            .forEach { adapter.add(it) }
                    if (adapter.isEmpty()) {
                        return@addOnSuccessListener
                    }
                    binding?.detections?.isVisible = true
                    binding?.result?.isVisible = false
                    adapter.notifyDataSetChanged()
                    showResult()
                }
                .addOnFailureListener { Timber.e(it) }
    }

    private fun makeDetection(bitmap: Bitmap, boundingBox: Rect, category: String): Detection {
        return Detection(
                Bitmap.createBitmap(
                        bitmap,
                        boundingBox.left,
                        boundingBox.top,
                        boundingBox.width(),
                        boundingBox.height()
                ),
                category
        )
    }

    /**
     * Get result text.
     */
    private fun getResultText(): String? = binding?.result?.text?.toString()

    /**
     * Show result with snackbar.
     */
    private fun showResult() {
        binding?.resultArea?.let {
            if (it.visibility != View.VISIBLE) {
                it.visibility = View.VISIBLE
            }
            it.startAnimation(slideUpBottom)
        }
    }

    override fun onResume() {
        super.onResume()
        binding?.barcodeView?.resume()
        val colorPair = preferenceApplier.colorPair()
        binding?.toolbar?.setTitleTextColor(colorPair.fontColor())
        binding?.let {
            it.resultArea.setBackgroundColor(colorPair.bgColor())
            Colors.setColors(it.clip, colorPair)
            Colors.setColors(it.share, colorPair)
            Colors.setColors(it.open, colorPair)
            Colors.setColors(it.result, colorPair)
        }
    }

    override fun onPause() {
        super.onPause()
        binding?.barcodeView?.pause()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startDecode()
            return
        }
        Toaster.tShort(this, R.string.message_requires_permission_camera)
        finish()
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_barcode_reader

        /**
         * Make this activity's intent.
         *
         * @param context
         * @return [Intent]
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, BarcodeReaderActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}
