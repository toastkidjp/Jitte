package jp.toastkid.yobidashi.browser.archive

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ActivityArchivesBinding
import jp.toastkid.yobidashi.libs.Toaster
import java.io.File

/**
 * Activity of archives.
 *
 * @author toastkidjp
 */
class ArchivesActivity : AppCompatActivity() {

    private var binding: ActivityArchivesBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(LAYOUT_ID)
        binding = DataBindingUtil.setContentView(this, LAYOUT_ID)
        binding?.archivesView?.layoutManager = LinearLayoutManager(this)

        val adapter = Adapter(this) { filePath ->
            setResult(
                    Activity.RESULT_OK,
                    Intent().also { it.putExtra(EXTRA_KEY_FILE_NAME, filePath) }
            )
            finish()
        }

        if (adapter.itemCount == 0) {
            finish()
            Toaster.tShort(this, R.string.message_empty_archives)
        }
        binding?.archivesView?.adapter = adapter
    }

    companion object {

        const val REQUEST_CODE = 0x0040

        @LayoutRes
        private const val LAYOUT_ID = R.layout.activity_archives

        private const val EXTRA_KEY_FILE_NAME = "FILE_NAME"


        fun extractFile(intent: Intent) =
                File(intent.getStringExtra(EXTRA_KEY_FILE_NAME))

        fun extractFileUrl(data: Intent): String =
                Uri.fromFile(extractFile(data)).toString()

        /**
         * Make launcher intent.
         * @param context
         *
         * @return
         */
        fun makeIntent(context: Context): Intent {
            val intent = Intent(context, ArchivesActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            return intent
        }
    }
}