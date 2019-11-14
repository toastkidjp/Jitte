package jp.toastkid.yobidashi.settings.color

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.libs.db.DatabaseFinder
import timber.log.Timber
import java.util.*

/**
 * @author toastkidjp
 */
object SavedColors {

    /**
     * Insert default colors.
     *
     * @param context
     */
    @SuppressLint("CheckResult")
    fun insertDefaultColors(context: Context) {
        Completable.fromAction {
            val repository = DatabaseFinder().invoke(context).savedColorRepository()
            DefaultColors.make(context).forEach { repository.add(it) }
        }
                .subscribeOn(Schedulers.io())
                .subscribe(
                        {},
                        Timber::e
                )
    }

    /**
     * Insert random colors.
     *
     * @param context
     */
    fun insertRandomColors(context: Context): Disposable {

        val random = Random()

        val bg = Color.argb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        )

        val font = Color.argb(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
        )

        return Completable.fromAction {
            DatabaseFinder().invoke(context)
                    .savedColorRepository()
                    .add(SavedColor.make(bg, font))
        }
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

}
