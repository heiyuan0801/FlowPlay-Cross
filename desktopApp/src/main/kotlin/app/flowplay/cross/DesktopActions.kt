package app.flowplay.cross

import app.flowplay.cross.repository.SourceTransfer
import app.flowplay.cross.ui.PlatformActions
import java.awt.Desktop
import java.awt.EventQueue
import java.awt.FileDialog
import java.awt.Frame
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.io.File
import java.net.URI
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DesktopActions : PlatformActions {
    private suspend fun choose(mode: Int): File? = withContext(Dispatchers.IO) {
        var file: File? = null
        EventQueue.invokeAndWait {
            val dialog = FileDialog(null as Frame?, if (mode == FileDialog.LOAD) "导入来源 JSON" else "导出来源 JSON", mode)
            try {
                dialog.file = if (mode == FileDialog.SAVE) "FlowPlay-sources.json" else "*.json"
                dialog.isVisible = true
                if (dialog.file != null) file = File(dialog.directory, dialog.file)
            } finally { dialog.dispose() }
        }
        file
    }

    override suspend fun readSourceDocument(): String? {
        val file = choose(FileDialog.LOAD) ?: return null
        return withContext(Dispatchers.IO) {
            val bytes = file.inputStream().use { it.readNBytes(SourceTransfer.MAX_BYTES + 1) }
            require(bytes.size <= SourceTransfer.MAX_BYTES) { "导入文件超过 2 MiB。" }
            bytes.decodeToString()
        }
    }

    override suspend fun writeSourceDocument(content: String): Boolean {
        val file = choose(FileDialog.SAVE) ?: return false
        withContext(Dispatchers.IO) {
            val target = file.toPath().toAbsolutePath()
            val temporary = Files.createTempFile(target.parent, ".flowplay-sources-", ".json")
            try {
                Files.writeString(temporary, content)
                try { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE) }
                catch (_: AtomicMoveNotSupportedException) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING) }
            } finally { Files.deleteIfExists(temporary) }
        }
        return true
    }

    override fun copyAddress(address: String) {
        Toolkit.getDefaultToolkit().systemClipboard.setContents(StringSelection(address), null)
    }

    override fun openExternal(address: String) = Desktop.getDesktop().browse(URI(address))
}
