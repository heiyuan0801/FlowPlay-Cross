package app.flowplay.cross.repository

import app.flowplay.cross.storage.CmsConfig
import app.flowplay.cross.storage.SourceJson
import app.flowplay.cross.storage.SourceRecord
import app.flowplay.cross.storage.validateSource
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull

@Serializable
data class TransferSource(
    val type: String = "cms",
    val name: String,
    val baseUrl: String,
    val enabled: Boolean = true,
    val config: CmsConfig = CmsConfig(),
)

@Serializable
private data class SourceDocument(val schemaVersion: Int = 1, val sources: List<TransferSource>)

data class ImportPlan(val sources: List<TransferSource>, val errors: List<String>)
data class SourceExport(val content: String, val included: Int, val skipped: Int)

object SourceTransfer {
    const val MAX_BYTES = 2 * 1024 * 1024

    fun decode(document: String): ImportPlan {
        require(document.encodeToByteArray().size <= MAX_BYTES) { "导入文件超过 2 MiB。" }
        val root = try { SourceJson.parseToJsonElement(document.trimStart('\uFEFF')) as? JsonObject }
            catch (_: Exception) { null }
        requireNotNull(root) { "导入文件必须是 JSON 对象。" }
        require((root["schemaVersion"] as? kotlinx.serialization.json.JsonPrimitive)?.intOrNull == 1) { "不支持的导入 schemaVersion，需要版本 1。" }
        val entries = root["sources"] as? JsonArray ?: throw IllegalArgumentException("缺少 sources 数组。")
        require(entries.size <= 500) { "每次最多导入 500 个来源。" }
        val valid = mutableListOf<TransferSource>()
        val errors = mutableListOf<String>()
        entries.forEachIndexed { index, entry ->
            try {
                val source = SourceJson.decodeFromJsonElement(TransferSource.serializer(), entry)
                require(source.type == "cms") { "当前支持 cms 类型。" }
                val (name, url) = validateSource(source.name, source.baseUrl, source.config)
                valid.add(source.copy(name = name, baseUrl = url))
            } catch (error: IllegalArgumentException) {
                // Decoder errors can embed user values. Keep the row number and a fixed validation reason.
                val reason = if (error is kotlinx.serialization.SerializationException) "字段类型或必填字段无效。"
                    else error.message ?: "配置无效。"
                errors.add("第 ${index + 1} 项 $reason")
            }
        }
        return ImportPlan(valid, errors)
    }

    fun encode(sources: List<SourceRecord>): SourceExport {
        val publicSources = sources.filter { it.type == "cms" && Url(it.baseUrl).encodedQuery.isEmpty() }
            .map { source ->
                // Explicit allowlist. Authentication headers and future Emby credentials have no export field.
                TransferSource(name = source.name, baseUrl = source.baseUrl, enabled = source.enabled,
                    config = CmsConfig(format = source.config().format, listAction = source.config().listAction))
            }
        return SourceExport(SourceJson.encodeToString(SourceDocument(sources = publicSources)),
            publicSources.size, sources.size - publicSources.size)
    }
}
