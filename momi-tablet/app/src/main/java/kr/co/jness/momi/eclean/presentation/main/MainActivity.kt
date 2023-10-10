package kr.co.jness.momi.eclean.presentation.main

import android.Manifest
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_license.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.EcleanApplication
import kr.co.jness.momi.eclean.databinding.ActivityMainBinding
import kr.co.jness.momi.eclean.presentation.license.LicenseActivity
import kr.co.jness.momi.eclean.receiver.DeviceAdminReceiver
import kr.co.jness.momi.eclean.service.EcleanService
import kr.co.jness.momi.eclean.service.UrlInterceptorService
import kr.co.jness.momi.eclean.utils.PermissionCheckUtil
import kr.co.jness.momi.eclean.utils.PreferenceUtils
import kr.co.jness.momi.eclean.utils.Value
import kr.co.jness.momi.eclean.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var app: EcleanApplication
    private var mService: Messenger? = null

    private var bound: Boolean = false

    private var isLogout = MutableLiveData<Boolean>()

    private var resumeCheckPermission = false

    private val licenseChanged by lazy {
        intent.getBooleanExtra("changed_license", false)
    }

    /**
     * 기기 등록 및 정책정보 업데이트를 EcleanService에게 맡겨서 처리한다.
     */
    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            mService = Messenger(service)
            bound = true

            /**
             * 기기 등록
             */
            sendMessageToService(EcleanService.MSG_REGISTER_CLIENT)
            /**
             * 정책정보 업데이트
             */
//            sendMessageToService(EcleanService.MSG_UPDATE_RULE)
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
            bound = false
        }
    }
    private val mMessenger by lazy { Messenger(ActivityHandler(this)) }
    private var mDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        /**
         * 필요한 권한을 모두 요청한다.
         */
        checkAppPermission()

        isLogout.observe(this) {
            it?.let {
                if(it) {
                    finishAffinity()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        stopEcleanService()

        if (mDialog?.isShowing == true) {
            mDialog?.dismiss()
            mDialog = null
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    private fun startEcleanService(context: Context) {
        Intent(context, EcleanService::class.java).also {
            startService(it)
            bindService(it, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun stopEcleanService() {
        if (bound) {
            mService?.let {
                val msg = Message.obtain(null, EcleanService.MSG_UNREGISTER_CLIENT)
                msg.replyTo = mMessenger
                it.send(msg)
            }
            unbindService(mConnection)
            bound = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        grantResults.forEach {
            if(it != PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder(this)
                    .setMessage(R.string.need_all_permission)
                    .setPositiveButton(android.R.string.ok
                    ) { _, _ ->
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", packageName, null)
                            startActivity(this)
                        }

                        resumeCheckPermission = true
                    }.show()
                return
            }
        }

        startEcleanService(this)
        checkAllPermission()
    }

    /**
     * 파일 Observer를 위한 권한 획득
     */
    private fun checkAppPermission() {
        requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
    }

    private fun checkAllPermission() {

        if(!checkAdminPermission()) {

        } else if(!checkUsageAccess()) {

        } else if(!checkAccessibilityService()) {

        }
    }

    /**
     *  사용자정보 접근허용
     *  앱 사용 모니터링 권한
     */
    private fun checkUsageAccess() : Boolean {
//        try {
//            val packageManager = packageManager
//            val applicationInfo =
//                packageManager.getApplicationInfo(packageName, 0)
//            val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
//            var mode = 0
//            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
//                mode = appOpsManager.checkOpNoThrow(
//                    AppOpsManager.OPSTR_GET_USAGE_STATS,
//                    applicationInfo.uid, applicationInfo.packageName
//                )
//            }
//            if(mode == AppOpsManager.MODE_ALLOWED) {
//                return true
//            }
//        } catch (e: PackageManager.NameNotFoundException) {
//
//        }

        if(!PermissionCheckUtil.checkUsageAccess(this)) {

            mDialog = AlertDialog.Builder(this)
                .setMessage(R.string.usage_access_permission_desc)
                .setPositiveButton(
                    android.R.string.ok
                ) { dialog, which ->
                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        permissionActivityForResult.launch(this)
                    }
                }.create()
            mDialog?.show()

            return false
        }

        return true
    }

    /**
     * 기기관리자 권한
     * 앱 삭제 방지
     */
    private fun checkAdminPermission() : Boolean {

        val adminName = ComponentName(this, DeviceAdminReceiver::class.java)
        val isAdminActive = PermissionCheckUtil.checkAdminPermission(this)

        if (!isAdminActive) {
            mDialog = AlertDialog.Builder(this)
                .setMessage(R.string.device_admin_permission_desc)
                .setPositiveButton(android.R.string.ok
                ) { dialog, which ->

                    Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName)
                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "안전한 환경을 위해 실행 버튼을 클릭해주세요")
                        permissionActivityForResult.launch(this)
                    }
                }.create()
            mDialog?.show()

            return false
        }
        return true
    }

    /**
     * 접근성으로 현재 표시되고 있는 화면 감지 한다.
     * 웹 페이지 접속 감지
     * 화면 타이틀 감시
     */
    private fun checkAccessibilityService() : Boolean {

//        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
//        val enabledServices: List<AccessibilityServiceInfo> =
//            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
//
//        for (enabledService in enabledServices) {
//            val enabledServiceInfo: ServiceInfo = enabledService.resolveInfo.serviceInfo
//            if (enabledServiceInfo.packageName == packageName
//                && enabledServiceInfo.name == UrlInterceptorService::class.java.name) {
//                return true
//            }
//        }
//
        if(!PermissionCheckUtil.checkAccessibilityService(this)) {

            mDialog = AlertDialog.Builder(this)
                .setMessage(R.string.accessibility_permission_desc)
                .setPositiveButton(
                    android.R.string.ok
                ) { dialog, which ->

                    Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        permissionActivityForResult.launch(this)
                    }
                }.create()
            mDialog?.show()
            return false
        }

        return true
    }

    private val permissionActivityForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        checkAllPermission()
    }

    private fun sendMessageToService(signal: Int) {
        try {
            val msg = Message.obtain(null, signal)
            msg.replyTo = mMessenger
            mService?.send(msg)
        } catch (e: RemoteException) {
        }
    }

    inner class ActivityHandler(context: Context) : Handler(context.mainLooper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                EcleanService.MSG_SET_VALUE -> {

                }
                // 정책정보가 업데이트되면
//                EcleanService.MSG_UPDATE_RULE -> {
//
//                }
                // 정책정보가 업데이트되면
                EcleanService.MSG_LOGOUT -> {
                    isLogout.value = true
                }
                else -> super.handleMessage(msg)
            }
        }
    }

    fun doLogout() {
        finishAffinity()
        startActivity(Intent(this, LicenseActivity::class.java))
    }

    override fun onResume() {
        super.onResume()

        val license = PreferenceUtils.getString(this, Value.LICENSE.type)
        if(license.isNullOrEmpty()) {
            doLogout()
        }

        if(resumeCheckPermission) {
            resumeCheckPermission = false
            checkAppPermission()
        }
    }

}