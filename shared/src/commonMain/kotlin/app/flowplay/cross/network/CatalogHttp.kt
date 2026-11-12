package app.flowplay.cross.network

import app.flowplay.cross.repository.SourceTransfer
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.headers
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.http.URLBuilder
import io.ktor.http.takeFrom
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

class CatalogHttp {
    private val client = HttpClient {
        followRedirects = false
        expectSuccess = false
        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 15_000
        }
    }

    suspend fun get(endpoint: String, parameters: Map<String, String> = emptyMap(), headers: Map<String, String> = emptyMap(),
        maxBytes: Int = 8 * 1024 * 1024): String {
        val initial = URLBuilder(endpoint).apply {
            parameters.forEach { (key, value) -> this.parameters[key] = value }
        }.build()
        var target = initial
        var requestHeaders = headers
        var redirects = 0
        var retries = 0
        while (true) {
            var location: String? = null
            var retryAfter: Long? = null
            val result = client.prepareGet(target) {
                headers { requestHeaders.forEach { (key, value) -> append(key, value) } }
            }.execute { response ->
                val status = response.status.value
                if (status in listOf(301, 302, 303, 307, 308)) {
                    location = response.headers["Location"] ?: throw CatalogException("HTTP $status，响应缺少重定向地址。")
                    return@execute null
                }
                if ((status == 429 || status in 500..599) && retries < 1) {
                    retryAfter = response.headers["Retry-After"]?.toLongOrNull()?.coerceIn(1, 30) ?: 1
                    return@execute null
                }
                if (status !in 200..299) throw CatalogException("HTTP $status ${response.status.description}")
                val channel = response.bodyAsChannel()
                val buffer = ByteArray(8192)
                val chunks = mutableListOf<ByteArray>()
                var size = 0
                while (true) {
                    val count = channel.readAvailable(buffer)
                    if (count < 0) break
                    if (count == 0) continue
                    size += count
                    if (size > maxBytes) throw CatalogException("响应超过 ${maxBytes / 1024} KiB 限制。")
                    chunks.add(buffer.copyOf(count))
                }
                val bytes = ByteArray(size)
                var offset = 0
                chunks.forEach { chunk -> chunk.copyInto(bytes, offset); offset += chunk.size }
                decodeHttpBytes(bytes, response.headers["Content-Type"].orEmpty())
            }
            if (result != null) return result
            if (retryAfter != null) {
                retries++
                delay(requireNotNull(retryAfter) * 1000)
            } else {
                require(++redirects <= 5) { "接口重定向超过 5 次。" }
                val next = URLBuilder(target).takeFrom(requireNotNull(location)).build()
                require(next.protocol.name in listOf("http", "https")) { "接口重定向协议无效。" }
                require(next.user.isNullOrEmpty() && next.password.isNullOrEmpty()) { "接口重定向包含认证信息。" }
                if (next.host != target.host || next.protocol != target.protocol || next.port != target.port) {
                    requestHeaders = emptyMap()
                }
                target = next
            }
        }
    }

    suspend fun importDocument(url: String): String {
        require(Regex("^https?://[^/?#]+", RegexOption.IGNORE_CASE).containsMatchIn(url.trim())) { "请输入完整的 HTTP 或 HTTPS 导入地址。" }
        val target = Url(url.trim())
        require(target.protocol.name in listOf("http", "https") && target.host.isNotBlank()) { "请输入 HTTP 或 HTTPS 导入地址。" }
        require(target.user.isNullOrEmpty() && target.password.isNullOrEmpty()) { "导入地址不能包含用户名和密码。" }
        return get(target.toString(), maxBytes = SourceTransfer.MAX_BYTES)
    }

    fun close() = client.close()
}

class CatalogException(message: String) : IllegalArgumentException(message)

fun readableError(error: Throwable): String {
    if (error is CancellationException) throw error
    val cause = generateSequence(error) { it.cause }.last()
    val message = (cause.message ?: error.message ?: "请求失败")
        .replace(Regex("https?://[^\\s]+", RegexOption.IGNORE_CASE), "[接口地址]")
        .replace(Regex("(?i)(token|key|password|authorization|cookie)\\s*[=:]\\s*[^\\s,;]+"), "$1=[已隐藏]")
        .take(350)
    return if (error is IllegalArgumentException) message else "${cause::class.simpleName} · $message"
}

expect fun decodeHttpBytes(bytes: ByteArray, contentType: String): String
