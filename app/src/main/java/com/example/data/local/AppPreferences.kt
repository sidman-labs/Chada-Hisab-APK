package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("chada_hishab_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SETUP_COMPLETED = "setup_completed"
        private const val KEY_ACTIVE_ORG_ID = "active_org_id"
        private const val KEY_PIN_LOCK_ENABLED = "pin_lock_enabled"
        private const val KEY_PIN_CODE = "pin_code"
        private const val KEY_DARK_MODE = "dark_mode"
    }

    var isSetupCompleted: Boolean
        get() = prefs.getBoolean(KEY_SETUP_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_SETUP_COMPLETED, value).apply()

    var activeOrgId: Long
        get() = prefs.getLong(KEY_ACTIVE_ORG_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_ACTIVE_ORG_ID, value).apply()

    var isPinLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_PIN_LOCK_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_PIN_LOCK_ENABLED, value).apply()

    var pinCode: String
        get() = prefs.getString(KEY_PIN_CODE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PIN_CODE, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()
}
