package com.consultantvendor.ui.dashboard.wallet

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.consultantvendor.R
import com.consultantvendor.data.models.responses.Wallet
import com.consultantvendor.data.network.ApiKeys.AFTER
import com.consultantvendor.data.network.ApiKeys.PER_PAGE
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.databinding.FragmentWalletBinding
import com.consultantvendor.ui.dashboard.wallet.addmoney.AddMoneyActivity
import com.consultantvendor.ui.drawermenu.DrawerActivity
import com.consultantvendor.ui.drawermenu.DrawerActivity.Companion.PAYOUT
import com.consultantvendor.utils.*
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.item_no_data.view.*
import javax.inject.Inject


class WalletFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory


    private lateinit var binding: FragmentWalletBinding

    private var rootView: View? = null

    private lateinit var viewModel: WalletViewModel

    private var items = ArrayList<Wallet>()

    private lateinit var adapter: WalletAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_wallet, container, false)
            rootView = binding.root

            initialise()
            setAdapter()
            listeners()
            bindObservers()
            hitApi(true)

        }
        return rootView
    }

    private fun initialise() {
        viewModel = ViewModelProvider(this, viewModelFactory)[WalletViewModel::class.java]

        binding.clNoData.ivNoData.setImageResource(R.drawable.ic_wallet_empty)
        binding.clNoData.tvNoData.text = getString(R.string.no_transaction)
        binding.clNoData.tvNoDataDesc.text = getString(R.string.no_transaction_desc)
    }

    private fun setAdapter() {
        adapter = WalletAdapter(items)
        binding.rvListing.adapter = adapter
        binding.rvListing.itemAnimator = null
    }


    private fun listeners() {
        binding.btnPayout.setOnClickListener {

            startActivityForResult(Intent(requireActivity(), DrawerActivity::class.java)
                    .putExtra(PAGE_TO_OPEN, PAYOUT), AppRequestCode.PAYOUT_MONEY)
        }

        binding.tvAddMoney.setOnClickListener {
            disableButton(binding.tvAddMoney)
            startActivityForResult(Intent(requireContext(), AddMoneyActivity::class.java), AppRequestCode.ADD_MONEY)
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.wallet(HashMap())
            hitApi(true)
        }

        binding.rvListing.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = binding.rvListing.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount - 1
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()

                if (!isLoadingMoreItems && !isLastPage && lastVisibleItemPosition >= totalItemCount) {
                    isLoadingMoreItems = true
                    hitApi(false)
                }
            }
        })
    }

    fun hitApi(firstHit: Boolean) {

        if (firstHit) {
            isFirstPage = true
            isLastPage = false
        }

        if (isConnectedToInternet(requireContext(), true)) {
            val hashMap = HashMap<String, String>()
            if (!isFirstPage && items.isNotEmpty())
                hashMap[AFTER] = items[items.size - 1].id ?: ""

            hashMap[PER_PAGE] = PER_PAGE_LOAD.toString()
            viewModel.walletHistory(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.wallet.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.tvAmount.text = getCurrency(it.data?.balance)

                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {

                }
            }
        })

        viewModel.walletHistory.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.clLoader.gone()

                    isLoadingMoreItems = false

                    val tempList = it.data?.payments ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                        items.addAll(tempList)

                        adapter.notifyDataSetChanged()
                    } else {
                        val oldSize = items.size
                        items.addAll(tempList)

                        adapter.notifyItemRangeInserted(oldSize, items.size)
                    }

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.clNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)

                    binding.clLoader.gone()
                    binding.swipeRefreshLayout.isRefreshing = false
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!binding.swipeRefreshLayout.isRefreshing && !isLoadingMoreItems)
                        binding.clLoader.visible()
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.PAYOUT_MONEY) {
                hitApi(true)
            }else if (requestCode == AppRequestCode.ADD_MONEY) {
                requireActivity().runOnUiThread {
                    viewModel.wallet(HashMap())
                    hitApi(true)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.wallet(HashMap())
    }
}