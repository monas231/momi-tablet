package kr.co.jness.momi.eclean.presentation.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.*
import kr.co.jness.momi.eclean.BuildConfig
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseActivity
import kr.co.jness.momi.eclean.common.BaseFragment
import kr.co.jness.momi.eclean.common.EcleanApplication
import kr.co.jness.momi.eclean.databinding.FragmentMainBinding
import kr.co.jness.momi.eclean.extension.debounceClick
import kr.co.jness.momi.eclean.extension.dpToPx
import kr.co.jness.momi.eclean.model.RuleInfoManager
import kr.co.jness.momi.eclean.presentation.block.BlockActivity
import kr.co.jness.momi.eclean.presentation.dialog.ConfirmDialog
import kr.co.jness.momi.eclean.presentation.dialog.InsertLicenseDialog
import kr.co.jness.momi.eclean.presentation.license.LicenseActivity
import kr.co.jness.momi.eclean.presentation.web.WebViewActivity
import kr.co.jness.momi.eclean.service.UrlInterceptorService
import kr.co.jness.momi.eclean.utils.*
import kr.co.jness.momi.eclean.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MainFragment : BaseFragment() {

    private lateinit var binding: FragmentMainBinding
    private val mViewModel: MainViewModel by activityViewModels()
    private lateinit var messagingAdapter: CurrentMessagingAdapter
    private lateinit var allowedAdapter: AllowedAppsAdapter
    @Inject
    lateinit var app: EcleanApplication
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == BlockActivity.ACTION_UPDATE_BLOCK_COUNT) {
                setBlockHistory()
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = mViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initObservers()
        registerReceivers()

        if(app.getDeviceId() != null) {
            mViewModel.getNotice(app.getDeviceId()!!, app.getSchoolId())
        } else {
            mViewModel.notifyInstallApp()
        }

        binding.clTopNotice.setOnClickListener {

            var intent = Intent(context, WebViewActivity::class.java).apply {
                putExtra("url", "${BuildConfig.SERVER_URL}WebService/notice.jns?school_id=${app.getSchoolId()}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()

        checkbox.isChecked = UrlInterceptorService.instance?.executeBlock ?: false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterReceivers()
    }

    private fun registerReceivers() {
        IntentFilter(BlockActivity.ACTION_UPDATE_BLOCK_COUNT).also {
            requireContext().registerReceiver(receiver, it)
        }
    }

    private fun unregisterReceivers() {
        requireContext().unregisterReceiver(receiver)
    }

    private fun initUI() {
        /**
         * 버전 표시
         */
        binding.tvVersionName.text = getString(R.string.main_01, BuildConfig.VERSION_NAME)
        /**
         * 기기 라이선스 표시
         */
        PreferenceUtils.getString(requireContext(), Value.LICENSE.type)?.let {
            if(it == Constant.DEMO_LICENSE) {
                mViewModel.isDemo.value = (it == Constant.DEMO_LICENSE)
            } else {
                binding.tvLicense.text = it
            }
        }

        /**
         * 데모라이선스의 만료일 표시
         */
        PreferenceUtils.getString(requireContext(), Value.EXPIRARION_DATE.type)?.let {
            try {
                val dateformat = SimpleDateFormat("yyyyMMdd").parse(it)
                binding.tvExpiration.text = SimpleDateFormat(getString(R.string.expiration)).format(dateformat)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        /**
         * 수업/자유/심야 시간 표시
         */
        messagingAdapter = CurrentMessagingAdapter(childFragmentManager, lifecycle)
        binding.pager.adapter = messagingAdapter
        binding.pager.isUserInputEnabled = false

        allowedAdapter = AllowedAppsAdapter()
        binding.rvAllowed.adapter = allowedAdapter
        binding.rvAllowed.addItemDecoration(object : DividerItemDecoration(
            requireContext(),
            LinearLayoutManager.VERTICAL
        ) {

        })
        binding.rvAllowed.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.top = 7.dpToPx
                outRect.bottom = 7.dpToPx
            }
        })

        /**
         * 더보기 중복클릭 방지
         */
        binding.btnMore.debounceClick {
            binding.btnMore.isVisible = allowedAdapter.showNextItems()
        }

        binding.tvDeviceUUID.text = getString(R.string.serial_number, DeviceInfo.getDeviceUniqueId())

        checkbox.setOnCheckedChangeListener { compoundButton, b ->
            UrlInterceptorService.instance?.executeBlock = b
        }

        /**
         * 체험판 종료
         */
        binding.btnFinishTrial.setOnClickListener {

            ConfirmDialog{
                app.stopMonitoring()
                requireActivity().finishAffinity()
                startActivity(Intent(requireActivity(), LicenseActivity::class.java))
            }.show(childFragmentManager, "finish_trial")
        }

        /**
         * 유료라이선스 전환
         */
        binding.btnChangeLicense.setOnClickListener {
            InsertLicenseDialog().show(childFragmentManager, "license_dialog")
        }

        mViewModel.errorMsg.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }

        mViewModel.apiResult.observe(viewLifecycleOwner) {

            (activity as BaseActivity ).hideProgress()

            when (it) {
                1->{
                    // 라이선스 정상
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

        setBlockHistory()
    }

    /**
     * 차단 내역
     */
    private fun setBlockHistory() {
        binding.tvBlockDate.text = SimpleDateFormat("yyyy'.'MM").format(Date())
        val key = MomiUtils.getBlockCountKey()
        var count = PreferenceUtils.getInt(requireContext(), key)
        if (count < 1) count = 0
        binding.tvBlockCount.text = count.toString()
    }

    /**
     * Rule DB에 boserver를 등록하여,
     * 업데이트 시, 화면을 자동 갱신한다.
     */
    private fun initObservers() {
        mViewModel.ruleInfo.observe(viewLifecycleOwner, Observer { info ->
            GlobalScope.launch {
                val mJob = async(Dispatchers.Default) {
                    mViewModel.ruleInfoManager = info?.let {
                        RuleInfoManager(
                            it,
                            MomiUtils.getTodayOfWeek()
                        )
                    }
                }
                mJob.await()
                withContext(Dispatchers.Main) {
                    mViewModel.ruleInfoManager?.let {
                        binding.pager.currentItem = it.getCurrentTimeStatus().position
                        setSettingTime(it)
                        setAllowedApps(it)
                    }
                }
            }
        })
    }

    /**
     * 허용된 앱 표시
     */
    private fun setAllowedApps(info: RuleInfoManager) {
        if (info.blockApp || info.blockSchoolApp) {
            binding.clBottom2.isVisible = true
            allowedAdapter.setItems(info.allowedApps)
            binding.btnMore.isVisible = !allowedAdapter.equalToMaxPage()

        } else {
            binding.clBottom2.isVisible = false
        }
    }

    /**
     * 수업 시간 및 설정시간 표시
     */
    private fun setSettingTime(info: RuleInfoManager) {
        if (info.useClassTime) {
            binding.bottom1Left.isVisible = true
            buildString {
                info.orderedClassTimes
                    .filterValues {
                        it.first().day in 1..7
                    }
                    .forEach { (day, classTimes) ->
                        // 요일을 제일 위에 표시.
                        val displayName = getNameByDayOfWeek(day)
                        append("$displayName\n")

                        classTimes.sortedBy { it.start }
                            .forEachIndexed { index, times ->
                                val startHour = times.start.substring(0, 2)
                                val startMinute = times.start.substring(2, 4)
                                val endHour = times.end.substring(0, 2)
                                val endMinute = times.end.substring(2, 4)

                                // 현재 요일에 해당하는 설정 시간을 순서대로 표시한다.
                                val timeStr = "$startHour:$startMinute ~ $endHour:$endMinute"
                                append(timeStr)
                                append("\n")
                                if (index == classTimes.lastIndex) append("\n")
                            }
                    }
            }.also {
                // 마지막에 붙은 \n 은 지워준다.
                binding.tvClassTimesDesc.text = it.trimEnd()
            }
        } else {
            binding.bottom1Left.isVisible = false
        }

        binding.bottom1Right.isVisible = info.useNight
        binding.clBottom1.isVisible = info.useClassTime || info.useNight
    }

    /**
     * 특정 요일을 문자로 가져온다. 서버에서는 1 이 일요일.
     */
    private fun getNameByDayOfWeek(dayOfWeek: Int) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val transformed = if (dayOfWeek == 1) 7 else dayOfWeek - 1
        DayOfWeek.of(transformed).getDisplayName(TextStyle.SHORT, Locale.getDefault())
    } else {
        when(dayOfWeek) {
            1 -> "일"
            2 -> "월"
            3 -> "화"
            4 -> "수"
            5 -> "목"
            6 -> "금"
            7 -> "토"
            else -> ""
        }
    }

}