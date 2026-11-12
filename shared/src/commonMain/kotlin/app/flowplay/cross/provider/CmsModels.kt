package app.flowplay.cross.provider

import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom

data class CmsCategory(val id: String, val name: String)
data class CmsEpisode(val name: String, val address: String)
data class CmsLine(val name: String, val episodes: List<CmsEpisode>)
data class CmsVideo(
    val id: String,
    val title: String,
    val poster: String = "",
    val overview: String = "",
    val year: String = "",
    val area: String = "",
    val actors: String = "",
    val director: String = "",
    val category: String = "",
    val remarks: String = "",
    val lines: List<CmsLine> = emptyList(),
)
data class CmsResponse(
    val categories: List<CmsCategory> = emptyList(),
    val videos: List<CmsVideo> = emptyList(),
    val page: Int = 1,
    val pageCount: Int? = null,
    val total: Int? = null,
    val limit: Int? = null,
) {
    fun hasNext(requestedPage: Int): Boolean = videos.isNotEmpty() &&
        (pageCount?.let { requestedPage < it } ?: total?.let { count ->
            limit?.takeIf { it > 0 }?.let { requestedPage.toLong() * it < count }
        } ?: false)
}

internal fun Map<String, String>.video(xmlLines: List<Pair<String, String>> = emptyList()): CmsVideo? {
    fun field(vararg keys: String) = keys.firstNotNullOfOrNull { get(it)?.takeIf(String::isNotBlank) }.orEmpty()
    val id = field("vod_id", "id")
    val title = plainText(field("vod_name", "name"))
    if (id.isBlank() || title.isBlank()) return null
    return CmsVideo(id, title, field("vod_pic", "pic"), plainText(field("vod_content", "des", "vod_blurb")),
        field("vod_year", "year"), field("vod_area", "area"), field("vod_actor", "actor"),
        field("vod_director", "director"), field("type_name", "type"), field("vod_remarks", "note"),
        parsePlayLines(field("vod_play_from"), field("vod_play_url"), xmlLines))
}

internal fun parsePlayLines(from: String, urls: String, xmlLines: List<Pair<String, String>>): List<CmsLine> {
    val names = from.split("$$$")
    val rawLines = xmlLines.ifEmpty { urls.split("$$$").mapIndexed { index, line ->
        names.getOrNull(index).orEmpty() to line
    } }
    return rawLines.mapIndexedNotNull { lineIndex, (rawName, raw) ->
        val entries = raw.trim().split(Regex("#|[\\r\\n]+"))
        val episodes = entries.mapIndexedNotNull { index, entry ->
            val cleaned = entry.trim().trim('#')
            if (cleaned.isEmpty()) return@mapIndexedNotNull null
            val separator = cleaned.indexOf('$')
            val isBareAddress = cleaned.startsWith("http://", true) || cleaned.startsWith("https://", true) ||
                cleaned.startsWith("//") || cleaned.startsWith("/") || Regex("^[A-Za-z][A-Za-z0-9+.-]*://").containsMatchIn(cleaned)
            val name = if (separator >= 0 && !isBareAddress) cleaned.take(separator).trim() else ""
            val address = if (separator >= 0 && !isBareAddress) cleaned.substring(separator + 1).trim() else cleaned
            if (address.isEmpty()) null else CmsEpisode(name.ifEmpty { "第 ${index + 1} 集" }, address.replace("&amp;", "&"))
        }
        if (episodes.isEmpty()) null else CmsLine(rawName.trim().ifEmpty { "线路 ${lineIndex + 1}" }, episodes)
    }
}

internal fun plainText(value: String): String = value.replace(Regex("(?i)<br\\s*/?>|</p>"), "\n")
    .replace(Regex("<[^>]*>"), "").replace("&nbsp;", " ").replace("&amp;", "&")
    .replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"").replace("&#39;", "'").trim()

fun resolveCmsAddress(endpoint: String, address: String): String = try {
    URLBuilder(endpoint).takeFrom(address.trim()).buildString()
} catch (_: Exception) { address.trim() }
