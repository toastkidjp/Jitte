package jp.toastkid.yobidashi.browser.page_search

import android.app.Activity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import androidx.core.view.isVisible
import io.reactivex.Maybe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.databinding.ModuleSearcherBinding
import jp.toastkid.yobidashi.libs.Inputs
import jp.toastkid.yobidashi.libs.TextInputs
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier
import timber.log.Timber

/**
 * Module for find in page.
 *
 * @param binding [ModuleSearcherBinding]
 * @param view [PageSearcherContract.View]
 *
 * @author toastkidjp
 */
class PageSearcherModule(
        private val binding: ModuleSearcherBinding,
        private val view: PageSearcherContract.View
) : PageSearcherContract.Presenter {

    /**
     * View context.
     */
    private val context = binding.root.context

    /**
     * This value is used by show/hide animation.
     */
    private val height = context.resources.getDimension(R.dimen.toolbar_height)

    /**
     * Use for open software keyboard.
     */
    private val editText: EditText

    /**
     * Use for disposing subscriptions.
     */
    private val disposables = CompositeDisposable()

    init {
        TextInputs.setEmptyAlert(binding.inputLayout)

        binding.module = this

        setBackgroundColor()

        editText = binding.inputLayout.editText as EditText
        initializeEditText()
        hide()
    }

    /**
     * Set background color to views.
     */
    private fun setBackgroundColor() {
        PreferenceApplier(context).colorPair().bgColor().also {
            binding.close.setColorFilter(it)
            binding.sipClear.setColorFilter(it)
            binding.sipUpward.setColorFilter(it)
            binding.sipDownward.setColorFilter(it)
        }
    }

    /**
     * Initialize edit text.
     */
    private fun initializeEditText() {
        editText.setOnEditorActionListener { input, _, _ ->
            view.findDown(input.text.toString())
            true
        }
        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                view.findDown(s.toString())
            }

            override fun afterTextChanged(s: Editable) = Unit
        })
    }

    /**
     * Implement for Data Binding.
     */
    override fun findUp() {
        view.findUp(editText.text.toString())
        Inputs.hideKeyboard(editText)
    }

    /**
     * Implement for Data Binding.
     */
    override fun findDown() {
        view.findDown(editText.text.toString())
        Inputs.hideKeyboard(editText)
    }

    /**
     * Implement for Data Binding.
     */
    override fun clearInput() {
        editText.setText("")
    }

    override fun isVisible() = binding.root.isVisible

    /**
     * Show module with opening software keyboard.
     *
     * @param activity [Activity]
     */
    override fun show(activity: Activity) {
        binding.root.animate()?.let {
            it.cancel()
            it.translationY(0f)
                    .setDuration(ANIMATION_DURATION)
                    .withStartAction { switchVisibility(View.GONE, View.VISIBLE) }
                    .withEndAction {
                        editText.requestFocus()
                        Inputs.showKeyboard(activity, editText)
                    }
                    .start()
        }
    }

    /**
     * Hide module.
     */
    override fun hide() {
        binding.root.animate()?.let {
            it.cancel()
            it.translationY(-height)
                    .setDuration(ANIMATION_DURATION)
                    .withStartAction { Inputs.hideKeyboard(editText) }
                    .withEndAction { switchVisibility(View.VISIBLE, View.GONE) }
                    .start()
        }
    }

    private fun switchVisibility(from: Int, to: Int) {
        Maybe.fromCallable { binding.root.visibility == from }
                .subscribeOn(Schedulers.computation())
                .filter { visible -> visible }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { binding.root.visibility = to },
                        Timber::e
                )
                .addTo(disposables)
    }

    /**
     * Close subscriptions.
     */
    override fun dispose() {
        disposables.clear()
    }

    companion object {

        /**
         * Animation duration [ms].
         */
        private const val ANIMATION_DURATION = 250L
    }
}
