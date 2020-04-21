package jp.toastkid.yobidashi.planning_poker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity

import jp.toastkid.yobidashi.R

/**
 * For using displaying selected card.
 *
 * @author toastkidjp
 */
class CardViewActivity : AppCompatActivity() {

    /**
     * Card Fragment.
     */
    private lateinit var cardFragment: CardFragment

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(LAYOUT_ID)

        cardFragment = CardFragment()
        cardFragment.arguments = Bundle().also {
            it.putString(EXTRA_KEY_CARD_TEXT, intent.getStringExtra(EXTRA_KEY_CARD_TEXT))
        }
        addFragment(cardFragment)
    }

    /**
     * Add fragment to this activity.
     *
     * @param fragment [Fragment]
     */
    private fun addFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.container, fragment)
        transaction.commit()
    }

    companion object {

        /**
         * Card extra key.
         */
        const val EXTRA_KEY_CARD_TEXT = "card_text"

        /**
         * Layout ID.
         */
        private const val LAYOUT_ID = R.layout.activity_transparent

        /**
         * Make [CardViewActivity]'s intent.
         *
         * @param context [Context]
         * @param text card text.
         * @return [Intent]
         */
        fun makeIntent(
                context: Context,
                text: String
        ) = Intent(context, CardViewActivity::class.java)
                .also { it.putExtra(EXTRA_KEY_CARD_TEXT, text) }
    }
}