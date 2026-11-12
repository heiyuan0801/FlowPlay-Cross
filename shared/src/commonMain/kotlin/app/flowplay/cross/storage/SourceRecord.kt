package app.flowplay.cross.storage

import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal val SourceJson = Json { ignoreUnknownKeys = true; prettyPrint = true; encodeDefaults = true }

@Serializable
data class CmsConfig(
    val format: String = "auto",
    val headers: Map<String, String> = emptyMap(),
    val listAction: String = "videolist",
)

@Serializable
data class SourceRecord(
    val id: String,
    val type: String = "cms",
    val name: String,
    val baseUrl: String,
    val enabled: Boolean = true,
    val sortOrder: Long = 0,
    val createdAt: Long,
    val updatedAt: Long,
    val extraConfigJson: String = "{}",
) {
    fun config(): CmsConfig = SourceJson.decodeFromString(extraConfigJson)
}

fun normalizeEndpoint(value: String): String {
    val raw = value.trim()
    require(raw.isNotEmpty()) { "请输入接口地址。" }
    require(Regex("^https?://[^/?#]+", RegexOption.IGNORE_CASE).containsMatchIn(raw)) { "请输入完整的 HTTP 或 HTTPS 接口地址。" }
    require(!raw.any { it.isWhitespace() || it.isISOControl() }) { "接口地址含空白或控制字符。" }
    val url = try { Url(raw) } catch (_: Exception) { throw IllegalArgumentException("接口地址格式无效。") }
    require(url.protocol.name in listOf("http", "https") && url.host.isNotBlank()) { "请输入完整的 HTTP 或 HTTPS 接口地址。" }
    require(url.user.isNullOrEmpty() && url.password.isNullOrEmpty()) { "请通过请求头配置认证，接口地址不能包含用户名和密码。" }
    return URLBuilder(url).apply {
        host = url.host.lowercase()
        fragment = ""
        listOf("ac", "pg", "t", "wd", "ids").forEach { parameters.remove(it) }
    }.buildString()
}

fun validateSource(name: String, endpoint: String, config: CmsConfig): Pair<String, String> {
    require(name.trim().isNotEmpty()) { "请输入来源名称。" }
    require(name.trim().length <= 100) { "来源名称最多 100 字。" }
    require(config.format in listOf("auto", "json", "xml")) { "格式必须为 auto、json 或 xml。" }
    require(config.listAction in listOf("list", "videolist", "detail")) { "列表动作必须为 list、videolist 或 detail。" }
    config.headers.forEach { (key, value) ->
        require(key.matches(Regex("[!#$%&'*+.^_`|~0-9A-Za-z-]+")) && !value.any { it == '\r' || it == '\n' }) {
            "请求头名称或内容无效。"
        }
        require(key.lowercase() !in listOf("host", "content-length", "connection")) { "该请求头由网络客户端管理。" }
    }
    return name.trim() to normalizeEndpoint(endpoint)
}

expect fun sourceId(): String
expect fun currentTimeMillis(): Long
