package com.consultantvendor.ui.drawermenu.classes

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
import com.consultantvendor.data.models.responses.ClassData
import com.consultantvendor.data.models.responses.CommonDataModel
import com.consultantvendor.data.models.responses.JitsiClass
import com.consultantvendor.data.network.ApiKeys.AFTER
import com.consultantvendor.data.network.ApiKeys.PER_PAGE
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.PER_PAGE_LOAD
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.ActivityListingToolbarBinding
import com.consultantvendor.ui.drawermenu.classes.addclass.AddClassFragment
import com.consultantvendor.ui.jitsimeet.JitsiActivity
import com.consultantvendor.utils.*
import com.consultantvendor.utils.dialogs.ProgressDialog
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.item_no_data.view.*
import javax.inject.Inject

class ClassesFragment : DaggerFragment() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: ActivityListingToolbarBinding

    private var rootView: View? = null

    private lateinit var viewModel: ClassesViewModel

    private lateinit var progressDialog: ProgressDialog

    private var items = ArrayList<ClassData>()

    private lateinit var adapter: ClassesAdapter

    private var isLastPage = false

    private var isFirstPage = true

    private var isLoadingMoreItems = false

    private var classSelectedData: ClassData? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.activity_listing_toolbar, container, false)
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
        binding.tvTitle.text = getString(R.string.classes)
        binding.tvAdd.visible()

        viewModel = ViewModelProvider(this, viewModelFactory)[ClassesViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        binding.clNoData.ivNoData.setImageResource(R.drawable.ic_requests_empty_state)
        binding.clNoData.tvNoData.text = getString(R.string.no_classes)
        binding.clNoData.tvNoDataDesc.text = getString(R.string.no_classes_desc)
    }

    private fun setAdapter() {
        adapter = ClassesAdapter(this, items)
        binding.rvListing.adapter = adapter
        binding.rvListing.itemAnimator = null
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.tvAdd.setOnClickListener {
            replaceResultFragment(this, AddClassFragment(), R.id.container, AppRequestCode.ADD_CLASS)
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
    }

    private fun hitApi(firstHit: Boolean) {
        if (firstHit) {
            isFirstPage = true
            isLastPage = false
        }

        val hashMap = HashMap<String, String>()
        if (isConnectedToInternet(requireContext(), true)) {
            if (!isFirstPage && items.isNotEmpty())
                hashMap[AFTER] = items[items.size - 1].id ?: ""

            hashMap[PER_PAGE] = PER_PAGE_LOAD.toString()
            hashMap["type"] = "VENDOR_ADDED"

            viewModel.classesList(hashMap)
        }
    }

    private fun bindObservers() {
        viewModel.classes.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    binding.clLoader.gone()
                    binding.swipeRefresh.isRefreshing = false

                    isLoadingMoreItems = false

                    val tempList = it.data?.classes ?: emptyList()
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

                    binding.swipeRefresh.isRefreshing = false
                    binding.clLoader.gone()
                    ApisRespHandler.handleError(it.error, requireActivity(), prefsManager)
                }
                Status.LOADING -> {
                    if (!isLoadingMoreItems && !binding.swipeRefresh.isRefreshing)
                        binding.clLoader.visible()
                }
            }
        })

        viewModel.classStatus.observe(this, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    startClassAction(it.data)
                    hitApi(true)
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppRequestCode.ADD_CLASS) {
                hitApi(true)
            }
        }
    }

    fun startClass(pos: Int) {
        classSelectedData = items[pos]

        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.start_class,
                R.string.start_class_message, R.string.start_class, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        if (isConnectedToInternet(requireContext(), true)) {
                            val hashMap = HashMap<String, String>()
                            hashMap["class_id"] = classSelectedData?.id ?: ""
                            hashMap["status"] = ClassType.STARTED
                            viewModel.classStatus(hashMap)
                        }
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    fun completeClass(pos: Int) {
        classSelectedData = items[pos]

        AlertDialogUtil.instance.createOkCancelDialog(requireActivity(), R.string.complete_class,
                R.string.complete_class_message, R.string.complete_class, R.string.cancel, false,
                object : AlertDialogUtil.OnOkCancelDialogListener {
                    override fun onOkButtonClicked() {
                        if (isConnectedToInternet(requireContext(), true)) {
                            val hashMap = HashMap<String, String>()
                            hashMap["class_id"] = classSelectedData?.id ?: ""
                            hashMap["status"] = ClassType.COMPLETED
                            viewModel.classStatus(hashMap)
                        }
                    }

                    override fun onCancelButtonClicked() {
                    }
                }).show()
    }

    private fun startClassAction(data: CommonDataModel?) {
        /*Data for jitsi class*/
        val jitsiClass = JitsiClass()
        jitsiClass.id = classSelectedData?.id
        jitsiClass.name = classSelectedData?.name
        jitsiClass.isClass=true

        if (classSelectedData?.status != ClassType.COMPLETED) {
                startActivity(Intent(requireActivity(), JitsiActivity::class.java)
                        .putExtra(EXTRA_CALL_NAME, jitsiClass))
        }
    }

}
