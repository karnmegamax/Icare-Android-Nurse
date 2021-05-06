package com.consultantvendor.ui.drawermenu.notification

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Notification
import com.consultantvendor.data.network.LoadingStatus.ITEM
import com.consultantvendor.data.network.LoadingStatus.LOADING
import com.consultantvendor.databinding.ItemPagingLoaderBinding
import com.consultantvendor.databinding.RvItemNotificationBinding
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.utils.AppRequestCode
import com.consultantvendor.utils.DateUtils.getTimeAgo
import com.consultantvendor.utils.EXTRA_REQUEST_ID
import com.consultantvendor.utils.PAGE_TO_OPEN
import com.consultantvendor.utils.loadImage


class NotificationAdapter(private val fragment: NotificationFragment,private val items: ArrayList<Notification>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var allItemsLoaded = true

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder.itemViewType != LOADING)
            (holder as ViewHolder).bind(items[position])
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM) {
            ViewHolder(
                    DataBindingUtil.inflate(
                            LayoutInflater.from(parent.context),
                            R.layout.rv_item_notification, parent, false
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

    inner class ViewHolder(val binding: RvItemNotificationBinding) :
            RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                when (items[adapterPosition].module) {
                    NotificationType.REQUEST ->
                        fragment.startActivityForResult(Intent(fragment.requireContext(), DrawerActivity::class.java)
                                .putExtra(PAGE_TO_OPEN, DrawerActivity.APPOINTMENT_DETAILS)
                                .putExtra(EXTRA_REQUEST_ID, items[adapterPosition].module_id), AppRequestCode.APPOINTMENT_DETAILS)
                }
            }
        }

        fun bind(item: Notification) = with(binding) {

            tvName.text = item.message

            loadImage(binding.ivPic, item.form_user?.profile_image,
                    R.drawable.ic_profile_placeholder)

            tvCallDuration.text = getTimeAgo(item.created_at)
        }
    }

    inner class ViewHolderLoader(val binding: ItemPagingLoaderBinding) :
            RecyclerView.ViewHolder(binding.root)

    fun setAllItemsLoaded(allLoaded: Boolean) {
        allItemsLoaded = allLoaded
    }
}
