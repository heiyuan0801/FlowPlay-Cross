package app.flowplay.cross.network

import java.nio.charset.Charset

actual fun decodeHttpBytes(bytes: ByteArray, contentType: String): String {
    val declared = Regex("charset\\s*=\\s*[\"']?([^;\\s\"']+)", RegexOption.IGNORE_CASE)
        .find(contentType)?.groupValues?.get(1)
    val xmlEncoding = Regex("<\\?xml[^>]*encoding=[\"']([^\"']+)", RegexOption.IGNORE_CASE)
        .find(bytes.take(180).toByteArray().toString(Charsets.US_ASCII))?.groupValues?.get(1)
    val charset = try { Charset.forName(declared ?: xmlEncoding ?: "UTF-8") }
        catch (_: Exception) { throw CatalogException("响应声明了不支持的字符编码。") }
    return bytes.toString(charset).trimStart('\uFEFF')
}
