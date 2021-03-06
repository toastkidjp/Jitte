package jp.toastkid.yobidashi.tab

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import jp.toastkid.yobidashi.tab.model.WebTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * [WebTab]'s test case.
 *
 * @author toastkidjp
 */
class WebTabTest {

    @Test
    @Throws(IOException::class)
    fun test() {
        val tab = makeTestTab()

        val tabJsonAdapter = makeTabJsonAdapter()
        val json = check_toJson(tab, tabJsonAdapter)

        check_fromJson(tabJsonAdapter, json)
    }

    @Throws(IOException::class)
    private fun check_fromJson(tabJsonAdapter: JsonAdapter<WebTab>, json: String) {
        val fromJson = tabJsonAdapter.fromJson(json)
        assertEquals("Title", fromJson?.latest?.title())
        assertEquals("URL", fromJson?.latest?.url())
    }

    private fun check_toJson(tab: WebTab, tabJsonAdapter: JsonAdapter<WebTab>): String {
        val json = tabJsonAdapter.toJson(tab)
        assertTrue(json.contains("\"histories\":[{\"scrolled\":0,\"title\":\"Title\",\"url\":\"URL\"}]"))
        return json
    }

    private fun makeTabJsonAdapter(): JsonAdapter<WebTab> {
        val moshi = Moshi.Builder().build()
        return moshi.adapter(WebTab::class.java)
    }

    private fun makeTestTab(): WebTab {
        val tab = WebTab()
        tab.addHistory(History.make("Title", "URL"))
        return tab
    }
}
