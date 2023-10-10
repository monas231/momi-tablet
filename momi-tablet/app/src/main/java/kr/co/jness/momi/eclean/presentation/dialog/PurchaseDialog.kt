package kr.co.jness.momi.eclean.presentation.dialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_trial_desc.*
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.common.BaseDialogFragment
import kr.co.jness.momi.eclean.databinding.DialogPuchaseBinding


@AndroidEntryPoint
class PurchaseDialog : BaseDialogFragment() {

    lateinit var binding: DialogPuchaseBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.dialog_puchase,
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
        binding.btnCustomerCenter.setOnClickListener {
            QuestionDialog().show(parentFragmentManager, "question_dialog")
        }
        binding.btnMall.setOnClickListener {
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.g2b.go.kr:8092")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            startActivity(browserIntent)
        }
    }

    override fun onResume() {
        super.onResume()

        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}