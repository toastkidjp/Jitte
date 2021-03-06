package jp.toastkid.yobidashi.main

import androidx.annotation.IdRes
import androidx.annotation.StringRes
import jp.toastkid.yobidashi.R

/**
 * Start-up view definition.
 *
 * @param titleId
 * @param radioButtonId
 *
 * @author toastkidjp
 */
enum class StartUp(@StringRes val titleId: Int, @IdRes val radioButtonId: Int) {
    SEARCH(R.string.title_search, R.id.start_up_search),
    BROWSER(R.string.title_browser, R.id.start_up_browser),
    BOOKMARK(R.string.title_bookmark, R.id.start_up_bookmark);

    companion object {

        /**
         * Find value by name.
         *
         * @param name [String]
         */
        fun findByName(name: String?): StartUp = if (name.isNullOrEmpty()) getDefault() else valueOf(name)

        /**
         * Find value by ID.
         *
         * @param checkedRadioButtonId [Int]
         */
        fun findById(@IdRes checkedRadioButtonId: Int): StartUp =
                values().find { it.radioButtonId == checkedRadioButtonId } ?: getDefault()

        /**
         * Return default value.
         */
        private fun getDefault(): StartUp = SEARCH
    }
}