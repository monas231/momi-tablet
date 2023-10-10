package kr.co.jness.momi.eclean.presentation.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.databinding.FragmentCurrentMessagingBinding
import kr.co.jness.momi.eclean.extension.getColorByVersion

class CurrentMessagingFragment : Fragment() {

    companion object {
        private const val POSITION = "position"

        @JvmStatic
        fun newInstance(position: Int) =
            CurrentMessagingFragment().apply {
                arguments = bundleOf(POSITION to position)
            }
    }

    private val currentPosition by lazy { requireArguments().getInt(POSITION) }
    private lateinit var binding: FragmentCurrentMessagingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_current_messaging, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (currentPosition) {
            0 -> {
                /**
                 * 자유시간
                 */
                binding.ivState.setImageResource(R.drawable.img_free)
                binding.tvMessage.setText(R.string.main_current_message_1)
                binding.tvMessage.setBackgroundColor(requireContext().getColorByVersion(R.color.apple))
            }
            1 -> {
                /**
                 * 수업시간
                 */
                binding.ivState.setImageResource(R.drawable.img_study)
                binding.tvMessage.setText(R.string.main_current_message_2)
                binding.tvMessage.setBackgroundColor(requireContext().getColorByVersion(R.color.squash))
            }
            else -> {
                /**
                 * 심야시간
                 */
                binding.ivState.setImageResource(R.drawable.img_night)
                binding.tvMessage.setText(R.string.main_current_message_3)
                binding.tvMessage.setBackgroundColor(requireContext().getColorByVersion(R.color.marine_blue))
            }
        }
    }
}