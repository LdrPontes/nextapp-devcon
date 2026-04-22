package dev.ldrpontes.devcon

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise

class NativeStorageModule(reactContext: ReactApplicationContext) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName() = "NativeStorage"

    @ReactMethod
    fun getItem(key: String, promise: Promise) {
        val prefs = reactApplicationContext.getSharedPreferences("NativeStorage", android.content.Context.MODE_PRIVATE)
        val value = prefs.getString(key, null)
        promise.resolve(value)
    }
}
