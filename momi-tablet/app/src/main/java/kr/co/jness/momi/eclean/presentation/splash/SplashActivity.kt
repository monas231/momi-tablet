package kr.co.jness.momi.eclean.presentation.splash

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseActivity
import kr.co.jness.momi.eclean.common.EcleanApplication
import kr.co.jness.momi.eclean.databinding.ActivitySplashBinding
import kr.co.jness.momi.eclean.presentation.license.LicenseActivity
import kr.co.jness.momi.eclean.presentation.main.MainActivity
import kr.co.jness.momi.eclean.utils.PreferenceUtils
import kr.co.jness.momi.eclean.utils.Value
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity: BaseActivity() {

    /**
     * 스플래시 화면
     */

    @Inject
    lateinit var app: EcleanApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding: ActivitySplashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash)

        CoroutineScope(Dispatchers.Main).launch {
            delay(1000)

            val license = PreferenceUtils.getString(this@SplashActivity, Value.LICENSE.type)
            if(license == null) {
                startActivity(Intent(this@SplashActivity, LicenseActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            }
            finish()
        }
    }



}