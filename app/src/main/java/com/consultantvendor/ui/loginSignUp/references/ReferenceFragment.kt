package com.consultantvendor.ui.loginSignUp.references

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.consultantvendor.R
import com.consultantvendor.data.models.requests.SaveAddress
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.network.ApisRespHandler
import com.consultantvendor.data.network.responseUtil.Status
import com.consultantvendor.data.repos.UserRepository
import com.consultantvendor.databinding.FragmentReferenceBinding
import com.consultantvendor.ui.AppVersionViewModel
import com.consultantvendor.ui.chat.UploadFileViewModel
import com.consultantvendor.ui.drawermenu.classes.ClassesViewModel
import com.consultantvendor.ui.loginSignUp.LoginViewModel
import com.consultantvendor.ui.loginSignUp.service.ServiceFragment.Companion.FILTER_DATA
import com.consultantvendor.utils.*
import com.consultantvendor.utils.PermissionUtils
import com.consultantvendor.utils.dialogs.ProgressDialog
import com.consultantvendor.utils.dialogs.ProgressDialogImage
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_register.*
import kotlinx.android.synthetic.main.fragment_register.view.*
import permissions.dispatcher.*
import java.io.File
import javax.inject.Inject


class ReferenceFragment : DaggerFragment(), OnDateSelected {

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentReferenceBinding

    private var rootView: View? = null

    private lateinit var viewModel: LoginViewModel

    private lateinit var viewModelAppVersion: AppVersionViewModel

    private lateinit var viewModelFilter: ClassesViewModel

    private lateinit var viewModelUpload: UploadFileViewModel

    private lateinit var progressDialog: ProgressDialog

    private lateinit var progressDialogImage: ProgressDialogImage

    private var userData: UserData? = null

    private var fileToUpload: File? = null

    private var hashMap = HashMap<String, Any>()

    private var qualification = ""

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        if (rootView == null) {
            binding = DataBindingUtil.inflate(inflater, R.layout.fragment_reference, container, false)
            rootView = binding.root


            initialise()
            listeners()
            bindObservers()

        }

        return rootView
    }

    private fun initialise() {
        viewModelAppVersion = ViewModelProvider(this, viewModelFactory)[AppVersionViewModel::class.java]
        viewModel = ViewModelProvider(this, viewModelFactory)[LoginViewModel::class.java]
        progressDialog = ProgressDialog(requireActivity())

        //editTextScroll(binding.etBio)
        //binding.cvQualification.gone()
    }

    private fun listeners() {
        binding.toolbar.setNavigationOnClickListener {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0)
                requireActivity().supportFragmentManager.popBackStack()
            else
                requireActivity().finish()
        }

        binding.etStartDateRefer1.setOnClickListener {
            binding.etStartDateRefer1.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, false, true)
        }
        binding.etStartDateRefer2.setOnClickListener {
            binding.etStartDateRefer2.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, false, true)
        }
        binding.etStartDateRefer3.setOnClickListener {
            binding.etStartDateRefer3.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, false, true)
        }
        binding.etEndDateRefer1.setOnClickListener {
            binding.etEndDateRefer1.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, false, true)
        }
        binding.etEndDateRefer2.setOnClickListener {
            binding.etEndDateRefer2.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, false, true)
        }
        binding.etEndDateRefer3.setOnClickListener {
            binding.etEndDateRefer3.hideKeyboard()
            DateUtils.openDatePicker(requireActivity(), this, false, true)
        }

        /*  binding.etBio.setOnFocusChangeListener { v, hasFocus ->
              if (hasFocus) binding.etBio.hint = getString(R.string.bio)
              else binding.etBio.hint = getString(R.string.bio_des)
          }*/

        binding.tvNext.setOnClickListener {

            when {
                binding.etNameRefer1.text.toString().trim().isEmpty() -> {
                    binding.etNameRefer1.showSnackBar(getString(R.string.enter_company))
                }
                binding.etContactPersonRefer1.text.toString().trim().isEmpty() -> {
                    binding.etContactPersonRefer1.showSnackBar(getString(R.string.enter_contact_pernson))
                }
                (binding.etContactNumberRefer1.visibility == View.VISIBLE &&
                        (binding.etContactNumberRefer1.text.toString().isEmpty() || binding.etContactNumberRefer1.text.toString().length < 6)) -> {
                    binding.etContactNumberRefer1.showSnackBar(getString(R.string.enter_phone_number))
                }
                (binding.etCCompanyEmailRefer1.text.toString().trim().isEmpty()) -> {
                    binding.etCCompanyEmailRefer1.showSnackBar(getString(R.string.enter_email))
                }
                (binding.etCCompanyEmailRefer1.text.toString().trim().isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(
                        binding.etCCompanyEmailRefer1.text.toString().trim()).matches()) -> {
                    binding.etCCompanyEmailRefer1.showSnackBar(getString(R.string.enter_correct_email))
                }
                binding.etJobTypeRefer1.text.toString().trim().isEmpty() -> {
                    binding.etJobTypeRefer1.showSnackBar(getString(R.string.enter_job))
                }
                binding.etStartDateRefer1.text.toString().trim().isEmpty() -> {
                    binding.etStartDateRefer1.showSnackBar(getString(R.string.start_date))
                }
                binding.etEndDateRefer1.text.toString().trim().isEmpty() -> {
                    binding.etStartDateRefer1.showSnackBar(getString(R.string.end_date))
                }
                binding.etNameRefer2.text.toString().trim().isEmpty() -> {
                    binding.etNameRefer2.showSnackBar(getString(R.string.enter_company))
                }
                binding.etContactPersonRefer2.text.toString().trim().isEmpty() -> {
                    binding.etContactPersonRefer2.showSnackBar(getString(R.string.enter_contact_pernson))
                }
                (binding.etContactNumberRefer2.visibility == View.VISIBLE &&
                        (binding.etContactNumberRefer2.text.toString().isEmpty() || binding.etContactNumberRefer2.text.toString().length < 6)) -> {
                    binding.etContactNumberRefer2.showSnackBar(getString(R.string.enter_phone_number))
                }
                (binding.etCCompanyEmailRefer2.text.toString().trim().isEmpty()) -> {
                    binding.etCCompanyEmailRefer2.showSnackBar(getString(R.string.enter_email))
                }
                (binding.etCCompanyEmailRefer2.text.toString().trim().isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(
                        binding.etCCompanyEmailRefer2.text.toString().trim()).matches()) -> {
                    binding.etCCompanyEmailRefer2.showSnackBar(getString(R.string.enter_correct_email))
                }
                binding.etJobTypeRefer2.text.toString().trim().isEmpty() -> {
                    binding.etJobTypeRefer2.showSnackBar(getString(R.string.enter_job))
                }
                binding.etStartDateRefer2.text.toString().trim().isEmpty() -> {
                    binding.etStartDateRefer2.showSnackBar(getString(R.string.start_date))
                }
                binding.etEndDateRefer2.text.toString().trim().isEmpty() -> {
                    binding.etStartDateRefer2.showSnackBar(getString(R.string.end_date))
                }
                binding.etNameRefer3.text.toString().trim().isEmpty() -> {
                    binding.etNameRefer3.showSnackBar(getString(R.string.enter_company))
                }
                binding.etContactPersonRefer3.text.toString().trim().isEmpty() -> {
                    binding.etContactPersonRefer3.showSnackBar(getString(R.string.enter_contact_pernson))
                }
                (binding.etContactNumberRefer3.visibility == View.VISIBLE &&
                        (binding.etContactNumberRefer3.text.toString().isEmpty() || binding.etContactNumberRefer3.text.toString().length < 6)) -> {
                    binding.etContactNumberRefer3.showSnackBar(getString(R.string.enter_phone_number))
                }
                (binding.etCCompanyEmailRefer3.text.toString().trim().isEmpty()) -> {
                    binding.etCCompanyEmailRefer3.showSnackBar(getString(R.string.enter_email))
                }
                (binding.etCCompanyEmailRefer3.text.toString().trim().isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(
                        binding.etCCompanyEmailRefer3.text.toString().trim()).matches()) -> {
                    binding.etCCompanyEmailRefer3.showSnackBar(getString(R.string.enter_correct_email))
                }
                binding.etJobTypeRefer3.text.toString().trim().isEmpty() -> {
                    binding.etJobTypeRefer3.showSnackBar(getString(R.string.enter_job))
                }
                binding.etStartDateRefer3.text.toString().trim().isEmpty() -> {
                    binding.etStartDateRefer3.showSnackBar(getString(R.string.start_date))
                }
                binding.etEndDateRefer3.text.toString().trim().isEmpty() -> {
                    binding.etStartDateRefer3.showSnackBar(getString(R.string.end_date))
                }
                isConnectedToInternet(requireContext(), true) -> {
                    requireActivity().intent.putExtra(FILTER_DATA, qualification.removeSuffix(","))

                    if (fileToUpload != null && fileToUpload?.exists() == true) {
                        //uploadFileOnServer(fileToUpload)
                    } else {
                        hitApi()
                    }
                }
            }
        }
    }

    private fun hitApi() {
        /*val hashMap = HashMap<String, Any>()

        hashMap["name"] = binding.etName.text.toString().trim()
        hashMap["location_name"] = address?.locationName ?: ""
        hashMap["lat"] = address?.location?.get(1).toString()
        hashMap["long"] = address?.location?.get(0).toString()
        hashMap["bio"] = binding.etBio.text.toString().trim()
        if (binding.etMobileNumber.text.toString().trim().isNotEmpty()) {
            hashMap["country_code"] = binding.ccpCountryCode.selectedCountryCodeWithPlus
            hashMap["phone"] = binding.etMobileNumber.text.toString()
        }
        if (binding.etEmail.text.toString().trim().isNotEmpty())
            hashMap["email"] = binding.etEmail.text.toString()*/


        /*Update profile or register*/
        when {
            arguments?.containsKey(UPDATE_NUMBER) == true -> {
                viewModel.updateProfile(hashMap)
            }
            arguments?.containsKey(UPDATE_PROFILE) == true -> {
                viewModel.updateProfile(hashMap)
            }
            else -> {
                hashMap["user_type"] = APP_TYPE
                viewModel.register(hashMap)
            }
        }
    }

    private fun bindObservers() {

        viewModel.updateProfile.observe(requireActivity(), Observer {
            it ?: return@Observer
            when (it.status) {
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(USER_DATA, it.data)

                    requireActivity().setResult(Activity.RESULT_OK)
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

    override fun onDateSelected(date: String) {
        binding.etStartDateRefer1.setText(date)
    }
}
