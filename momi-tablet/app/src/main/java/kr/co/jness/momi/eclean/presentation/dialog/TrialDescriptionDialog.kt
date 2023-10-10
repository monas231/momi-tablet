package kr.co.jness.momi.eclean.presentation.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_trial_desc.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseDialogFragment
import kr.co.jness.momi.eclean.databinding.DialogTrialDescBinding
import kr.co.jness.momi.eclean.utils.Constant
import kr.co.jness.momi.eclean.viewmodel.LicenseViewModel
import kr.co.jness.momi.eclean.viewmodel.TrialViewModel

interface TrialCallback {
    fun callbackCall(license: String, s_id: Long, s_nm: String, mail: String, phone: String)
}

@AndroidEntryPoint
//class TrialDescriptionDialog(val callback: (String, Long, String, String, String) -> Unit) : BaseDialogFragment() {
class TrialDescriptionDialog : BaseDialogFragment() {

    lateinit var binding: DialogTrialDescBinding
    private val viewModel : TrialViewModel by viewModels()
    private val activityViewModel: LicenseViewModel by activityViewModels()

    var callback:TrialCallback ? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_trial_desc,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        binding.tabSchool.setOnClickListener {
            viewModel.schoolType.value = 0
        }
        binding.tabEtc.setOnClickListener {
            viewModel.schoolType.value = 1
        }

        binding.ibClose.setOnClickListener {
            dismiss()
        }
        binding.btnNext.setOnClickListener {
            viewModel.step.value = 1
        }
        binding.btnStartTrial.setOnClickListener {

            if(!binding.checkAgree.isChecked) {
                Toast.makeText(requireContext(), R.string.need_agreement, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var schoolId = -1L
            var agencyName = ""
            if(viewModel.schoolType.value == 0) {
                if(viewModel.selectedSchool.value != null) {
                    schoolId = viewModel.selectedSchool.value!!.id
                    agencyName = viewModel.selectedSchool.value!!.name
                }
            } else {

                if(!viewModel.agency.value.isNullOrEmpty()
                    && viewModel.area.value != 0) {
                    agencyName = viewModel.agency.value!!
                    schoolId = 0L
                }
            }

            if(schoolId == -1L) {
                Toast.makeText(requireContext(), R.string.no_agency, Toast.LENGTH_SHORT).show()
            } else if(
                viewModel.emailId.value.isNullOrEmpty()
                || viewModel.domain.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.insert_email, Toast.LENGTH_SHORT).show()
            } else if(
                viewModel.phone1.value.isNullOrEmpty()
                || viewModel.phone2.value.isNullOrEmpty()
                || viewModel.phone3.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.insert_phone, Toast.LENGTH_SHORT).show()
            } else if(
                viewModel.name.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.insert_name, Toast.LENGTH_SHORT).show()
            } else {
                callback?.callbackCall(
                    Constant.DEMO_LICENSE,
                    schoolId,
                    agencyName,
                    viewModel.getEmailAddress(),
                    viewModel.getPhoneNumber()
                )
            }
        }
        binding.btnPolicy.setOnClickListener {
            PolicyDialog().show(parentFragmentManager, "policy_popup")
        }
        binding.etSchool.setOnEditorActionListener { v, actionId, event ->
            if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                doSearchSchool()
                true
            } else {
                false
            }
        }
        binding.btnSearch.setOnClickListener {
            if(binding.etSchool.text.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.insert_school_name, Toast.LENGTH_SHORT).show()
            } else {
                doSearchSchool()
            }
        }

        binding.tabSchool.setOnClickListener {
            viewModel.schoolType.value = 0
        }
        binding.tabEtc.setOnClickListener {
            viewModel.schoolType.value = 1
        }

        binding.spArea.apply {
            val areas = context.resources.getStringArray(R.array.spinner_area)
            this.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, areas)
            this.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.area.value = position
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }

            }
        }

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

        viewModel.errorMsg.observe(this) {
            hideProgress()
            if(it.isNotEmpty()) {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }

        /**
         * 학교, 미취학 탭 선택 시.
         */
        viewModel.schoolType.observe(this) {
            binding.tabSchool.isSelected = it==0
            binding.tabEtc.isSelected = it==1
        }
        /**
         * 학교검색
         */
        viewModel.schoolList.observe(this) {
            if(it != null) {
                if (it.isEmpty()) {
                    Toast.makeText(requireContext(), R.string.no_result, Toast.LENGTH_LONG).show()
                } else {
                    val names = it.map { it.name }.toTypedArray()
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.choose_school)
                        .setSingleChoiceItems(
                            names, -1
                        ) { dialog, which ->
                            viewModel.selectedSchool.value = it[which]
                            binding.etSchool.setText(it[which].name)
                            dialog.dismiss()
                            viewModel.schoolList.value = null
                        }.show()
                }
            }
        }

        this.isCancelable = false
    }

    fun doSearchSchool() {
        etSchool.text?.let {
            if(it.isNotEmpty()) {
                activityViewModel.deviceId.value?.let { deviceId ->
                    showProgress()
                    viewModel.fetchSchoolList(deviceId, it.toString())
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    fun set(callback: TrialCallback): TrialDescriptionDialog {
        this.callback = callback
        return this
    }
}