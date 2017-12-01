package jp.toastkid.yobidashi.search.suggestion

import java.util.*
import java.util.regex.Pattern

/**
 * Suggestion response parser.
 *
 * @author toastkidjp
 */
internal object SuggestionParser {

    /**
     * For extracting suggested word.
     */
    private val PATTERN: Pattern = Pattern.compile("<suggestion data=\"(.+?)\"/>", Pattern.DOTALL)

    /**
     * Parse response xml.
     * @param response
     *
     * @return suggest words
     */
    fun parse(response: String): List<String> {
        val split = response.split("</CompleteSuggestion>").dropLastWhile { it.isEmpty() }.toTypedArray()
        val suggestions = ArrayList<String>(split.size)
        for (line in split) {
            val matcher = PATTERN.matcher(line)
            if (matcher.find()) {
                suggestions.add(matcher.group(1))
            }
        }
        return suggestions
    }

}
