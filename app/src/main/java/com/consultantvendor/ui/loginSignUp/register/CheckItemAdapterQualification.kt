package com.consultantvendor.ui.loginSignUp.register

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.FilterOption
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemCheckExperienceBinding
import com.consultantvendor.utils.CustomFields.skillsexperience
import com.consultantvendor.utils.gone
import com.consultantvendor.utils.visible


class CheckItemAdapterQualification(private val multiSelect: Boolean, private val items: ArrayList<FilterOption>, private var context: Context) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    //private lateinit var context: Context
    private var rate = arrayOf(/*"Experience", */"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "20+")

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.rv_item_check_experience, parent, false
                    )
            )
        } else {
            ViewHolderLoader(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.item_paging_loader, parent, false
                    )
            )
        }
    }

    override fun getItemCount(): Int = if (allItemsLoaded) items.size else items.size + 1

    override fun getItemViewType(position: Int) = if (position >= items.size) LOADING else ITEM

    inner class ViewHolder(val binding: RvItemCheckExperienceBinding) :
            RecyclerView.ViewHolder(binding.root) {

        fun bind(item: FilterOption) = with(binding) {
            if (multiSelect) {
                cbName.visible()
                rbName.gone()
            } else {
                cbName.gone()
                rbName.visible()
            }

            cbName.text = item.option_name
            rbName.text = item.option_name
            val aa = ArrayAdapter(context, android.R.layout.simple_spinner_item, rate)
            expSpinner.adapter = aa
            expSpinner.onItemSelectedListener
            items[adapterPosition].skill_exp = expSpinner.selectedItem.toString()
            skillsexperience = expSpinner.selectedItem.toString()
            rbName.isChecked = item.isSelected
            cbName.isChecked = item.isSelected
            rbName.setOnCheckedChangeListener { buttonView, isChecked ->
                if (rbName.isChecked) {
                    expSpinner.visible()
                    expSpinner.isFocusable = true
                    expSpinner.isClickable = true
                } else {
                    expSpinner.gone()
                    expSpinner.isFocusable = false
                    expSpinner.isClickable = false
                }
            }
            if (rbName.isChecked) {
                expSpinner.visible()
                expSpinner.isFocusable = true
                expSpinner.isClickable = true
            } else {
                expSpinner.gone()
                expSpinner.isFocusable = false
                expSpinner.isClickable = false
            }
            clMain.setOnClickListener {
                if (multiSelect) {
                    items[adapterPosition].isSelected = !items[adapterPosition].isSelected
                    notifyDataSetChanged()
                } else {
                    items.forEachIndexed { index, filterOption ->
                        items[index].isSelected = adapterPosition == index
                    }
                    notifyDataSetChanged()
                }
            }

        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }

    /*override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        items[0].skill_exp = rate[position]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }*/
}

