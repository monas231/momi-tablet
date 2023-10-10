package kr.co.jness.momi.eclean.presentation.license

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_license.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseActivity
import kr.co.jness.momi.eclean.common.EcleanApplication
import kr.co.jness.momi.eclean.databinding.ActivityLicenseBinding
import kr.co.jness.momi.eclean.presentation.dialog.TrialCallback
import kr.co.jness.momi.eclean.presentation.dialog.TrialDescriptionDialog
import kr.co.jness.momi.eclean.presentation.main.MainActivity
import kr.co.jness.momi.eclean.utils.DeviceInfo
import kr.co.jness.momi.eclean.utils.PreferenceUtils
import kr.co.jness.momi.eclean.utils.Value
import kr.co.jness.momi.eclean.viewmodel.LicenseViewModel
import kr.co.jness.momi.eclean.viewmodel.MainViewModel
import javax.inject.Inject

@AndroidEntryPoint
class LicenseActivity: BaseActivity() {

    private val mViewModel: LicenseViewModel by viewModels()
    lateinit var binding: ActivityLicenseBinding
    @Inject
    lateinit var app: EcleanApplication

    val trialPopup by lazy {
        TrialDescriptionDialog()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_license)

        mViewModel.license.observe(this) {

            if(trialPopup.isAdded) {
                trialPopup.dismiss()
            }
            hideProgress()

            app.resetSchoolId()

            startActivity(Intent(this@LicenseActivity, MainActivity::class.java))
            finish()
        }

        mViewModel.errorMsg.observe(this) {
            hideProgress()

            Toast.makeText(this@LicenseActivity, it, Toast.LENGTH_SHORT).show()
        }

        mViewModel.apiResult.observe(this) {

            hideProgress()

            when (it) {
                1->{
                    // 라이선스 정상
                    mViewModel.errorMsg.value = getString(R.string.registered)
                }
                7 -> {
                    mViewModel.errorMsg.value = getString(R.string.invalid_license)
                }
                8 -> {
                    mViewModel.errorMsg.value = getString(R.string.already_used)
                }
                else -> {
                    mViewModel.errorMsg.value = getString(R.string.invalid_license)
                }
            }
        }

        binding.btnConfirm.setOnClickListener {
            checkLicense(etLicense.text.toString())
        }

        binding.btnTrial.setOnClickListener {
            trialPopup.set(object: TrialCallback {
                override fun callbackCall(
                    license: String,
                    s_id: Long,
                    s_nm: String,
                    mail: String,
                    phone: String
                ) {
                    checkLicense(license, s_id, s_nm, mail, phone)
                }

            }).show(supportFragmentManager, "trial_popup")
        }

        binding.tvDeviceUUID.text = getString(R.string.serial_number, DeviceInfo.getDeviceUniqueId())

        mViewModel.notifyInstallApp()

    }

    fun checkLicense(license: String, s_id: Long?=null, s_nm: String?=null, mail: String?=null, phone: String?=null) {
        if (!license.isNullOrEmpty()) {
            mViewModel.checkLicense(this@LicenseActivity, license, s_id, s_nm, mail, phone)
            showProgress()
        } else {
            Toast.makeText(
                this@LicenseActivity,
                R.string.plz_insert_license,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroy() {

        super.onDestroy()
    }

}