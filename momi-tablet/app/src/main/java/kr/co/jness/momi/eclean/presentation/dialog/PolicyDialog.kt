package kr.co.jness.momi.eclean.presentation.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.app.DialogCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.INDICATOR_GRAVITY_TOP
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_trial_desc.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseDialogFragment
import kr.co.jness.momi.eclean.databinding.DialogPolicyBinding
import kr.co.jness.momi.eclean.databinding.DialogQuestionBinding
import kr.co.jness.momi.eclean.databinding.DialogTrialDescBinding
import kr.co.jness.momi.eclean.viewmodel.TrialViewModel

@AndroidEntryPoint
class PolicyDialog : BaseDialogFragment() {

    lateinit var binding: DialogPolicyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_policy,
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
        binding.btnConfirm.setOnClickListener {
            dismiss()
        }
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}