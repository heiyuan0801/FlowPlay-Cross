package app.flowplay.cross.storage

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.flowplay.cross.repository.SourceRepository
import java.io.File

fun desktopDataDirectory(): File {
    System.getProperty("flowplay.dataDir")?.let { return File(it) }
    val home = System.getProperty("user.home")
    return when {
        System.getProperty("os.name").startsWith("Windows") -> File(
            System.getenv("APPDATA") ?: File(home, "AppData/Roaming").path, "FlowPlay")
        System.getProperty("os.name").startsWith("Mac") -> File(home, "Library/Application Support/FlowPlay")
        else -> File(System.getenv("XDG_DATA_HOME") ?: File(home, ".local/share").path, "FlowPlay")
    }
}

fun desktopSourceRepository(): SourceRepository {
    val directory = desktopDataDirectory()
    check(directory.isDirectory || directory.mkdirs()) { "无法创建应用数据目录。" }
    return SourceRepository(JdbcSqliteDriver("jdbc:sqlite:${File(directory, "flowplay.db").absolutePath}",
        schema = FlowDatabase.Schema))
}
