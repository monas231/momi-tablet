package kr.co.jness.momi.eclean.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*


object MomiUtils {

    /**
     * 주어진 파일의 시작부터 주어진 크기만큼 읽어들여 byte array 로 반환한다.
     */
    fun getByteArrayFromFile(source: File, size: Int): ByteArray? {
        if (!source.exists() || !source.canRead() || !source.isFile) return null

        val arr = ByteArray(size)
        source.inputStream().use {
            val total = it.read(arr)
            Logger.d("total size read : $total")
        }
        return arr
    }

    /**
     * 주어진 파일의 스트림에서 시작부터 주어진 크기만큼 읽어들여 byte array 로 반환한다.
     */
    fun getByteArrayFromInputStream(stream: InputStream, size: Int): ByteArray {
        val arr = ByteArray(size)
        stream.use {
            val total = it.read(arr)
            Logger.d("total size read : $total")
        }
        return arr
    }

    fun getTimeInMillis(dayOfWeek: Int = 0, hourOfDay: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            time = Date()
            if (dayOfWeek > 0) set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
        }.timeInMillis

    fun getTodayOfWeek() = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    fun getBlockCountKey(): String {
        val today = SimpleDateFormat("yyyyMM").format(Date())
        return "block_count_$today"
    }

    fun isOnline(context: Context): Boolean {
        val connectivityMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            // Checking internet connectivity
            connectivityMgr.activeNetworkInfo != null
        } else {
            connectivityMgr.allNetworks.any {
                val nc = connectivityMgr.getNetworkCapabilities(it)
                nc != null && (nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
            }
        }
    }

    fun getAppName(context: Context, packageName: String): CharSequence? {
        val pm = context.packageManager
        return try {
            val packageInfo = pm.getPackageInfo(packageName, 0)
            pm.getApplicationLabel(packageInfo.applicationInfo)
        } catch (e: Exception) {
            null
        }
    }

    fun getSystemApps(context: Context) = run {
        val pm: PackageManager = context.packageManager
        val list = pm.getInstalledPackages(0)
        list.filter {
            val ai = pm.getApplicationInfo(it.packageName, 0)
            ai.flags and ApplicationInfo.FLAG_SYSTEM != 0
        }.map { it.packageName }
    }

}