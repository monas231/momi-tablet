package kr.co.jness.momi.eclean.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson

object PreferenceUtils {


    fun getPreference(context: Context): SharedPreferences {
        return context.getSharedPreferences("eclean", Context.MODE_PRIVATE)
    }

    fun putString(context: Context, key: String, value: String) {
        getPreference(context).edit().apply {
            this.putString(key, value)
            commit()
        }
    }

    fun getString(context: Context, key: String) : String? {
        return getPreference(context).getString(key, null)
    }

    fun putInt(context: Context, key: String, value: Int) {
        getPreference(context).edit().apply {
            this.putInt(key, value)
            commit()
        }
    }

    fun getInt(context: Context, key: String, defaultValue: Int = -1) : Int {
        return getPreference(context).getInt(key, defaultValue)
    }

    fun putLong(context: Context, key: String, value: Long) {
        getPreference(context).edit().apply {
            this.putLong(key, value)
            commit()
        }
    }

    fun getLong(context: Context, key: String, defValue: Long = -1) : Long {
        return getPreference(context).getLong(key, defValue)
    }

    fun putObject(context: Context, key: String, value: Any) {
        getPreference(context).edit {
            putString(key, Gson().toJson(value))
        }
    }

    fun <T> getObject(context: Context, key: String, type: Class<T>): T? {
        val result = getPreference(context).getString(key, null)
        return if (result != null) Gson().fromJson(result, type) else null
    }

    fun putBoolean(context: Context, key: String, value: Boolean) {
        getPreference(context).edit {
            putBoolean(key, value)
        }
    }

    fun getBoolean(context: Context, key: String): Boolean {
        return getPreference(context).getBoolean(key, false)
    }

    fun removeValue(context: Context, key: String) {
        getPreference(context).edit {
            remove(key)
        }
    }

}

enum class Value(val type: String) {
    DEVICE_ID("device_id"),
    SCHOOL_ID("school_id"),
    SUBSCRIBE_NOTICE("subscribe_notice"),
    SUBSCRIBE_RULE("subscribe_rule"),
    PING_INTERVAL_TIME("ping_interval_time"),
    LICENSE("user_key"),
    EXPIRARION_DATE("expiration_date"),
    LOCAL_VIDEO_HASH_VERSION("local_video_hash_version"),
    LOCAL_WHITE_LIST_VERSION("local_whitelist_version")
}