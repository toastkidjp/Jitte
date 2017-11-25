package jp.toastkid.yobidashi.browser.bookmark

import android.text.TextUtils
import jp.toastkid.yobidashi.browser.bookmark.model.Bookmark

/**
 * Bookmark exporter.
 *
 * @author toastkidjp
 */
class Exporter(private val bookmarks: List<Bookmark?>) {

    fun invoke(): String {
        val builder = StringBuilder()
                .append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n")
                .append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n")
                .append("<TITLE>Bookmarks</TITLE>\n")
                .append("<H1>ブックマークメニュー</H1><DL><p>\n")
        bookmarks.filter { TextUtils.equals("root", it?.parent) }
                .forEach { convertDirectory(builder, it) }
        builder.append("</DL>")
        return builder.toString()
    }

    fun convertDirectory(builder: StringBuilder, item: Bookmark?) {
        item?.let {
            builder.append("<DT><H3>${it.title}</H3>\n")
            builder.append("<DL><p>\n")
            val dirBookmarks = getDirBookmarks(it.title)
            dirBookmarks.forEach {
                if (it?.folder ?: false) {
                    convertDirectory(builder, it)
                } else {
                    convertBookmarkItem(builder, it)
                }
            }
            builder.append("</DL></p>\n")
        }
    }

    fun convertBookmarkItem(builder: StringBuilder, item: Bookmark?) {
        item?.let { builder.append("<DT><A href='${it.url}'>${it.title}</A>\n") }
    }

    fun getDirBookmarks(parent: String): List<Bookmark?> =
            bookmarks.filter { TextUtils.equals(parent, it?.parent) }
}