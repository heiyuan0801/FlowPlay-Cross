package app.flowplay.cross.playback

import app.flowplay.cross.model.MediaId
import kotlinx.coroutines.flow.StateFlow

/** Ephemeral authenticated URLs and headers must never be serialized or logged. */
class PlaybackRequest(
    val mediaId: MediaId,
    val uri: String,
    val headers: Map<String, String> = emptyMap(),
    val startPositionMs: Long = 0,
    val isLive: Boolean = false,
)

data class PlaybackTrack(val id: String, val title: String, val language: String? = null)
data class BufferedRange(val startMs: Long, val endMs: Long)
enum class PlaybackStatus { Idle, Loading, Playing, Paused, Buffering, Ended, Failed }

data class PlaybackState(
    val status: PlaybackStatus = PlaybackStatus.Idle,
    val positionMs: Long = 0,
    val durationMs: Long? = null,
    val buffered: List<BufferedRange> = emptyList(),
    val audioTracks: List<PlaybackTrack> = emptyList(),
    val subtitleTracks: List<PlaybackTrack> = emptyList(),
    val errorMessage: String? = null,
)

/** Android Media3 and desktop libmpv implement this contract in M3. */
interface PlaybackEngine {
    val state: StateFlow<PlaybackState>
    suspend fun load(request: PlaybackRequest)
    fun play()
    fun pause()
    suspend fun seek(positionMs: Long)
    fun setRate(rate: Float)
    fun setVolume(volume: Float)
    fun selectAudioTrack(id: String?)
    fun selectSubtitleTrack(id: String?)
    suspend fun addExternalSubtitle(uri: String)
    fun stop()
    fun release()
}
