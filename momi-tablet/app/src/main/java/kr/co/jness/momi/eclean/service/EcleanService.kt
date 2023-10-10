package kr.co.jness.momi.eclean.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.module.FilterWordScoreFilter
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.common.*
import kr.co.jness.momi.eclean.database.DbRepository
import kr.co.jness.momi.eclean.database.NetworkFileUpdater
import kr.co.jness.momi.eclean.extension.toMD5
import kr.co.jness.momi.eclean.model.*
import kr.co.jness.momi.eclean.presentation.main.MainActivity
import kr.co.jness.momi.eclean.service.RecursiveFileObserver.FileObserverListener
import kr.co.jness.momi.eclean.utils.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.HashMap

@AndroidEntryPoint
class EcleanService : Service() {

    companion object {
        const val FILE_OBSERVER_MASK = FileObserver.OPEN
        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_SET_VALUE = 3
//        const val MSG_UPDATE_RULE = 4
        const val MSG_LOGOUT = 5

        var isRunning = false
    }

    @Inject
    lateinit var apiRepository: ApiRepository
    @Inject
    lateinit var dbRepository: DbRepository
    @Inject
    lateinit var app: EcleanApplication
    @Inject
    lateinit var filterWord: FilterWordScoreFilter

    @Inject
    lateinit var networkFileUpdater: NetworkFileUpdater

    private val videoExtensions by lazy { resources.getStringArray(R.array.video_extensions) }

    /**
     * 연속해서 체크하는 것을 방지하기 위해, 최종시간을 설정해둔다.
     */
    private val previousFileDetections: HashMap<String, Long?> = HashMap()
    private val disposable = CompositeDisposable()
    private val pingDisposable = CompositeDisposable()

    /**
     * 파일 접근 이벤트를 수신 시 동작
     */
    private val rfObserver by lazy {
        RecursiveFileObserver(
            Environment.getExternalStorageDirectory(),
            FILE_OBSERVER_MASK
        ).apply {
            listener = object : FileObserverListener {
                override fun onEventReceived(event: Int, path: String) {
                    CoroutineScope(computeDispatcher).launch {
                        try {

                            val eventTime = System.currentTimeMillis()
                            val lastCheckTime = if (previousFileDetections.containsKey(path)) {
                                previousFileDetections[path]!!
                            } else {
                                0.toLong()
                            }

                            /**
                             * 500ms 이하 중복 처리 안함.
                             */
                            val passedTime = eventTime - lastCheckTime
                            if (passedTime > 500) {

                                Logger.d("event : $event, path : $path")
                                Logger.d("RecursiveFileObserver : passedTime = ${passedTime}")

                                val checkFile = File(path)
                                val fileExtension = checkFile.extension.toLowerCase(Locale.getDefault())
                                if (ruleInfoManager?.blockExtensions?.any { it == fileExtension } == true) {

                                    /**
                                     * 확장자 차단
                                     */
                                    previousFileDetections[path] = eventTime
                                    checkFileBlock(fileExtension, checkFile.name)
                                }
                                else if (videoExtensions.any { it == fileExtension }) {
                                    /**
                                     * video의 경우 Hash값 생성하여 API 호출
                                     */
                                    previousFileDetections[path] = eventTime
                                    checkVideoBlock(path, checkFile.name)
                                }

                                /**
                                 * 파일명으로 score 계산하여 block
                                 */
                                if(::filterWord.isInitialized && filterWord.isOverWordScore(checkFile.name, app.blockScore)) {
                                    UrlInterceptorService.instance?.accessNotAllowedVideo(checkFile.name)
                                }
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

            }
        }
    }
    private val computeDispatcher by lazy { Dispatchers.Default }
    private val ioDispatcher by lazy { Dispatchers.IO }
    private lateinit var mMessenger: Messenger
    private val mClients = mutableListOf<Messenger>()
    private var mValue = 0
    private val ruleInfo by lazy { dbRepository.loadRuleInfo() }

    /**
     * Rule 정보 변경되면, Ping update 주기를 재설정한다.
     */
    private val mObserver = Observer<RuleInfoVO?> {

        Logger.d("----- rule updated")

        CoroutineScope(computeDispatcher).launch {
            ruleInfoManager = if (it == null) null else RuleInfoManager(it, MomiUtils.getTodayOfWeek() + 1)
        }

        val syncTime = it?.syncTime?.toLong() ?: Constant.PING_DELAY_SECOND
        val intervalTime = PreferenceUtils.getLong(applicationContext, Value.PING_INTERVAL_TIME.type)
        if (syncTime != intervalTime || pingDisposable.size() == 0) {
            PreferenceUtils.putLong(applicationContext, Value.PING_INTERVAL_TIME.type, syncTime)
            startPingTimer(syncTime)
        }

        it?.let {
            app.blockScore = it.blockScore
            Logger.d("----- rule blockScore = ${it.blockScore}")
        }

        updateFile()
    }
    private var ruleInfoManager: RuleInfoManager? = null

    override fun onCreate() {
        super.onCreate()

        startObserve()
    }

    private fun insertLocalVideoHashs() {
        val mWork = OneTimeWorkRequestBuilder<EcleanWorker>().apply {
            setInputData(workDataOf(WORK_TYPE to WorkType.INSERT_DB.number))
//            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }.build()
        WorkManager.getInstance(applicationContext)
            .enqueue(mWork)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startForeground()

        /**
         * 서비스 실행 시, File observer를 등록한다.
         */
        CoroutineScope(computeDispatcher).launch {
            rfObserver.startWatching()
            Logger.d("startWatching")
        }

        insertLocalVideoHashs()
        updateRule()

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        mMessenger = Messenger(ServiceHandler(this))
        return mMessenger.binder
    }

    override fun onDestroy() {
        super.onDestroy()

        isRunning = false
        stopObserve()
        rfObserver.stopWatching()
        Logger.d("EcleanService onDestroy, stopWatching")
        disposable.clear()
        pingDisposable.clear()
    }

    private fun startObserve() {
        ruleInfo.observeForever(mObserver)
    }

    private fun stopObserve() {
        ruleInfo.removeObserver(mObserver)
    }

    private fun startForeground() {
        val homeIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, homeIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notification = NotificationCompat.Builder(applicationContext, "momi")
            .setSmallIcon(R.mipmap.ico_notification)
            .setContentTitle(getString(R.string.app_name))
            .setContentIntent(pendingIntent)
            .setContentText(getString(R.string.service_is_running))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("momi", "clean", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channel.setShowBadge(false)
            manager.createNotificationChannel(channel)
        }

        startForeground(NOTIFICATION_ID_SERVICE, notification.build())
    }

    /**
     * ping update 시간 주기에 따라 ping api 호출
     */
    private fun startPingTimer(intervalTime: Long = Constant.PING_DELAY_SECOND) {
        try {
            if (pingDisposable.size() > 0 && !pingDisposable.isDisposed) pingDisposable.dispose()

            Logger.d("callPing time started")

            pingDisposable +=
                Flowable.interval(0, intervalTime, TimeUnit.SECONDS)
                    .take(Long.MAX_VALUE)
                    .filter { MomiUtils.isOnline(applicationContext) }
                    .subscribeOn(Schedulers.trampoline())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        callPing()
                    }, {
                        it.printStackTrace()
                    })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * ping 수행
     */
    private fun callPing() {

        try {
            app.getDeviceId()?.let {

                disposable += apiRepository.sendPing(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeBy(
                        onSuccess = { p ->
                            Logger.d("callPing")
                            if (p.resultCode == 1) {
                                if (p.data.flag == 1) {
                                    updateRule()
                                }
                            } else if(p.resultCode == 9) {

                                /**
                                 * 라이센스 만료로 로그아웃 처리.
                                 */
                                mMessenger.send(Message().apply {
                                    this.what = MSG_LOGOUT
                                })

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val channel = NotificationChannel(
                                        EcleanFCMService.CHANNEL_ID_NOTICE,
                                        EcleanFCMService.CHANNEL_NAME_NOTICE, NotificationManager.IMPORTANCE_LOW)
                                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                    channel.setShowBadge(false)
                                    manager.createNotificationChannel(channel)
                                }

                                val mIntent = Intent()
                                val pendingIntent = PendingIntent.getActivity(applicationContext, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                                val builder = NotificationCompat.Builder(applicationContext, EcleanFCMService.CHANNEL_ID_NOTICE)
                                    .setSmallIcon(R.mipmap.ico_notification)
                                    .setContentTitle(getString(R.string.app_name))
                                    .setContentText(getString(R.string.use_license_another_device))
                                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                    .setContentIntent(pendingIntent)
                                    .setAutoCancel(true)
                                NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID_LOGOUT, builder.build())

                                stopSelf()
                                app.stopMonitoring()

                            }
                        },
                        onError = {
                            it.printStackTrace()
                        }
                    )
            }

            /**
             * 1회에 한해, 기기 설치 API호출
             */
            if (app.getDeviceId() == null) {
                notifyInstallApp {
                    subscribeAllFCMTopic()
                    checkUpdatingData()
                }
            } else {
                checkUpdatingData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun notifyInstallApp(callback: () -> Unit) {
        disposable += apiRepository.notifyInstallApp()
            .subscribe({
                PreferenceUtils.putLong(applicationContext, Value.DEVICE_ID.type, it.data.deviceId)
                callback.invoke()
            }, {})
    }

    /**
     * 날짜 변경 시, Rule 정보 및 캐시 데이터 리셋
     */
    private fun checkUpdatingData() {

        Logger.d("----- checkUpdatingData")

        try {
            val updatedAt = ruleInfo.value?.updatedAt
            if (updatedAt != null) {
                val lastDate = Calendar.getInstance().apply {
                    time = updatedAt
                }
                val currentDate = Calendar.getInstance()
                if (lastDate.get(Calendar.DAY_OF_YEAR) != currentDate.get(Calendar.DAY_OF_YEAR)
                    && lastDate.get(Calendar.YEAR) != currentDate.get(Calendar.YEAR)
                ) {
                    updateData()
                }
            } else {
                updateData()
            }
        } catch (e: Exception) {
            Logger.e(e.message ?: "")
        }
    }

    private fun updateData() {

        Logger.d("----- updateData")

        try {
            updateRule()
            UrlInterceptorService.instance?.clearMap()
        } catch (e: Exception) {
            Logger.e(e.message ?: "")
        }
    }

    /**
     * 파일 업데이트
     */
    private fun updateFile() {

        Logger.d("----- rule updateFile")

        try {
            val deviceId = app.getDeviceId()
            val schoolId = app.getSchoolId()
            if (deviceId != null) {

                Logger.d("----- checkFile API 호출")

                disposable += apiRepository.checkFile(deviceId, schoolId)
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                        { res ->
                            networkFileUpdater.addFileList(res.data.fileList)

                        }, {
                            Logger.e(it.message ?: "")
                        }
                    )

                disposable += filterWord.updateKeywordApi(deviceId, schoolId)

                loadFilterWords()
            }
        } catch (e: Exception) {
            Logger.e(e.message ?: "")
        }
    }

    private fun loadFilterWords() {
        filterWord.loadFilterWord()
        if(filterWord.serverFilterWords.isNullOrEmpty()) {
            disposable += filterWord.loadServerFilterWord()
        }
    }


    /**
     * 정책정보 업데이트
     */
    private fun updateRule() {

        Logger.d("----- updateRule")

        try {
            val deviceId = app.getDeviceId()
            val schoolId = app.getSchoolId()
            if (deviceId != null) {
                Logger.d("----- apiRepository.requestRuleInfo")

                disposable += apiRepository.requestRuleInfo(deviceId, schoolId)
                    .subscribe(
                        { res ->
                            CoroutineScope(ioDispatcher).launch {
                                val savedData = res.data
                                savedData.updatedAt = Calendar.getInstance().time
                                /**
                                 * rule 정보 저장
                                 */
                                dbRepository.setRuleInfo(listOf(savedData))
                                /**
                                 * 규칙정보가 업데이트되면 history 도 비워준다.
                                 */
                                disposable += dbRepository.deleteAllApiCache()
                                    .subscribe()
                            }
                        }, {
                            Logger.e(it.message ?: "")
                        }
                    )
            }
        } catch (e: Exception) {
            Logger.e(e.message ?: "")
        }
    }

    /**
     * video hash 값 추출
     */
    private fun getHashData(path: String) = File(path).inputStream().use {
        val arr = ByteArray(1024 * 1024)
        val total = it.read(arr)
        arr.toMD5()
    }

    /**
     * FCM Topic 등록(school id)
     */
    private fun subscribeAllFCMTopic() {
        val schoolId = app.getSchoolId()

        /**
         * notice
         */
        val noticeValue = PreferenceUtils.getString(this, Value.SUBSCRIBE_NOTICE.type)
        if (noticeValue == null && schoolId != null) {
            val topicName = FirebaseTopic.NOTICE.topicName + "_$schoolId"
            Firebase.messaging
                .subscribeToTopic(topicName)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        PreferenceUtils.putString(this, Value.SUBSCRIBE_NOTICE.type, topicName)
                    }
                }
        }

        /**
         * rule
         */
        val ruleValue = PreferenceUtils.getString(this, Value.SUBSCRIBE_RULE.type)
        if (ruleValue == null && schoolId != null) {
            val topicName = FirebaseTopic.RULE.topicName + "_$schoolId"
            Firebase.messaging
                .subscribeToTopic(topicName)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        PreferenceUtils.putString(this, Value.SUBSCRIBE_RULE.type, topicName)
                    }
                }
        }
    }

    /**
     * topic 해제
     */
    private fun unsubscribeAllFCMTopic() {
        PreferenceUtils.getString(this, Value.SUBSCRIBE_NOTICE.type)?.let { value ->
            Firebase.messaging
                .unsubscribeFromTopic(value)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        PreferenceUtils.removeValue(this, Value.SUBSCRIBE_NOTICE.type)
                    }
                }
        }

        PreferenceUtils.getString(this, Value.SUBSCRIBE_RULE.type)?.let { value ->
            Firebase.messaging
                .unsubscribeFromTopic(value)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        PreferenceUtils.putBoolean(this, Value.SUBSCRIBE_RULE.type, false)
                    }
                }
        }
    }

    /**
     * file 차단 API
     */
    private fun checkFileBlock(extension: String, fileName: String) {
        val deviceId = app.getDeviceId()
        val schoolId = app.getSchoolId()
        if (deviceId != null) {
            apiRepository.blockFile(deviceId, schoolId, extension)
                .subscribe({
                }, {

                })

            /**
             * 전송 후, 무조건 차단
             */
            UrlInterceptorService.instance?.accessNotAllowedVideo(fileName)
        }
    }


    /**
     * video 차단 DB
     */
    private fun checkVideoBlock(path: String, fileName: String) {
        try {
            val deviceId = app.getDeviceId()
            if (deviceId != null) {
                val md5Result = getHashData(path)

                Logger.d("----- [VIDEOHASH] check video hash = $md5Result")

                disposable += Single.zip(
                    dbRepository.getLocalVideoHashCount(md5Result),
                    dbRepository.getVideoHashCount(md5Result),
                    BiFunction<Int, Int, Int> { localCount, serverCount -> localCount+serverCount }
                ).subscribe { t1, t2 ->
                    if(t1>0) {
                        Logger.d("----- [VIDEOHASH] check video hash = $md5Result is exist")
                        UrlInterceptorService.instance?.accessNotAllowedVideo(fileName)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class ServiceHandler(context: Context) : Handler(context.mainLooper) {

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT -> mClients.add(msg.replyTo)
                MSG_UNREGISTER_CLIENT -> mClients.remove(msg.replyTo)
                MSG_SET_VALUE -> {
                    mValue = msg.arg1
                    for (i in mClients.indices.reversed()) {
                        try {
                            mClients[i].send(Message.obtain(null, MSG_SET_VALUE, mValue, 0))
                        } catch (e: RemoteException) {
                            // The client is dead.  Remove it from the list;
                            // we are going through the list from back to front
                            // so this is safe to do inside the loop.
                            mClients.removeAt(i)
                        }
                    }
                }
//                MSG_UPDATE_RULE -> {
//                    updateRule()
//                }
                MSG_LOGOUT -> {
                    for (i in mClients.indices.reversed()) {
                        try {
                            mClients[i].send(Message.obtain(null, MSG_LOGOUT, 0))
                        } catch (e: RemoteException) {
                            mClients.removeAt(i)
                        }
                    }
                }
            }
        }
    }

}