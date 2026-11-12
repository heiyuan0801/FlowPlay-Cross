package app.flowplay.cross.provider

import app.flowplay.cross.network.CatalogException
import app.flowplay.cross.network.CatalogHttp
import app.flowplay.cross.storage.SourceJson
import app.flowplay.cross.storage.SourceRecord
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

class CmsProvider(private val http: CatalogHttp, val source: SourceRecord) {
    suspend fun categories(): CmsResponse = request(mapOf("ac" to "list"))

    suspend fun items(category: String? = null, page: Int = 1, search: String = ""): CmsResponse {
        val parameters = mutableMapOf("ac" to source.config().listAction, "pg" to page.toString())
        if (!category.isNullOrBlank()) parameters["t"] = category
        if (search.isNotBlank()) parameters["wd"] = search.trim()
        return try { request(parameters) } catch (error: CatalogException) {
            if (parameters["ac"] == "list") throw error
            request(parameters + ("ac" to "list"))
        }
    }

    suspend fun detail(id: String): CmsVideo {
        val response = try { request(mapOf("ac" to "detail", "ids" to id)) }
            catch (_: CatalogException) { null }
        return response?.videos?.firstOrNull { it.id == id }
            ?: request(mapOf("ac" to "videolist", "ids" to id)).videos.firstOrNull { it.id == id }
            ?: throw CatalogException("接口未返回所选条目的详情。")
    }

    suspend fun testConnection(): String {
        val response = categories()
        return "连接成功，返回 ${response.categories.size} 个分类、${response.videos.size} 个条目。"
    }

    private suspend fun request(parameters: Map<String, String>): CmsResponse {
        val config = source.config()
        val text = http.get(source.baseUrl, parameters, config.headers).trimStart('\uFEFF', ' ', '\n', '\r', '\t')
        val response = when (config.format) {
            "xml" -> parseCmsXml(text)
            "json" -> parseCmsJson(text)
            else -> if (text.startsWith("<")) parseCmsXml(text) else parseCmsJson(text)
        }
        return response.copy(videos = response.videos.map { video -> video.copy(
            poster = video.poster.takeIf(String::isNotBlank)?.let { resolveCmsAddress(source.baseUrl, it) }
                ?.takeIf { it.startsWith("http://", true) || it.startsWith("https://", true) }.orEmpty(),
        ) })
    }
}

internal fun parseCmsJson(text: String): CmsResponse {
    val root = try { SourceJson.parseToJsonElement(text) as? JsonObject } catch (_: Exception) { null }
        ?: throw CatalogException("CMS JSON 解析失败，需要对象格式的响应。")
    fun scalar(key: String) = (root[key] as? JsonPrimitive)?.contentOrNull.orEmpty()
    val code = scalar("code")
    if (code.isNotBlank() && code !in listOf("1", "200")) throw CatalogException("CMS 拒绝请求，返回非成功状态。")
    if (root["class"] !is JsonArray && root["list"] !is JsonArray) {
        throw CatalogException("响应缺少 CMS class / list 数组。")
    }
    val categories = (root["class"] as? JsonArray).orEmpty().mapNotNull { entry ->
        val value = entry as? JsonObject ?: return@mapNotNull null
        val id = (value["type_id"] as? JsonPrimitive)?.contentOrNull.orEmpty()
        val name = (value["type_name"] as? JsonPrimitive)?.contentOrNull.orEmpty()
        if (id.isBlank() || name.isBlank()) null else CmsCategory(id, plainText(name))
    }.distinctBy { it.id }
    val videos = (root["list"] as? JsonArray).orEmpty().mapNotNull { entry ->
        (entry as? JsonObject)?.mapValues { (_, value) -> (value as? JsonPrimitive)?.contentOrNull.orEmpty() }?.video()
    }
    return CmsResponse(categories, videos, scalar("page").toIntOrNull() ?: 1,
        scalar("pagecount").toIntOrNull(), scalar("total").toIntOrNull(), scalar("limit").toIntOrNull())
}

internal expect fun parseCmsXml(text: String): CmsResponse
