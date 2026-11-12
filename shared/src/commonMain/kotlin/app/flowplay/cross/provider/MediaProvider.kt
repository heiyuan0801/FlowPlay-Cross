package app.flowplay.cross.provider

import app.flowplay.cross.model.MediaId
import app.flowplay.cross.model.MediaItem
import app.flowplay.cross.model.MediaPage
import app.flowplay.cross.model.MediaSource
import app.flowplay.cross.model.PlaybackProgress
import app.flowplay.cross.playback.PlaybackRequest
import kotlinx.coroutines.flow.Flow

/** Each provider owns pagination and source permissions. UI consumes a repository in M1. */
interface MediaProvider {
    val source: MediaSource
    suspend fun libraries(): List<MediaItem>
    suspend fun items(library: MediaId?, page: Int): MediaPage
    fun search(query: String): Flow<MediaPage>
    suspend fun detail(id: MediaId): MediaItem
    suspend fun episodes(series: MediaId, season: Int): List<MediaItem>
    suspend fun playbackInfo(id: MediaId): PlaybackRequest
    suspend fun downloadResource(id: MediaId): DownloadResource
    suspend fun reportProgress(progress: PlaybackProgress)
}

/** Permission is resolved by the provider before a downloader receives this resource. */
data class DownloadResource(
    val mediaId: MediaId,
    val request: PlaybackRequest,
    val format: Format,
) {
    enum class Format { Progressive, Hls }
}
