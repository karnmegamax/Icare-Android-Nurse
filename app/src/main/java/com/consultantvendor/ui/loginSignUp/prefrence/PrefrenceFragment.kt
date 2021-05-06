package com.consultantvendor.ui.loginSignUp.prefrence

import android.app.Activity
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
import com.consultantvendor.data.models.requests.SetFilter
import com.consultantvendor.data.models.requests.UpdateServices
import com.consultantvendor.data.models.responses.Categories
import com.consultantvendor.data.models.responses.Filter
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentServiceBinding
import com.consultantvendor.ui.drawermenu.classes.ClassesViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment.Companion.FILTER_DATA
import com.consultantvendor.ui.loginSignUp.subcategory.SubCategoryFragment.Companion.CATEGORY_PARENT_ID
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import javax.inject.Inject

class PrefrenceFragment : DaggerFragment() {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentServiceBinding

    private var rootView: View? = null

    private lateinit var viewModel: ClassesViewModel

    private lateinit var viewModelLogin: LoginViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<Filter>()

    private lateinit var adapter: PrefrenceAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var categoryData: Categories? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_service, container, false)
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
        viewModel = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        viewModelLogin = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        binding.tvTitle.text = getString(R.string.set_preferences)
        categoryData = arguments?.getSerializable(CATEGORY_PARENT_ID) as Categories

        if (requireActivity().intent.hasExtra(UPDATE_PREFRENCES))
            binding.tvNext.text = getString(R.string.update)
    }

    private fun setAdapter() {
        adapter = PrefrenceAdapter(this, items)
        binding.rvListing.adapter = adapter
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.swipeRefresh.setOnRefreshListener {
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

        binding.tvNext.setOnClickListener {
            /*Check selected Filter*/
            val filterArray = ArrayList<SetFilter>()

            var setFilter: SetFilter

            items.forEachIndexed { index, filter ->
                setFilter = SetFilter()

                /*Set filter Id*/
                setFilter.filter_id = filter.id
                setFilter.filter_option_ids = ArrayList()

                var selectedOption = false
                filter.options?.forEach {
                    if (it.isSelected) {
                        selectedOption = true

                        setFilter.filter_option_ids?.add(it.id ?: "")
                    }
                }

                if (selectedOption) {
                    filterArray.add(setFilter)
                } else {
                    binding.toolbar.showSnackBar(filter.preference_name ?: "")
                    return@setOnClickListener
                }
            }

            /*If Need to update*/
            if (requireActivity().intent.hasExtra(UPDATE_PREFRENCES)) {
                val updateServices = UpdateServices()
                updateServices.category_id = categoryData?.id

                updateServices.filters = ArrayList()
                updateServices.filters?.addAll(filterArray)

                if (isConnectedToInternet(requireContext(), true)) {
                    viewModelLogin.updateServices(updateServices)
                }
            } else {

                val fragment = ServiceFragment()
                val bundle = Bundle()
                bundle.putSerializable(CATEGORY_PARENT_ID, categoryData)
                bundle.putSerializable(FILTER_DATA, filterArray)

                fragment.arguments = bundle

                replaceFragment(
                        requireActivity().supportFragmentManager,
                        fragment, R.id.container
                )
            }

        }
    }

    private fun hitApi(firstHit: Boolean) {
        if (firstHit) {
            isFirstPage = true
            isLastPage = false
        }

        val hashMap = HashMap<String, String>()
        if (isConnectedToInternet(requireContext(), true)) {

            hashMap["category_id"] = categoryData?.id ?: ""
            if (requireActivity().intent.hasExtra(UPDATE_PREFRENCES))
                hashMap["user_id"] = userRepository.getUser()?.id ?: ""

            viewModel.getFilters(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.getFilters.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    isLoadingMoreItems = false

                    val tempList = it.data?.filters ?: emptyList()
                    if (isFirstPage) {
                        isFirstPage = false
                        items.clear()
                    }

                    items.addAll(tempList)
                    adapter.notifyDataSetChanged()

                    if (items.isNotEmpty())
                        binding.tvNext.visible()

                    isLastPage = tempList.size < PER_PAGE_LOAD
                    adapter.setAllItemsLoaded(isLastPage)

                    binding.tvNoData.hideShowView(items.isEmpty())
                }
                Status.ERROR -> {
                    binding.swipeRefresh.isRefreshing = false
                    isLoadingMoreItems = false
                    adapter.setAllItemsLoaded(true)
                    binding.clLoader.gone()

                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                    binding.tvNext.gone()
                }
            }
        })

        viewModelLogin.updateServices.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    progressDialog.setLoading(true)

                }
            }
        })
    }


    fun clickItem(item: Filter?) {

    }
}
