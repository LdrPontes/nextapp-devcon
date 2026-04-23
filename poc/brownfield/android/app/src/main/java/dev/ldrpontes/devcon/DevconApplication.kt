package dev.ldrpontes.devcon

import android.app.Application
import dev.ldrpontes.devconrn.rn.ReactNativeHostManager

class DevconApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        MigrationBridge.loadCachedState(this)
        ReactNativeHostManager.initialize(this) {
            println("JS bundle loaded")
            MigrationBridge.start(this)
        }
    }
}
