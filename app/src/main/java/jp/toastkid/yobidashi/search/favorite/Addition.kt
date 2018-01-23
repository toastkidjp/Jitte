package jp.toastkid.yobidashi.search.favorite

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.annotation.LayoutRes
import android.support.design.widget.TextInputLayout
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.FavoriteSearchAdditionDialogContentBinding
import jp.toastkid.yobidashi.libs.Colors
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import jp.toastkid.yobidashi.search.SearchCategorySpinnerInitializer
import java.text.MessageFormat

/**
 * Show input dialog and call inserting action.
 *
 * @param parent For using extract background color.
 * @param toasterCallback Callback
 * @author toastkidjp
 */
class Addition internal constructor(
        private val parent: ViewGroup,
        private val toasterCallback: (String) -> Unit
) {

    /**
     * Context.
     */
    private val context: Context = parent.context

    /**
     * Binding object.
     */
    private val binding: FavoriteSearchAdditionDialogContentBinding

    /**
     * Search category selector.
     */
    private val categorySelector: Spinner

    /**
     * Input area.
     */
    private val input: EditText

    init {
        binding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                LAYOUT_ID,
                parent,
                false
        )
        binding.action = this
        val content = binding.root

        categorySelector = initSpinner(content)

        input = initInput(content)

        if (parent.childCount == 0) {
            parent.addView(content)
        }
    }

    /**
     * Show input dialog.
     */
    internal operator fun invoke() {
        val colorPair = PreferenceApplier(context).colorPair()
        Colors.setColors(binding.close, colorPair)
        Colors.setColors(binding.add, colorPair)
        parent.visibility = View.VISIBLE
    }

    /**
     * Initialize spinner.
     *
     * @param content
     * @return
     */
    private fun initSpinner(content: View): Spinner {
        val categorySelector = content.findViewById<Spinner>(R.id.favorite_search_addition_categories)
        SearchCategorySpinnerInitializer.invoke(categorySelector)
        return categorySelector
    }

    /**
     * Initialize input field.
     *
     * @param content
     * @return
     */
    private fun initInput(content: View): EditText {
        val inputLayout = content.findViewById<TextInputLayout>(R.id.favorite_search_addition_query)

        return TextInputs.setEmptyAlert(inputLayout)
    }

    /**
     * Cancel action.
     *
     * @param ignored for Data Binding
     */
    fun cancel(ignored: View) {
        parent.visibility = View.GONE
        Inputs.hideKeyboard(input)
    }

    /**
     * Ok action.
     *
     * @param ignored for Data Binding
     */
    fun ok(ignored: View) {
        val query = input.text.toString()

        if (TextUtils.isEmpty(query)) {
            toasterCallback(context.getString(R.string.favorite_search_addition_dialog_empty_message))
            return
        }

        val category = categorySelector.selectedItem.toString()

        FavoriteSearchInsertion(context, category, query).invoke()

        val message = MessageFormat.format(
                context.getString(R.string.favorite_search_addition_successful_format),
                query
        )
        toasterCallback(message)
    }

    companion object {

        /**
         * Layout ID.
         */
        @LayoutRes
        private const val LAYOUT_ID: Int = R.layout.favorite_search_addition_dialog_content
    }
}