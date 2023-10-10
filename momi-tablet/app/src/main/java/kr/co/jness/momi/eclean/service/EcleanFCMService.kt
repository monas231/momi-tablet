package kr.co.jness.momi.eclean.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.FCM_TOKEN
import kr.co.jness.momi.eclean.common.EcleanWorker
import kr.co.jness.momi.eclean.common.WORK_TYPE
import kr.co.jness.momi.eclean.common.WorkType
import kr.co.jness.momi.eclean.dto.FCMDto
import kr.co.jness.momi.eclean.presentation.splash.SplashActivity
import kr.co.jness.momi.eclean.utils.Constant
import kr.co.jness.momi.eclean.utils.Logger
import java.util.concurrent.TimeUnit


const val NOTIFICATION_ID_SERVICE = 0x01
const val NOTIFICATION_ID_NOTICE = 0x02
const val NOTIFICATION_ID_LOGOUT = 0x03

class EcleanFCMService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ID_NOTICE = "eclean_notice"
        const val CHANNEL_ID_RULE = "eclean_rule"

        const val CHANNEL_NAME_NOTICE = "공지사항"
        const val CHANNEL_NAME_RULE = "규칙 업데이트"
    }

    override fun handleIntent(intent: Intent) {
        super.handleIntent(intent)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Logger.d("From: ${remoteMessage.from}")

        if (remoteMessage.data.isNotEmpty()) {
            Logger.d("Message data payload: ${remoteMessage.data}")
            val mFrom = remoteMessage.from ?: ""
            if (mFrom.contains("notice")) {
                remoteMessage.data.values.firstOrNull()?.let {
                    val response = Gson().fromJson(it, FCMDto::class.java)
                    notifyFCM(CHANNEL_ID_NOTICE, response.title, response.body)
                }
            }
            else if (mFrom.contains("rule")) {
                UrlInterceptorService.instance?.clearMap()
                updateRule()
            }

        }

        remoteMessage.notification?.let {
            Logger.d("Message Notification Body: ${it.body}")
        }

    }

    override fun onMessageSent(p0: String) {
        super.onMessageSent(p0)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        registerFCMToken(token)
    }

    private fun updateRule() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = workDataOf(
            WORK_TYPE to WorkType.UPDATE_RULE_INFO.number
        )
        val mWork = OneTimeWorkRequestBuilder<EcleanWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                Constant.PING_DELAY_SECOND,
                TimeUnit.SECONDS
            )
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueue(mWork)
    }

    /**
     * 서버에 fcm token 을 업로드 한다.
     */
    private fun registerFCMToken(token: String) {
        createNotificationChannel(CHANNEL_ID_NOTICE, CHANNEL_NAME_NOTICE, true)
        createNotificationChannel(CHANNEL_ID_RULE, CHANNEL_NAME_RULE, false)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = workDataOf(
            FCM_TOKEN to token,
            WORK_TYPE to WorkType.REGISTER_FCM_TOKEN.number
        )
        val mWork = OneTimeWorkRequestBuilder<EcleanWorker>()
            .setInputData(inputData)
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                Constant.PING_DELAY_SECOND,
                TimeUnit.SECONDS
            )
            .build()
        WorkManager.getInstance(applicationContext)
            .enqueue(mWork)
    }

    private fun createNotificationChannel(id: String, name: String, enableBadge: Boolean) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(id, name, importance)
            channel.setShowBadge(enableBadge)
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     *
     */
    private fun notifyFCM(id: String, title: String, message: String) {
        val mIntent = Intent(applicationContext, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        val builder = NotificationCompat.Builder(applicationContext, id)
            .setSmallIcon(R.mipmap.ico_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID_NOTICE, builder.build())
    }
}