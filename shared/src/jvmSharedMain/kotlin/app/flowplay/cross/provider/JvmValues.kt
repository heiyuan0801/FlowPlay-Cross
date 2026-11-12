package app.flowplay.cross.storage

actual fun sourceId(): String = java.util.UUID.randomUUID().toString()
actual fun currentTimeMillis(): Long = System.currentTimeMillis()
