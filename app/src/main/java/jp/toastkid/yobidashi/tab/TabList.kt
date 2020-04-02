package jp.toastkid.yobidashi.tab

import android.content.Context
import androidx.annotation.Keep
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import io.reactivex.disposables.CompositeDisposable
import jp.toastkid.yobidashi.tab.model.EditorTab
import jp.toastkid.yobidashi.tab.model.PdfTab
import jp.toastkid.yobidashi.tab.model.Tab
import jp.toastkid.yobidashi.tab.model.WebTab
import okio.Okio
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * First collection of [Tab].
 *
 * @author toastkidjp
 */
class TabList private constructor() {

    @Transient
    private val tabs: MutableList<Tab> = mutableListOf()

    @Transient
    private val disposables = CompositeDisposable()

    @Keep
    private var index: Int = 0

    internal fun currentTab(): Tab? {
        if (tabs.isEmpty() || invalidIndex(index)) {
            return null
        }
        return tabs[index]
    }

    internal fun setIndex(newIndex: Int) {
        index = if (invalidIndex(newIndex)) 0 else newIndex
    }

    @Keep
    fun getIndex(): Int = index

    @Keep
    fun size(): Int = tabs.size

    internal fun get(position: Int): Tab? =
            if (position < 0 || tabs.size <= position) {
                null
            } else {
                tabs[position]
            }

    internal fun set(index: Int, currentTab: Tab) {
        val target = if (invalidIndex(index)) { 0 } else { index }
        tabs[target] = currentTab
    }

    private fun invalidIndex(newIndex: Int): Boolean {
        return !inRange(newIndex)
    }

    /**
     * Save current state to file.
     */
    internal fun save() {
        val json = jsonAdapter.toJson(this)
        tabsFile?.let {
            Okio.buffer(Okio.sink(it)).run {
                writeUtf8(json)
                flush()
                close()
            }
        }
        savingLock.withLock {
            itemsDir?.let {
                it.deleteRecursively()
                it.mkdirs()
            }
            tabs.forEach { tab ->
                val source: ByteArray? = when (tab) {
                    is WebTab -> webTabJsonAdapter.toJson(tab)?.toByteArray(charset)
                    is EditorTab -> editorTabJsonAdapter.toJson(tab)?.toByteArray(charset)
                    is PdfTab -> pdfTabJsonAdapter.toJson(tab)?.toByteArray(charset)
                    else -> ByteArray(0)
                }
                source?.let {
                    Okio.buffer(Okio.sink(File(itemsDir, "${tab.id()}.json"))).use {
                        it.write(source)
                        it.flush()
                        it.close()
                    }
                }
            }
        }
    }

    internal val isEmpty: Boolean
        get() = tabs.isEmpty()

    internal fun add(newTab: Tab) {
        val newIndex = index + 1
        if (inRange(newIndex)) {
            tabs.add(newIndex, newTab)
        } else {
            tabs.add(newTab)
        }
    }

    internal fun closeTab(index: Int) {
        if (index <= this.index && this.index != 0) {
            this.index--
        }
        val tab: Tab = tabs[index]
        remove(tab)
    }

    // TODO merge it.
    internal fun remove(tab: Tab) {
        File(itemsDir, tab.id() + ".json").delete()
        tabs.remove(tab)
    }

    internal fun clear() {
        tabs.clear()
        index = 0
        tabsFile?.delete()
        itemsDir?.delete()
        save()
    }

    fun swap(from: Int, to: Int) {
        if (inRange(from, to)) {
            val currentTab = currentTab() ?: return
            Collections.swap(tabs, from, to)
            setIndex(tabs.indexOf(currentTab))
        }
    }

    private fun inRange(vararg indexes: Int): Boolean {
        val size = tabs.size
        return indexes.none { it < 0 || size <= it }
    }

    companion object {

        private const val TABS_DIR = "tabs"

        private const val TABS_ITEM_DIR = "$TABS_DIR/items"

        private val savingLock = ReentrantLock()

        private var tabsFile: File? = null

        private val charset = charset("UTF-8")

        private val jsonAdapter: JsonAdapter<TabList> by lazy {
            Moshi.Builder().build().adapter(TabList::class.java)
        }

        private val webTabJsonAdapter: JsonAdapter<WebTab> by lazy {
            Moshi.Builder().build().adapter(WebTab::class.java)
        }

        private val editorTabJsonAdapter: JsonAdapter<EditorTab> by lazy {
            Moshi.Builder().build().adapter(EditorTab::class.java)
        }

        private val pdfTabJsonAdapter: JsonAdapter<PdfTab> by lazy {
            Moshi.Builder().build().adapter(PdfTab::class.java)
        }

        private var itemsDir: File? = null

        internal fun loadOrInit(context: Context): TabList {
            initTabsFile(context)
            if (tabsFile == null || !tabsFile!!.exists()) {
                return TabList()
            }

            try {
                val fromJson: TabList? = Okio.buffer(Okio.source(tabsFile as File)).let {
                    val from: TabList? = jsonAdapter.fromJson(it)
                    it.close()
                    return@let from
                }

                loadTabsFromDir()
                        ?.forEach { it?.let { fromJson?.add(it) } }
                if (fromJson?.size() as Int <= fromJson.index) {
                    fromJson.index = fromJson.size() - 1
                }
                return fromJson
            } catch (e: IOException) {
                Timber.e(e)
            }

            return TabList()
        }

        private fun loadTabsFromDir(): List<Tab?>? {
            return itemsDir?.list()
                    ?.map {
                        val json: String = Okio.buffer(Okio.source(File(itemsDir, it))).let {
                            val readUtf8 = it.readUtf8()
                            it.close()
                            return@let readUtf8
                        }
                        when {
                            json.contains("editorTab") -> editorTabJsonAdapter.fromJson(json)
                            json.contains("pdfTab") -> pdfTabJsonAdapter.fromJson(json)
                            else -> webTabJsonAdapter.fromJson(json)
                        }
                    }
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
    }

    internal fun indexOf(tab: Tab): Int = tabs.indexOf(tab)

    fun dispose() {
        disposables.clear()
    }

    override fun toString(): String = tabs.toString()

    /**
     * Load background tab from file if needs.
     */
    fun loadBackgroundTabsFromDirIfNeed() {
        BackgroundTabQueue.iterate {
            tabs.add(WebTab.makeBackground(it.first, it.second.toString()))
        }
        save()
    }

    fun updateWithIdAndHistory(idAndHistory: Pair<String, History>) {
        val targetId = idAndHistory.first
        for (i in 0 until tabs.size) {
            val tab = tabs[i]
            if (tab !is WebTab || tab.id() != targetId) {
                continue
            }

            tab.addHistory(idAndHistory.second)
            tabs.set(i, tab)
            save()
            return
        }
    }

}
