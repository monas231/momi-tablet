package kr.co.jness.momi.eclean.presentation.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.DialogCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.INDICATOR_GRAVITY_TOP
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_trial_desc.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseDialogFragment
import kr.co.jness.momi.eclean.databinding.DialogQuestionBinding
import kr.co.jness.momi.eclean.databinding.DialogTrialDescBinding
import kr.co.jness.momi.eclean.viewmodel.LicenseViewModel
import kr.co.jness.momi.eclean.viewmodel.QuestionViewModel
import kr.co.jness.momi.eclean.viewmodel.TrialViewModel

@AndroidEntryPoint
class QuestionDialog : BaseDialogFragment() {

    lateinit var binding: DialogQuestionBinding
    private val viewModel : QuestionViewModel by viewModels()
    private val activityViewModel: LicenseViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_question,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        binding.ibClose.setOnClickListener {
            dismiss()
        }

        /**
         * 사용자 구분
         */
        binding.spUser.apply {
            val areas = context.resources.getStringArray(R.array.spinner_user)
            this.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, areas)
            this.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.type.value = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        /**
         * 문의항목
         */
        binding.spType.apply {
            val areas = context.resources.getStringArray(R.array.spinner_question_type)
            this.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, areas)
            this.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.q_type.value = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }
        }

        /**
         * Email 구분
         */
        binding.spEmailType.apply {
            val areas = context.resources.getStringArray(R.array.spinner_domain)
            this.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, areas)
            this.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if(position == 0 || position == areas.size-1) {
                        binding.etEmailService.setText("")
                    } else {
                        binding.etEmailService.setText(areas[position])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
        }

        /**
         * 문의하기 버튼 선택
         */
        binding.btnAsk.setOnClickListener {
            activityViewModel.deviceId.value?.let { deviceId ->
                if(viewModel.emailId.value.isNullOrEmpty()
                    || viewModel.domain.value.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), R.string.insert_email, Toast.LENGTH_SHORT).show()
                } else if(
                    viewModel.phone1.value.isNullOrEmpty()
                    || viewModel.phone2.value.isNullOrEmpty()
                    || viewModel.phone3.value.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), R.string.insert_phone, Toast.LENGTH_SHORT).show()
                } else if(viewModel.name.value.isNullOrEmpty()) {
                    Toast.makeText(requireContext(), R.string.insert_name, Toast.LENGTH_SHORT).show()
                } else if(!viewModel.allDataIsSet()) {
                    Toast.makeText(context, R.string.need_all_data, Toast.LENGTH_SHORT).show()
                } else {
                    showProgress()
                    viewModel.sendQuestion(deviceId)
                }
            }
        }

        viewModel.errorMsg.observe(this) {
            if(!it.isNullOrEmpty()) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
            hideProgress()
        }
        viewModel.apiResult.observe(this) {
            hideProgress()
            when (it) {
                1->{
                    Toast.makeText(context, R.string.question_is_sent, Toast.LENGTH_SHORT).show()
                    dismiss()
                }
                else -> {
                    Toast.makeText(context, "error code = $it", Toast.LENGTH_SHORT).show()
                }
            }
        }

        activityViewModel.notifyInstallApp()

        this.isCancelable = false
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}