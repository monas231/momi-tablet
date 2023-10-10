package kr.co.jness.momi.eclean.presentation.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_license.*
import kotlinx.android.synthetic.main.dialog_trial_desc.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseDialogFragment
import kr.co.jness.momi.eclean.common.EcleanApplication
import kr.co.jness.momi.eclean.databinding.DialogLicenseBinding
import kr.co.jness.momi.eclean.databinding.DialogPuchaseBinding
import kr.co.jness.momi.eclean.presentation.main.MainActivity
import kr.co.jness.momi.eclean.utils.PreferenceUtils
import kr.co.jness.momi.eclean.utils.Value
import kr.co.jness.momi.eclean.viewmodel.LicenseViewModel
import javax.inject.Inject


@AndroidEntryPoint
class InsertLicenseDialog : BaseDialogFragment() {

    lateinit var binding: DialogLicenseBinding
    private val mViewModel: LicenseViewModel by viewModels()
    @Inject
    lateinit var app: EcleanApplication

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_license,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.ibClose.setOnClickListener {
            dismiss()
        }
        binding.btnBuy.setOnClickListener {
            PurchaseDialog().show(parentFragmentManager, "question_dialog")
        }
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
        binding.btnAuth.setOnClickListener {
            checkLicense(etLicense.text.toString())
        }

        mViewModel.notifyInstallApp()

        mViewModel.license.observe(this) {

            app.resetSchoolId()

            Intent(requireContext(), MainActivity::class.java).apply {
                this.putExtra("changed_license", true)
                startActivity(this)
            }
            requireActivity().finishAffinity()
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

        mViewModel.errorMsg.observe(viewLifecycleOwner) {
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }


    fun checkLicense(license: String, s_id: Long?=null, s_nm: String?=null, mail: String?=null, phone: String?=null) {
        if (!license.isNullOrEmpty()) {
            mViewModel.checkLicense(requireContext(), license, s_id, s_nm, mail, phone)
            showProgress()
        } else {
            Toast.makeText(
                requireContext(),
                R.string.plz_insert_license,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}