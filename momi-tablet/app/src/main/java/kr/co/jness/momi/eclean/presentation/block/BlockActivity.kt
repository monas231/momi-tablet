package kr.co.jness.momi.eclean.presentation.block

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_prohibit.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseActivity
import kr.co.jness.momi.eclean.databinding.ActivityProhibitBinding
import kr.co.jness.momi.eclean.utils.Logger
import kr.co.jness.momi.eclean.utils.MomiUtils
import kr.co.jness.momi.eclean.utils.PreferenceUtils
import kr.co.jness.momi.eclean.viewmodel.ProhibitViewModel

@AndroidEntryPoint
class BlockActivity : BaseActivity() {

    companion object {
        const val ACTION_UPDATE_BLOCK_COUNT = "kr.co.jness.momi.eclean.UPDATE_BLOCK_COUNT"
        const val TAG_WHY = "why"

        fun show(context: Context, why: String) {
            try {
                Intent(context, BlockActivity::class.java).apply {
                    putExtra(TAG_WHY, why)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    context.startActivity(this)
                }

                Logger.e("BlockActivity show")
            } catch (e: ActivityNotFoundException) {
                // the expected browser is not installed
                Intent(Intent.ACTION_MAIN).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    context.startActivity(this)
                }
            }
        }
    }

    val mViewModel: ProhibitViewModel by viewModels()
    private var message = ""
    private val timeoutSec = 3L

    @SuppressLint("StringFormatInvalid")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding : ActivityProhibitBinding = DataBindingUtil.setContentView(this, R.layout.activity_prohibit)

        mViewModel.timerCount.observe(this, Observer {
            Logger.e("BlockActivity mViewModel.timerCount  = $it")
            if(it>0) {
                tv_dismiss.text = getString(R.string.will_dismiss, it.toInt())
            } else {
                moveToHome()
            }
        })

        tv_why.text = intent.getStringExtra(TAG_WHY)

        tv_dismiss.text = getString(R.string.will_dismiss, timeoutSec)
        mViewModel.startTimer(timeoutSec-1)

        btnClose.setOnClickListener {
            moveToHome()
        }
        increaseBlockCount()

        Logger.e("BlockActivity onCreate")
    }

    override fun finish() {

        mViewModel.disposable.clear()

        super.finish()
        Logger.e("BlockActivity finish")
    }

    override fun onDestroy() {
        super.onDestroy()

        Logger.e("BlockActivity onDestroy")
    }

    override fun onBackPressed() {
        moveToHome()
    }

    fun moveToHome() {

        Logger.e("BlockActivity moveToHome")

        finish()

        Intent(Intent.ACTION_MAIN).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addCategory(Intent.CATEGORY_HOME)
            startActivity(this)
        }
    }

    /**
     * 블락 횟수를 하나 늘린다.
     */
    private fun increaseBlockCount() {
        val key = MomiUtils.getBlockCountKey()
        var count = PreferenceUtils.getInt(this, key)
        if (count < 1) count = 0
        PreferenceUtils.putInt(this, key, ++count)

        Intent().also {
            it.action = ACTION_UPDATE_BLOCK_COUNT
            sendBroadcast(it)
        }
    }
}