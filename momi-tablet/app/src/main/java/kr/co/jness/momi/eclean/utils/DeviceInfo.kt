package kr.co.jness.momi.eclean.utils

import android.os.Build
import java.util.*


object DeviceInfo {

    fun getDeviceUniqueId(): String {

        val deviceId = ("35" +
                Build.BOARD.length % 10
                + Build.BRAND.length % 10
                + Build.CPU_ABI.length % 10
                + Build.DEVICE.length % 10
                + Build.MANUFACTURER.length % 10
                + Build.MODEL.length % 10
                + Build.PRODUCT.length % 10)

        var serial: String? = null
        try {
            serial = Build::class.java.getField("SERIAL")[null].toString()

            val uuid = UUID(deviceId.hashCode().toLong(), serial.hashCode().toLong()).toString()
            Logger.d("UUID = $uuid")
            return uuid
        } catch (e: Exception) {
            serial = "serial" // some value
        }

        val uuid =  UUID(deviceId.hashCode().toLong(), serial.hashCode().toLong()).toString()
        Logger.d("UUID = $uuid")
        return uuid
    }
}
