package kr.co.jness.momi.eclean.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.os.UserHandle
import android.widget.Toast
import kr.co.jness.momi.eclean.R

class DeviceAdminReceiver : DeviceAdminReceiver() {

    private fun showToast(context: Context, msg: String) {
        msg.let { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onEnabled(context: Context, intent: Intent) =
            showToast(context, context.getString(R.string.enabled_admin_permission))

    override fun onDisabled(context: Context, intent: Intent) =
            showToast(context, context.getString(R.string.disabled_admin_permission))

}