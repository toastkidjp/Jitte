package jp.toastkid.search

import android.net.Uri
import androidx.core.net.toUri

/**
 * @author toastkidjp
 */
class SearchQueryExtractor {

    private val commonQueryParameterNames = setOf("q", "query", "text", "word")

    operator fun invoke(url: String?) = invoke(url?.toUri())

    operator fun invoke(uri: Uri?): String? {
        val host = uri?.host ?: return null
        return when {
            host.startsWith("www.google.")
                    or host.startsWith("play.google.")
                    or host.startsWith("www.bing.")
                    or host.endsWith("www.aolsearch.com")
                    or host.endsWith("www.ask.com")
                    or host.endsWith("twitter.com")
                    or host.endsWith("stackoverflow.com")
                    or host.endsWith("github.com")
                    or host.endsWith("mvnrepository.com")
                    or host.endsWith("searchcode.com")
                    or host.equals("www.qwant.com")
                    or host.equals("www.reddit.com")
                    or host.equals("www.ft.com")
                    or host.equals("www.startpage.com")
                    or host.equals("www.imdb.com")
                    or host.equals("duckduckgo.com")
                    or host.endsWith("medium.com")
                    or host.endsWith("ted.com")
                    or host.endsWith(".slideshare.net")
                    or host.endsWith("cse.google.com")
                    or host.endsWith(".buzzfeed.com")
                    or host.endsWith("openweathermap.org")
                    or host.endsWith(".quora.com")
                    or host.endsWith(".livejournal.com")
                    or host.endsWith("search.daum.net") ->
                uri.getQueryParameter("q")
            host.startsWith("www.amazon.") ->
                uri.getQueryParameter("field-keywords")
            host.endsWith(".linkedin.com") ->
                uri.getQueryParameter("keywords")
            host.startsWith("www.yandex.") ->
                uri.getQueryParameter("text")
            host.startsWith("www.youtube.") ->
                uri.getQueryParameter("search_query")
            host.startsWith("www.flickr.") ->
                uri.getQueryParameter("text")
            host.endsWith(".yelp.com") ->
                uri.getQueryParameter("find_desc")
            host.equals("www.tumblr.com")
                    or host.equals("web.archive.org")-> uri.lastPathSegment
            host.endsWith("archive.org")
                    or host.endsWith("search.naver.com") ->
                uri.getQueryParameter("query")
            host.endsWith(".wikipedia.org")
                or host.endsWith(".wikimedia.org") ->
                if (uri.queryParameterNames.contains("search")) {
                    uri.getQueryParameter("search")
                } else {
                    // "/wiki/"'s length.
                    Uri.decode(uri.encodedPath?.substring(6))
                }
            host.endsWith("search.yahoo.com")
                    or host.endsWith("search.yahoo.co.jp") ->
                uri.getQueryParameter("p")
            host.endsWith("www.baidu.com") ->
                uri.getQueryParameter("wd")
            host == "www.wolframalpha.com" ->
                uri.getQueryParameter("i")
            host.endsWith("facebook.com")
                    or host.equals("www.instagram.com")
                    or host.equals("www.espn.com") ->
                Uri.decode(uri.lastPathSegment)
            else -> uri.getQueryParameter(
                    commonQueryParameterNames
                            .find { uri.queryParameterNames.contains(it) } ?: ""
            )
        }
    }
}