package kr.co.jness.momi.eclean.utils

import android.content.Context

object LicenseUtil {

    fun clearLicense(context: Context) {
        PreferenceUtils.removeValue(context, Value.EXPIRARION_DATE.type)
        PreferenceUtils.removeValue(context, Value.LICENSE.type)
        PreferenceUtils.removeValue(context, Value.SCHOOL_ID.type)
    }

}