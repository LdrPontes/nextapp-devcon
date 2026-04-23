package dev.ldrpontes.devcon

import android.content.Context
import com.callstack.reactnativebrownfield.OnMessageListener
import com.callstack.reactnativebrownfield.ReactNativeBrownfield
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.json.JSONObject

object MigrationBridge {
    private const val MIGRATION_PREFS = "MigrationState"
    private const val MIGRATED_KEY = "mmkv_migrated_username"

    private val _migratedUsername = MutableStateFlow<String?>(null)
    val migratedUsername: StateFlow<String?> = _migratedUsername

    private var registered = false

    fun loadCachedState(context: Context) {
        if (_migratedUsername.value != null) return
        _migratedUsername.value = context.applicationContext
            .getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
            .getString(MIGRATED_KEY, null)
    }

    fun start(context: Context) {
        val appContext = context.applicationContext
        loadCachedState(appContext)
        if (registered) return
        registered = true

        ReactNativeBrownfield.shared.addMessageListener(OnMessageListener { raw ->
            val json = runCatching { JSONObject(raw) }.getOrNull() ?: return@OnMessageListener
            when (json.optString("type")) {
                "getNativeValue" -> {
                    val requestId = json.optString("requestId")
                    val key = json.optString("key")
                    val prefs = appContext.getSharedPreferences(
                        "NativeStorage",
                        Context.MODE_PRIVATE
                    )
                    val value = prefs.getString(key, null)
                    val response = JSONObject().apply {
                        put("type", "nativeValueResponse")
                        put("requestId", requestId)
                        put("value", value ?: JSONObject.NULL)
                    }
                    ReactNativeBrownfield.shared.postMessage(response.toString())
                }
                "migrationDone" -> {
                    val value = if (json.isNull("value")) null else json.optString("value")
                    appContext.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
                        .edit()
                        .putString(MIGRATED_KEY, value)
                        .apply()
                    _migratedUsername.value = value
                }
            }
        })
    }
}
