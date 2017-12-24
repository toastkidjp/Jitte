package jp.toastkid.yobidashi.advertisement

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import jp.toastkid.yobidashi.BuildConfig
import jp.toastkid.yobidashi.R

/**
 * For production environment AD initializer.
 *
 * @param context need ApplicationContext
 * @author toastkidjp
 */
internal class ProductionAdInitializer(context: Context) : AdInitializer {

    init {
        if (BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        val appAdId = context.getString(R.string.production_app_admob_id)
        MobileAds.initialize(context, appAdId)
    }

    /**
     * Invoke this initializer with [AdView].
     * @param adView [AdView]
     */
    override fun invoke(adView: AdView) {
        if (BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        adView.loadAd(makeRequest())
    }

    /**
     * Invoke this initializer with [InterstitialAd].
     * @param interstitialAd [InterstitialAd]
     */
    override fun invoke(interstitialAd: InterstitialAd) {
        if (BuildConfig.DEBUG) {
            throw IllegalStateException()
        }
        interstitialAd.loadAd(makeRequest())
    }

    /**
     * Make [AdRequest] object.
     *
     * @return [AdRequest] object
     */
    private fun makeRequest(): AdRequest = AdRequest.Builder().build()
}
