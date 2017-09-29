package jp.toastkid.yobidashi.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Spinner
import android.widget.TextView
import jp.toastkid.yobidashi.R
import jp.toastkid.yobidashi.libs.preference.PreferenceApplier

/**
 * TODO Delete it.
 *
 * @author toastkidjp
 */
object StartUpSpinnerInitializer {

    fun initialize(spinner: Spinner) {
        val adapter = object : BaseAdapter() {
            override fun getCount(): Int {
                return StartUp.values().size
            }

            override fun getItem(position: Int): Any {
                return StartUp.values()[position]
            }

            override fun getItemId(position: Int): Long {
                return StartUp.values()[position].titleId.toLong()
            }

            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val startUp = StartUp.values()[position]

                val context = spinner.context
                val inflater = LayoutInflater.from(context)
                val view = inflater.inflate(R.layout.item_spinner_simple, parent, false)
                val textView = view.findViewById<TextView>(R.id.text)
                textView.setText(startUp.titleId)
                return view
            }
        }
        spinner.adapter = adapter
        spinner.setSelection(StartUp.findIndex(PreferenceApplier(spinner.context).startUp) ?: 0)
    }

}
