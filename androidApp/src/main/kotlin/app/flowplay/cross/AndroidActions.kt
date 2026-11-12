package app.flowplay.cross

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import app.flowplay.cross.repository.SourceTransfer
import app.flowplay.cross.ui.PlatformActions
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidActions(private val activity: ComponentActivity) : PlatformActions {
    private var importing: CompletableDeferred<Uri?>? = null
    private var exporting: CompletableDeferred<Uri?>? = null
    private val open = activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { importing?.complete(it) }
    private val create = activity.registerForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { exporting?.complete(it) }

    override suspend fun readSourceDocument(): String? {
        val result = CompletableDeferred<Uri?>()
        importing = result
        return try {
            try { open.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) }
            catch (_: android.content.ActivityNotFoundException) {
                throw IllegalArgumentException("系统没有文件选择器，请使用 URL 导入。")
            }
            val uri = result.await() ?: return null
            withContext(Dispatchers.IO) {
                val bytes = activity.contentResolver.openInputStream(uri)?.use { input ->
                    val output = java.io.ByteArrayOutputStream()
                    val buffer = ByteArray(8192)
                    while (output.size() <= SourceTransfer.MAX_BYTES) {
                        val count = input.read(buffer, 0, minOf(buffer.size, SourceTransfer.MAX_BYTES + 1 - output.size()))
                        if (count < 0) break
                        output.write(buffer, 0, count)
                    }
                    output.toByteArray() }
                    ?: throw IllegalArgumentException("无法读取所选文件。")
                require(bytes.size <= SourceTransfer.MAX_BYTES) { "导入文件超过 2 MiB。" }
                bytes.decodeToString()
            }
        } finally { importing = null }
    }

    override suspend fun writeSourceDocument(content: String): Boolean {
        val result = CompletableDeferred<Uri?>()
        exporting = result
        return try {
            try { create.launch("FlowPlay-sources.json") }
            catch (_: android.content.ActivityNotFoundException) {
                throw IllegalArgumentException("系统没有文件保存器。可在手机或桌面导出来源 JSON，再由电视通过 URL 导入。")
            }
            val uri = result.await() ?: return false
            withContext(Dispatchers.IO) {
                val output = activity.contentResolver.openOutputStream(uri, "wt")
                    ?: throw IllegalArgumentException("无法写入所选文件。")
                output.use { it.write(content.encodeToByteArray()) }
            }
            true
        } finally { exporting = null }
    }

    override fun copyAddress(address: String) {
        activity.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("分集地址", address))
    }

    override fun openExternal(address: String) {
        try {
            activity.startActivity(Intent(Intent.ACTION_VIEW).setDataAndType(Uri.parse(address), "video/*"))
        } catch (_: android.content.ActivityNotFoundException) {
            throw IllegalArgumentException("未找到可打开此地址的播放器，请复制地址到已安装的播放器。")
        }
    }
}
