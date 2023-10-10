package kr.co.jness.momi.eclean.service

import android.accessibilityservice.AccessibilityService
import android.content.*
import android.graphics.Rect
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.module.FilterWordScoreFilter
import kr.co.jness.momi.eclean.api.ApiRepository
import kr.co.jness.momi.eclean.common.EcleanApplication
import kr.co.jness.momi.eclean.common.UsedTimeStatus
import kr.co.jness.momi.eclean.database.DbRepository
import kr.co.jness.momi.eclean.model.*
import kr.co.jness.momi.eclean.module.BrowserConfig
import kr.co.jness.momi.eclean.module.CheckAppConfig
import kr.co.jness.momi.eclean.module.FilterWhitelist
import kr.co.jness.momi.eclean.presentation.block.BlockActivity
import kr.co.jness.momi.eclean.utils.Constant
import kr.co.jness.momi.eclean.utils.Logger
import kr.co.jness.momi.eclean.utils.MomiUtils
import kr.co.jness.momi.eclean.utils.PermissionCheckUtil
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class UrlInterceptorService : AccessibilityService() {

    companion object {
        var instance: UrlInterceptorService? = null
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
    lateinit var whitelist: FilterWhitelist
    @Inject
    lateinit var browserConfig: BrowserConfig

    private val disposable = CompositeDisposable()

    private val accessbilityMenus = mutableListOf<BlockMenu>()

    /**
     * url check 체크 시간
     */
    private val previousUrlDetections: HashMap<String, Long?> = HashMap()

    /**
     * url check 결과, 메모리캐시
     */
    private val resultUrlDetections: HashMap<String, Boolean?> = HashMap()
    private val resultClassTimeUrlDetections: HashMap<String, Boolean?> = HashMap()

    /**
     * package check 체크 시간
     */
    private val previousPackageDetections: HashMap<String, Long?> = HashMap()

    /**
     * package check 결과, 메모리캐시
     */
    private val resultPackageDetections: HashMap<String, Boolean?> = HashMap()

    private var alertToastTime = 0L
    private val ruleInfo by lazy { dbRepository.loadRuleInfo() }

    /**
     * 정책 정보 감시자.
     */
    private val mObserver = Observer<RuleInfoVO?> {
        CoroutineScope(Dispatchers.IO).launch {

            try {
                ruleInfoManager = it?.let {
                    RuleInfoManager(it, MomiUtils.getTodayOfWeek())
                } ?: kotlin.run {
                    null
                }

                /**
                 * whitelist는 UrlInterceptorService에서만 사용하여, 여기서만 업데이트 하면 됨.
                 */
                whitelist.setWhitelistApiItem(ruleInfoManager?.allowedUrls)

            } catch (e: Exception) {
                e.message?.let { it1 -> Logger.e(it1) }
            }
        }
    }

    /**
     * 정책 정보 매니저
     */
    private var ruleInfoManager: RuleInfoManager? = null

    /**
     * system app list
     * package 정보 값 확인
     */
    private val systemAppList by lazy {
        MomiUtils.getSystemApps(applicationContext)
    }

    var expiredLicense = false

    /**
     * block screen 중복 실행 회피
     */
    var executeBlock = true
    private val blackScreenSubject: BehaviorSubject<String> = BehaviorSubject.create()

    /**
     * 수업, 야간시간에 허용 앱.
     */
    private val blackListExcept by lazy { resources.getStringArray(R.array.black_list_except) }

    /**
     * 수업, 야간시간에 차단 앱.
     */
    private val blackList by lazy { resources.getStringArray(R.array.black_list) }

    override fun onCreate() {
        super.onCreate()

        /**
         * block screen 중복 회피.
         */
        disposable += blackScreenSubject.throttleLast(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                GlobalScope.launch {
                    delay(Constant.BACK_BUTTON_EXECUTE_DELAY)
                    BlockActivity.show(this@UrlInterceptorService, it)
                }
            }

        try {
            /**
             * 설정화면 진입 시, 차단해야하는 이름 추가
             */
            accessbilityMenus.add(BlockMenu("", applicationContext.getString(R.string.app_name)))

            applicationContext.assets.open("block_setting_menu.json").use { inputStream ->
                JsonReader(inputStream.reader()).use { jsonReader ->
                    val type = object : TypeToken<List<BlockMenu>>() {}.type
                    accessbilityMenus.addAll(Gson().fromJson(jsonReader, type))
                }
            }
        } catch (e: Exception) {
            Logger.e(e.message ?: "error load block_setting_menu")
        }
    }

    fun clearMap() {
        try {

            /**
             * memory cache를 통해, DB 접근을 최소 한다.
             */
            synchronized(previousUrlDetections){
                previousUrlDetections.clear()
            }
            synchronized(resultUrlDetections){
                resultUrlDetections.clear()
            }
            synchronized(resultClassTimeUrlDetections){
                resultClassTimeUrlDetections.clear()
            }
            synchronized(previousPackageDetections){
                previousPackageDetections.clear()
            }
            synchronized(resultPackageDetections){
                resultPackageDetections.clear()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        startObserve()
        instance = this
    }

    override fun onUnbind(intent: Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }

    private fun startObserve() {
        ruleInfo.observeForever(mObserver)
    }

    private fun stopObserve() {
        ruleInfo.removeObserver(mObserver)
    }

    /**
     * 브라우저 앱의 입력필드에서 url정보를 가져온다.
     */
    private fun captureUrl(
        info: AccessibilityNodeInfo,
        config: CheckAppConfig
    ): String? {
        var url: String? = null

        try {
            val nodes =
                info.findAccessibilityNodeInfosByViewId(config.addressBarId)
            if (nodes == null || nodes.size <= 0) {
                return null
            }
            val addressBarNodeInfo = nodes[0]
            if (addressBarNodeInfo.text != null) {
                url = addressBarNodeInfo.text.toString()
            }
            addressBarNodeInfo.recycle()

        } catch (e:Exception) {
            e.printStackTrace()
        }
        return url
    }

    /**
     * 항상 허용 앱. 설정 등.
     */
    private fun isPackageInWhiteList(packageName: String) : Boolean {
        return ruleInfoManager?.allowedApps?.any { it.info == packageName } == true
    }

    /**
     * 시스템 앱인지 검사.
     */
    private fun isSystemApp(targetPackageName: String) = systemAppList.any { it == targetPackageName }

    /**
     * 수업, 야간시간에 차단해야 할 앱인지 검사.
     */
    private fun isBlackList(targetPackageName: String) = blackList.any { it == targetPackageName }

    /**
     * 수업, 야간시간에 허용해야 할 앱인지 검사.
     */
    private fun isBlackListExcept(targetPackageName: String) =
        blackListExcept.any {
            targetPackageName.startsWith(it)
        }

    /**
     * 시간별 허용앱인지 확인
     * blacklist가 아니면서, whitelist이거나, 시스템앱이거나, 맘아이 앱 이거나, 설정이거나, 예외로 실행한앱인지 확인.
     * 수업시간이거나, 심야시간인경우에도 쓸수 있어야 하는 앱.
     */
    private fun isAllowedClassOrNight(name: String) =
        !isBlackList(name) && (
                isPackageInWhiteList(name)
                        || isSystemApp(name)
                        || name == packageName
                        || name == "com.android.settings"
                        || isBlackListExcept(name)
                )

    /**
     * 항상 사용 가능한 앱. 시스템, 맘아이 앱
     */
    private fun isAllowedApp(name: String) = isSystemApp(name) || name == packageName || name == "com.android.settings"

    /**
     * 아래의 경우에 호출이 된다.
     * 1. 화면 이동 또는 앱 실행
     * 2. 브라우저 주소입력
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if(!executeBlock || expiredLicense) {
            return
        }

        try {
            /**
             * 패키지명을 못 받아오는 경우가 있음.
             */
            val eventTime = event.eventTime
            val primaryLocale = applicationContext.resources.configuration.locales[0]

            if(event.packageName != null) {
                val targetPackageName = event.packageName.toString()

                val isConcentrateTime = ruleInfoManager?.getCurrentTimeStatus()
                /**
                 * 앱 실행을 확인 하는 부분.
                 */
                synchronized(previousPackageDetections) {
                    val lastCheckTime = if (previousPackageDetections.containsKey(targetPackageName)) {
                        previousPackageDetections[targetPackageName]!!
                    } else {
                        0.toLong()
                    }

                    /**
                     * 입력된 url의 마지막 체크 시간을 확인해서 1초 미만의 재 요청의 경우 무시 한다.
                     */
                    if (eventTime - lastCheckTime > Constant.SKIP_SAME_REQUEST_MILLISECOND) {
                        previousPackageDetections[targetPackageName] = eventTime

                        if (isConcentrateTime == UsedTimeStatus.CLASS || isConcentrateTime == UsedTimeStatus.NIGHT) {

                            /**
                             * 수업시간이거나, 심야시간인경우 쓸수 없는 앱인 경우.
                             */
                            if (!isAllowedClassOrNight(targetPackageName)) {
                                val deviceId = app.getDeviceId()
                                val schoolId = app.getSchoolId()
                                if (deviceId != null) {

                                    /**
                                     * 차단내용 서버로 전송.
                                     */
                                    disposable += apiRepository.blockFile(deviceId, schoolId, targetPackageName)
                                        .subscribeOn(Schedulers.io())
                                        .subscribe({
                                        }, {})


                                    /**
                                     * 전송 후, 무조건 차단
                                     */
                                    performHistoryBack()

                                    val appName = MomiUtils.getAppName(applicationContext, targetPackageName)
                                        ?: targetPackageName
                                    launchBlockScreen(appName.toString())
                                }
                            }
                        } else {
                            /**
                             * 자유 시간에 허용된 앱이 아닌경우.
                             */
                            if (!isAllowedApp(targetPackageName)) {
                                /**
                                 * 항상 사용 가능한 앱이거나, 시스템, 맘아이 앱이 아닌 경우.
                                 */
                                val appName = MomiUtils.getAppName(applicationContext, targetPackageName) ?: targetPackageName

                                if (resultPackageDetections.containsKey(targetPackageName)) {
                                    /**
                                     * 입력된 url의 결과가 맵에 있으면 바로 사용한다.
                                     */
                                    if (resultPackageDetections[targetPackageName] == true) {
                                        performHistoryBack()
                                        performHistoryBack(200)

                                        launchBlockScreen(appName.toString())
                                    }

                                    Logger.d("resultUrlDetections ${targetPackageName}, isblock ${resultPackageDetections[targetPackageName]}")
                                } else {

                                    /**
                                     * package 정보로 cache 조회
                                     */
                                    checkBlockCache(targetPackageName, appName.toString()) {
                                        /**
                                         * cache에 없으면 api출 호출
                                         */
                                        checkBlockPackageApi(targetPackageName)
                                    }
                                }
                            }
                        }
                    }
                }

                val parentNodeInfo = event.source ?: return

                if (parentNodeInfo.packageName.contains("com.android.settings")) {
                    /**
                     * 설정앱이 실행된 경우, 맘아이 앱 설정 변경을 막기 위한 처리.
                     */

                    val title = getActionBarTitle(parentNodeInfo)
                    Logger.d("packageNameByUsageStats title = ${primaryLocale.language}:${title}")

                    /**
                     * 설정 앱 화면내의 경우, 화면 전체에서 앱 이름 검사
                     */
                    if (parentNodeInfo.findAccessibilityNodeInfosByText(applicationContext.getString(R.string.app_name)).size > 0) {
                        val bounds = Rect()
                        parentNodeInfo.getBoundsInScreen(bounds)
                        if (bounds.left == 0 && bounds.top == 0) {

                            if(PermissionCheckUtil.isAllPermissionAllowed(applicationContext)) {
                                performHistoryBack()
                                showAlertToast()
                                return
                            }
                        }
                    }

                    accessbilityMenus.forEach {
                        if(it.language.isNullOrEmpty() || it.language == primaryLocale.language) {

                            if(it.text == title) {
                                if(PermissionCheckUtil.isAllPermissionAllowed(applicationContext)) {
                                    performHistoryBack()
                                    showAlertToast()
                                    return@forEach
                                }
                            }
                        }
                    }

                } else if (parentNodeInfo.packageName.contains("com.samsung.accessibility")) {
                    /**
                     * 접근성앱이 실행된 경우, 맘아이 앱 설정 변경을 막기 위한 처리.
                     */
                    val title = getActionBarTitle(parentNodeInfo)
                    Logger.d("UrlInterceptorService packageNameByUsageStats title = $title")

                    accessbilityMenus.forEach {
                        if(it.language.isNullOrEmpty() || it.language == primaryLocale.language) {
                            if (title != null && applicationContext.getString(R.string.app_name) == title) {
                                if(PermissionCheckUtil.isAllPermissionAllowed(applicationContext)) {
                                    performHistoryBack()
                                    showAlertToast()
                                    return@forEach
                                }
                            }
                        }
                    }

                } else {
                    /**
                     * 브라우저 정보가 없으면 처리 안함.
                     */
                    browserConfig.getAppConfig(targetPackageName)?.let {

                        /**
                         * url정보 획득
                         */
                        captureUrl(parentNodeInfo, it)?.let { captureUrl ->

                            val url = captureUrl.replace("\u200E", "")
                            val domain = UrlCheck.getUrlCheck(url)?.let {
                                it.checkDomain
                            } ?: run {
                                url
                            }

                            Logger.d("----- [WHITELIST] domain = $domain")

                            if (url.isNotEmpty() && url.contains(".") && event.contentChangeTypes!=0 &&  (event.contentChangeTypes == 7 || event.contentChangeTypes == 2 || event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)) {

                                /**
                                 * contentChangeTypes = 0 앱이 포그라운드로 올라옴.
                                 * contentChangeTypes = 7. 키패드 이동 or url 이동.
                                 * contentChangeTypes = 2. 링크 클릭.
                                 * AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED 앱 background에서 foreground로 표시.
                                 */

                                synchronized(previousUrlDetections) {

                                    /**
                                     * 입력된 url의 마지막 체크 시간을 확인해서 1초 미만의 재 요청의 경우 무시 한다.
                                     */
                                    val lastRecordedTime =
                                        if (previousUrlDetections.containsKey(url)) previousUrlDetections[url]!! else 0.toLong()
                                    if (eventTime - lastRecordedTime > Constant.SKIP_SAME_REQUEST_MILLISECOND) {

                                        previousUrlDetections[url] = eventTime

                                        /**
                                         * Whitelist인지 체크!
                                         */
                                        disposable += whitelist.isWhitelistItem(domain) {
                                            if (it) {
                                                /**
                                                 * whitelist의 경우 허용
                                                 */
                                                Logger.d("----- [WHITELIST] $url is whitelist!!!")
                                            } else {

                                                Logger.d("----- [WHITELIST] $url is not whitelist!!!")


                                                /**
                                                 * url로 score 계산하여 block
                                                 */
                                                if (::filterWord.isInitialized && filterWord.isOverWordScore(
                                                        url,
                                                        app.blockScore
                                                    )
                                                ) {
                                                    accessNotAllowedVideo(url)
                                                    return@isWhitelistItem
                                                }

                                                /**
                                                 * 화이트리스트에 포함되지 않은 url 인 경우.
                                                 */
                                                if (isConcentrateTime == UsedTimeStatus.CLASS || isConcentrateTime == UsedTimeStatus.NIGHT) {
                                                    /**
                                                     * rule 정보에 의한 수업/심야 시간에도 사용 가능한 허용리스트 인가?
                                                     */

                                                    /**
                                                     * rule의 허용된 항목이 차단정보 전달
                                                     */
                                                    val deviceId = app.getDeviceId()
                                                    val schoolId = app.getSchoolId()
                                                    if (deviceId != null) {
                                                        disposable += apiRepository.blockTime(
                                                            deviceId,
                                                            schoolId,
                                                            url
                                                        )
                                                            .subscribeOn(Schedulers.io())
                                                            .observeOn(AndroidSchedulers.mainThread())
                                                            .subscribe({
                                                                performHistoryBack()
                                                                performHistoryBack(200)
                                                                launchBlockScreen(url)
                                                            }, {})
                                                    }
                                                } else {

                                                    if (resultUrlDetections.containsKey(url)) {
                                                        /**
                                                         * 입력된 url의 결과가 맵에 있으면 바로 사용한다.
                                                         */
                                                        if (resultUrlDetections[url] == true) {
                                                            performHistoryBack()
                                                            performHistoryBack(200)
                                                            launchBlockScreen(url)
                                                        }
                                                        Logger.d(
                                                            "resultUrlDetections $url, isblock ${
                                                                resultUrlDetections.getValue(
                                                                    url
                                                                )
                                                            }"
                                                        )

                                                    } else {
                                                        /**
                                                         * url 정보로 DB조회.
                                                         */
                                                        checkBlockCache(url) {
                                                            /**
                                                             * cache에 없으면 api출 호출
                                                             */
                                                            checkBlockUrlApi(
                                                                url,
                                                                targetPackageName
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                        }
                    }
                    parentNodeInfo.recycle()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterrupt() {

    }

    fun getActionBarTitle(nodeInfo: AccessibilityNodeInfo) : String {
        try {
            if (nodeInfo.childCount > 0) {
                val firstNode = nodeInfo.getChild(0)
                for (i in 0 until firstNode.childCount) {
                    if (firstNode.getChild(i).className.equals(TextView::class.qualifiedName)) {
                        if (firstNode.getChild(i).text != null) {
                            return firstNode.getChild(i).text.toString()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    private fun checkBlockPackageApi(targetPackageName: String) {

        try {
            app.getDeviceId()?.let { deviceId ->

                val schoolId = app.getSchoolId()
                disposable += apiRepository.checkApp(deviceId, schoolId, targetPackageName)
                    .subscribeOn(Schedulers.io())
                    .subscribe({
                        disposable += dbRepository.insertApiCache(
                            ApiCacheVO(
                                info = targetPackageName,
                                type = 4,
                                block = it.data.block,
                                updateTime = System.currentTimeMillis()
                            )
                        )
                            .subscribeOn(Schedulers.io())
                            .subscribe {
                                Logger.d("[checkBlockPackageApi] : ${targetPackageName} added block = ${it.data.block}")
                            }

                        /**
                         * 메모리 캐시
                         */
                        resultPackageDetections[targetPackageName] = it.data.isBlockApp()

                        if (it.data.isBlockApp()) {
                            performHistoryBack()

                            val appName =
                                MomiUtils.getAppName(applicationContext, targetPackageName)
                                    ?: targetPackageName
                            launchBlockScreen(appName.toString())
                            Logger.d("[checkBlockPackageApi] : ${targetPackageName} has blocked")
                        } else {
                            Logger.d("[checkBlockPackageApi] : ${targetPackageName} has not blocked")
                        }
                    }, {
                        it.printStackTrace()
                    })
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun checkBlockCache(info: String, title: String = info, listener: () -> Unit) {

        try {
            disposable += dbRepository.getApiCache(info)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if(it.block == 1) {
                        performHistoryBack()
                        performHistoryBack(200)

                        launchBlockScreen(title)
                        Logger.d("[checkBlockCache] : info = ${it.info}, type = ${it.type} has blocked")
                    }

                    if(info == title) {
                        resultUrlDetections[info] = it.block == 1
                    } else {
                        resultPackageDetections[info] = it.block == 1
                    }

                }, {
                    listener.invoke()
                })

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun checkBlockUrlApi(url: String, targetPackageName: String) {

        try {

            app.getDeviceId()?.let {deviceId->

                val schoolId = app.getSchoolId()

                UrlCheck.getUrlCheck(url)?.let {
                    disposable += apiRepository.checkUrl(deviceId, schoolId, it)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({

                            disposable += dbRepository.insertApiCache(ApiCacheVO(info = url, type = 0, block = it.data.block, updateTime = System.currentTimeMillis()))
                                .subscribeOn(Schedulers.io())
                                .subscribe {
                                    Logger.d("[checkBlockUrlApi] : ${it} added block = ${it.data.block}")
                                }

                            resultUrlDetections[url] = it.data.isBlockApp()

                            if (it.data.isBlockApp()) {
                                performHistoryBack()
                                performHistoryBack(200)

                                launchBlockScreen(url)
                                Logger.d("[checkBlockUrlApi] : ${url} has blocked")
                            } else {
                                Logger.d("[checkBlockUrlApi] : ${url} has not blocked")
                            }
                        }, {
                            it.printStackTrace()
                        })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun performHistoryBack(delay: Long = 0) {
        if(delay>0) {
            GlobalScope.launch {
                delay(delay)
                performGlobalAction(GLOBAL_ACTION_BACK)
            }
        } else {
            performGlobalAction(GLOBAL_ACTION_BACK)
        }

    }

    override fun onDestroy() {
        disposable.clear()
        stopObserve()

        super.onDestroy()
    }

    fun showAlertToast() {
        try {
            if (System.currentTimeMillis() > alertToastTime + 2000) {
                alertToastTime = System.currentTimeMillis()
                Toast.makeText(
                    applicationContext,
                    R.string.protected_app_setting,
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun launchBlockScreen(why: String) {
        blackScreenSubject.onNext(why)
    }

    fun accessNotAllowedVideo(path: String) {
        performHistoryBack()
        launchBlockScreen(path)
    }

}