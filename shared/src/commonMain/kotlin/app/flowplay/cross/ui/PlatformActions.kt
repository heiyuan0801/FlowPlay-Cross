package app.flowplay.cross.ui

interface PlatformActions {
    suspend fun readSourceDocument(): String?
    suspend fun writeSourceDocument(content: String): Boolean
    fun copyAddress(address: String)
    fun openExternal(address: String)
}
