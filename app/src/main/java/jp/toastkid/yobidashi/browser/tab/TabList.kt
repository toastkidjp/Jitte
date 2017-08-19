package jp.toastkid.yobidashi.browser.tab

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import jp.toastkid.yobidashi.libs.Logger
import okio.Okio
import java.io.File
import java.io.IOException
import java.util.*

/**
 * First collection of [Tab].

 * @author toastkidjp
 */
class TabList private constructor() {

    @Transient private val tabs: MutableList<Tab>

    private var index: Int = 0

    init {
        this.tabs = ArrayList<Tab>()
    }

    internal fun currentTab(): Tab {
        return tabs[index]
    }

    internal fun setIndex(newIndex: Int) {
        index = newIndex
    }

    fun size(): Int {
        return tabs.size
    }

    internal fun get(position: Int): Tab {
        return tabs[position]
    }

    /**
     * Save current state to file.
     */
    internal fun save() {
        try {
            initJsonAdapterIfNeed()
            val json = jsonAdapter?.toJson(this)
            Okio.buffer(Okio.sink(tabsFile)).write(json?.toByteArray(charset("UTF-8"))).flush()
            itemsDir?.list()?.map { File(itemsDir, it) }?.forEach { it.delete() }
            (0..tabs.size - 1).forEach {
                Logger.i("save " + File(itemsDir, "$it.json").absoluteFile)
                Okio.buffer(Okio.sink(File(itemsDir, "$it.json")))
                    .write(tabJsonAdapter?.toJson(tabs.get(it))?.toByteArray(charset("UTF-8")))
                    .flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    internal val isEmpty: Boolean
        get() = tabs.isEmpty()

    internal fun add(newTab: Tab) {
        tabs.add(newTab)
    }

    private fun remove(index: Int) {
        tabs.removeAt(index)
    }

    internal fun closeTab(index: Int) {
        if (index <= this.index) {
            this.index--
        }
        remove(index)
        save()
    }

    internal fun clear() {
        tabs.clear()
        index = -1
        tabsFile?.delete()
        itemsDir?.delete()
    }

    companion object {

        private val TABS_DIR = "tabs"

        private val TABS_ITEM_DIR = TABS_DIR + "/items"

        private var tabsFile: File? = null

        private var tabJsonAdapter: JsonAdapter<Tab>? = null

        private var jsonAdapter: JsonAdapter<TabList>? = null

        private var itemsDir: File? = null

        internal fun loadOrInit(context: Context): TabList {
            initTabsFile(context)
            if (tabsFile == null || !tabsFile!!.exists()) {
                return TabList()
            }

            try {
                initJsonAdapterIfNeed()
                initTabJsonAdapterIfNeed()

                val fromJson: TabList?
                        = jsonAdapter?.fromJson(Okio.buffer(Okio.source(tabsFile as File)))

                Logger.i("size = ${itemsDir?.list()?.size}")

                itemsDir?.list()
                        ?.map{ tabJsonAdapter?.fromJson(Okio.buffer(Okio.source(File(itemsDir, it))))}
                        ?.forEach { fromJson?.add(it as Tab) }

                return fromJson as TabList
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return TabList()
        }

        private fun initTabsFile(context: Context) {
            val storeDir = File(context.filesDir, TABS_DIR)
            if (!storeDir.exists()) {
                storeDir.mkdirs()
            }
            tabsFile = File(storeDir, "tabs.json")

            itemsDir = File(context.filesDir, TABS_ITEM_DIR)
            if (itemsDir != null && !(itemsDir as File).exists()) {
                itemsDir?.mkdirs()
            }
        }

        private fun initTabJsonAdapterIfNeed() {
            if (tabJsonAdapter != null) {
                return
            }
            tabJsonAdapter = Moshi.Builder().build().adapter(Tab::class.java)
        }

        private fun initJsonAdapterIfNeed() {
            if (jsonAdapter != null) {
                return
            }
            jsonAdapter = Moshi.Builder().build().adapter(TabList::class.java)
        }
    }
}
