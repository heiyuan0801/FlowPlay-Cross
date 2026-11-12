package app.flowplay.cross.model

import kotlinx.serialization.Serializable

@Serializable
enum class SourceKind { Emby, Cms, TvBox, LivePlaylist, Local }

@Serializable
data class MediaSource(
    val id: String,
    val name: String,
    val kind: SourceKind,
    val endpoint: String? = null,
    val enabled: Boolean = true,
)

@Serializable
data class MediaId(val sourceId: String, val value: String)

@Serializable
enum class MediaKind { Movie, Series, Episode, Video, LiveChannel }

@Serializable
data class MediaItem(
    val id: MediaId,
    val title: String,
    val kind: MediaKind,
    val year: Int? = null,
    val posterUrl: String? = null,
    val seriesId: MediaId? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val externalIds: Map<String, String> = emptyMap(),
)

data class MediaPage(val items: List<MediaItem>, val nextPage: Int? = null)

data class PlaybackProgress(
    val mediaId: MediaId,
    val positionMs: Long,
    val durationMs: Long?,
    val paused: Boolean,
    val event: Event,
) {
    enum class Event { Started, Progress, Stopped }
}
