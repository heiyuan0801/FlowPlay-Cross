package app.flowplay.cross.repository

import app.cash.sqldelight.db.SqlDriver
import app.flowplay.cross.storage.CmsConfig
import app.flowplay.cross.storage.FlowDatabase
import app.flowplay.cross.storage.SourceJson
import app.flowplay.cross.storage.SourceRecord
import app.flowplay.cross.storage.currentTimeMillis
import app.flowplay.cross.storage.sourceId
import app.flowplay.cross.storage.validateSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SourceRepository(private val driver: SqlDriver) {
    private val database = FlowDatabase(driver)
    private val queries = database.sourcesQueries
    private val mutex = Mutex()
    private val mutableSources = MutableStateFlow<List<SourceRecord>>(emptyList())
    val sources = mutableSources.asStateFlow()

    private fun read() = queries.selectAll { id, type, name, baseUrl, enabled, sortOrder, createdAt, updatedAt, extraConfigJson ->
        SourceRecord(id, type, name, baseUrl, enabled != 0L, sortOrder, createdAt, updatedAt, extraConfigJson)
    }.executeAsList()

    private suspend fun change(action: () -> Unit) = withContext(Dispatchers.Default) {
        mutex.withLock {
            val updated = database.transactionWithResult { action(); read() }
            mutableSources.value = updated
        }
    }

    suspend fun refresh() = change { }

    suspend fun save(existingId: String?, name: String, endpoint: String, enabled: Boolean, config: CmsConfig) {
        val (cleanName, cleanUrl) = validateSource(name, endpoint, config)
        change {
            val existing = read()
            require(existing.none { it.id != existingId && it.type == "cms" && it.baseUrl == cleanUrl }) { "此接口地址已存在。" }
            val now = currentTimeMillis()
            val json = SourceJson.encodeToString(config)
            if (existingId == null) {
                queries.insertSource(sourceId(), "cms", cleanName, cleanUrl, if (enabled) 1 else 0,
                    (existing.maxOfOrNull { it.sortOrder } ?: -1) + 1, now, now, json)
            } else {
                require(existing.any { it.id == existingId }) { "来源已删除，请返回列表。" }
                queries.updateSource(cleanName, cleanUrl, if (enabled) 1 else 0, now, json, existingId)
            }
        }
    }

    suspend fun delete(id: String) = change { queries.deleteSource(id) }
    suspend fun enable(id: String, value: Boolean) = change { queries.setEnabled(if (value) 1 else 0, currentTimeMillis(), id) }

    suspend fun move(id: String, offset: Int) = change {
        val ordered = read().toMutableList()
        val index = ordered.indexOfFirst { it.id == id }
        val target = index + offset
        if (index >= 0 && target in ordered.indices) {
            val source = ordered.removeAt(index)
            ordered.add(target, source)
            val now = currentTimeMillis()
            ordered.forEachIndexed { position, item -> queries.setOrder(position.toLong(), now, item.id) }
        }
    }

    suspend fun importSources(document: String): String {
        val plan = SourceTransfer.decode(document)
        var added = 0
        var duplicates = 0
        change {
            val known = read().map { it.type to it.baseUrl }.toMutableSet()
            val now = currentTimeMillis()
            var order = (read().maxOfOrNull { it.sortOrder } ?: -1) + 1
            plan.sources.forEach { source ->
                if (!known.add("cms" to source.baseUrl)) {
                    duplicates++
                } else {
                    queries.insertSource(sourceId(), "cms", source.name, source.baseUrl, if (source.enabled) 1 else 0,
                        order++, now, now, SourceJson.encodeToString(source.config))
                    added++
                }
            }
        }
        return "导入 $added 个，重复 $duplicates 个，无效 ${plan.errors.size} 个。" +
            plan.errors.joinToString(separator = "\n", prefix = if (plan.errors.isEmpty()) "" else "\n")
    }

    fun close() = driver.close()
}
