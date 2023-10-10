package kr.co.jness.momi.eclean.presentation.main

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class CurrentMessagingAdapter(fm: FragmentManager, lc: Lifecycle) : FragmentStateAdapter(fm, lc) {

    companion object {
        const val PAGE_MAX_COUNT = 3
    }

    override fun getItemCount() = PAGE_MAX_COUNT

    override fun createFragment(position: Int) = CurrentMessagingFragment.newInstance(position)

}