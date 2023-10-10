package kr.co.jness.momi.eclean.presentation.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import kr.co.jness.momi.eclean.R
import kr.co.jness.momi.eclean.databinding.ItemAllowedAppBinding
import kr.co.jness.momi.eclean.model.Os

class AllowedAppsAdapter : RecyclerView.Adapter<AllowedAppsAdapter.ViewHolder>() {

    companion object {
        const val ITEM_SIZE_PER_PAGE = 8
    }

    private var items = listOf<Os>()
    private var currentPage = 1
    private var maxPage = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val mBinding = DataBindingUtil.inflate<ItemAllowedAppBinding>(
            inflater,
            R.layout.item_allowed_app,
            parent,
            false
        )
        return ViewHolder(mBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, items[position])
    }

    override fun getItemCount(): Int {
        return when {
            currentPage < maxPage -> currentPage * ITEM_SIZE_PER_PAGE
            else -> items.size
        }
    }

    fun setItems(items: List<Os>) {
        this.items = items
        val divide = items.size / ITEM_SIZE_PER_PAGE
        val remain = items.size % ITEM_SIZE_PER_PAGE

        currentPage = 1
        maxPage = if (divide > 0 && remain > 0) divide + 1 else if (divide > 0 && remain == 0) divide else 1

        notifyDataSetChanged()
    }

    /**
     * 최대 페이지에 도달하지 않았다면 true.
     */
    fun showNextItems(): Boolean {
        if (currentPage < maxPage) {
            currentPage += 1
            notifyDataSetChanged()
        }

        return currentPage < maxPage
    }

    fun equalToMaxPage() = currentPage == maxPage

    class ViewHolder(private val binding: ItemAllowedAppBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, item: Os) {
            when(item.icon?.toInt()) {
                1 -> {
                    binding.btnType.setBackgroundResource(R.drawable.round_apple_14)
                    binding.btnType.text = item.getIconName()
                }
                2 -> {
                    binding.btnType.setBackgroundResource(R.drawable.round_yellowish_orange_14)
                    binding.btnType.text = item.getIconName()
                }
                3 -> {
                    binding.btnType.setBackgroundResource(R.drawable.round_dusk_blue_14)
                    binding.btnType.text = item.getIconName()
                }
                4 -> {
                    binding.btnType.setBackgroundResource(R.drawable.round_cool_grey_14)
                    binding.btnType.text = item.getIconName()
                }
            }
            binding.tvName.text = item.name
            binding.tvDesc.text = item.desc

            binding.executePendingBindings()
        }

    }

}