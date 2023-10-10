package kr.co.jness.momi.eclean.utils

import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.appcompat.app.AlertDialog
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.receiver.DeviceAdminReceiver
import kr.co.jness.momi.eclean.service.UrlInterceptorService

object PermissionCheckUtil {

    fun isAllPermissionAllowed(context: Context) : Boolean {
        return checkAdminPermission(context) && checkUsageAccess(context) && checkAccessibilityService(context)
    }

    fun checkAdminPermission(context: Context) : Boolean {
        val dPM = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminName = ComponentName(context, DeviceAdminReceiver::class.java)
        return dPM.isAdminActive(adminName)
    }

    fun checkUsageAccess(context: Context) : Boolean {
        try {
            val packageManager = context.packageManager
            val applicationInfo =
                packageManager.getApplicationInfo(context.packageName, 0)
            val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            var mode = 0
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName
                )
            }
            if (mode == AppOpsManager.MODE_ALLOWED) {
                return true
            }
        } catch (e: PackageManager.NameNotFoundException) {

        }

        return false
    }

    fun checkAccessibilityService(context: Context) : Boolean {

        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices: List<AccessibilityServiceInfo> =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)

        for (enabledService in enabledServices) {
            val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
            if (enabledServiceInfo.packageName == context.packageName
                && enabledServiceInfo.name == UrlInterceptorService::class.java.name) {
                return true
            }
        }

        return false
    }

}