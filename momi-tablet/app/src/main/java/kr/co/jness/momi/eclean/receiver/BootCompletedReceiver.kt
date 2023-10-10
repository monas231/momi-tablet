package kr.co.jness.momi.eclean.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import kr.co.jness.momi.eclean.service.EcleanService

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED && !EcleanService.isRunning) {
            startObserverService(context)
        }
    }

    private fun startObserverService(context: Context) {
        val serviceIntent = Intent(context, EcleanService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        }
        else {
            context.startService(serviceIntent)
        }
    }

}