package app.flowplay.cross

import android.app.Application
import app.flowplay.cross.network.CatalogHttp
import app.flowplay.cross.storage.androidSourceRepository

class FlowPlayApplication : Application() {
    val repository by lazy { androidSourceRepository(this) }
    val http by lazy { CatalogHttp() }
}
