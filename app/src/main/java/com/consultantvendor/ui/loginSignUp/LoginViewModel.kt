package com.consultantvendor.ui.loginSignUp

import androidx.lifecycle.ViewModel
import com.consultantvendor.data.apis.WebService
import com.consultantvendor.data.models.requests.UpdateDocument
import com.consultantvendor.data.models.requests.UpdateServices
import com.consultantvendor.data.models.responses.CommonDataModel
import com.consultantvendor.data.models.responses.UserData
import com.consultantvendor.data.network.responseUtil.ApiResponse
import com.consultantvendor.data.network.responseUtil.ApiUtils
import com.consultantvendor.data.network.responseUtil.Resource
import com.consultantvendor.di.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class LoginViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val login by lazy { SingleLiveEvent<Resource<UserData>>() }

    val profile by lazy { SingleLiveEvent<Resource<UserData>>() }

    val updateNumber by lazy { SingleLiveEvent<Resource<UserData>>() }

    val manualAvailable by lazy { SingleLiveEvent<Resource<UserData>>() }

    val register by lazy { SingleLiveEvent<Resource<UserData>>() }

    val forgotPassword by lazy { SingleLiveEvent<Resource<UserData>>() }

    val changePassword by lazy { SingleLiveEvent<Resource<UserData>>() }

    val updateProfile by lazy { SingleLiveEvent<Resource<UserData>>() }

    val logout by lazy { SingleLiveEvent<Resource<UserData>>() }

    val sendSMS by lazy { SingleLiveEvent<Resource<UserData>>() }

    val sendEmailOtp by lazy { SingleLiveEvent<Resource<UserData>>() }

    val emailVerify by lazy { SingleLiveEvent<Resource<UserData>>() }

    val updateServices by lazy { SingleLiveEvent<Resource<UserData>>() }

    val pagesLink by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val additionalDetails by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    val additionalDetailsUpdate by lazy { SingleLiveEvent<Resource<CommonDataModel>>() }

    fun login(hashMap: HashMap<String, Any>) {
        login.value = Resource.loading()

        webService.login(hashMap)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            login.value = Resource.success(response.body()?.data)
                        } else {
                            login.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        login.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun profile() {
        profile.value = Resource.loading()

        webService.profile()
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            profile.value = Resource.success(response.body()?.data)
                        } else {
                            profile.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        profile.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun updateNumber(hashMap: HashMap<String, Any>) {
        updateNumber.value = Resource.loading()

        webService.updateNumber(hashMap)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            updateNumber.value = Resource.success(response.body()?.data)
                        } else {
                            updateNumber.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        updateNumber.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun manualAvailable(hashMap: HashMap<String, Any>) {
        manualAvailable.value = Resource.loading()

        webService.manualAvailable(hashMap)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            manualAvailable.value = Resource.success(response.body()?.data)
                        } else {
                            manualAvailable.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        manualAvailable.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }


    fun register(hashMap: HashMap<String, Any>) {
        register.value = Resource.loading()

        webService.register(hashMap)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            register.value = Resource.success(response.body()?.data)
                        } else {
                            register.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        register.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun forgotPassword(hashMap: HashMap<String, Any>) {
        forgotPassword.value = Resource.loading()

        webService.forgotPassword(hashMap)
            .enqueue(object : Callback<ApiResponse<UserData>> {

                override fun onResponse(call: Call<ApiResponse<UserData>>,
                                        response: Response<ApiResponse<UserData>>) {
                    if (response.isSuccessful) {
                        forgotPassword.value = Resource.success(response.body()?.data)
                    } else {
                        forgotPassword.value = Resource.error(
                            ApiUtils.getError(response.code(),
                                response.errorBody()?.string()))
                    }
                }

                override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                    forgotPassword.value = Resource.error(ApiUtils.failure(throwable))
                }

            })
    }

    fun changePassword(hashMap: HashMap<String, Any>) {
        changePassword.value = Resource.loading()

        webService.changePassword(hashMap)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            changePassword.value = Resource.success(response.body()?.data)
                        } else {
                            changePassword.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        changePassword.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun updateProfile(hashMap: HashMap<String, Any>) {
        updateProfile.value = Resource.loading()

        webService.updateProfile(hashMap)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            updateProfile.value = Resource.success(response.body()?.data)
                        } else {
                            updateProfile.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        updateProfile.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun logout() {
        logout.value = Resource.loading()

        webService.logout()
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            logout.value = Resource.success(response.body()?.data)
                        } else {
                            logout.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        logout.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun sendSms(hashMap: HashMap<String, Any>) {
        sendSMS.value = Resource.loading()

        webService.sendSMS(hashMap)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            sendSMS.value = Resource.success(response.body()?.data)
                        } else {
                            sendSMS.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        sendSMS.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun sendEmailOtp(hashMap: HashMap<String, Any>) {
        sendEmailOtp.value = Resource.loading()

        webService.sendEmailOtp(hashMap)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            sendEmailOtp.value = Resource.success(response.body()?.data)
                        } else {
                            sendEmailOtp.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        sendEmailOtp.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun emailVerify(hashMap: HashMap<String, Any>) {
        emailVerify.value = Resource.loading()

        webService.emailVerify(hashMap)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            emailVerify.value = Resource.success(response.body()?.data)
                        } else {
                            emailVerify.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        emailVerify.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun updateServices(updateServiceModel: UpdateServices) {
        updateServices.value = Resource.loading()

        webService.updateServices(updateServiceModel)
                .enqueue(object : Callback<ApiResponse<UserData>> {

                    override fun onResponse(call: Call<ApiResponse<UserData>>,
                                            response: Response<ApiResponse<UserData>>) {
                        if (response.isSuccessful) {
                            updateServices.value = Resource.success(response.body()?.data)
                        } else {
                            updateServices.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<UserData>>, throwable: Throwable) {
                        updateServices.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun getPages() {
        pagesLink.value = Resource.loading()

        webService.getPages()
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            pagesLink.value = Resource.success(response.body()?.data)
                        } else {
                            pagesLink.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        pagesLink.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }

    fun additionalDetails(hashMap: HashMap<String, String>) {
        additionalDetails.value = Resource.loading()

        webService.additionalDetails(hashMap)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            additionalDetails.value = Resource.success(response.body()?.data)
                        } else {
                            additionalDetails.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        additionalDetails.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }


    fun additionalDetailsUpdate(updateDocument: UpdateDocument) {
        additionalDetailsUpdate.value = Resource.loading()

        webService.additionalDetailsUpdate(updateDocument)
                .enqueue(object : Callback<ApiResponse<CommonDataModel>> {

                    override fun onResponse(call: Call<ApiResponse<CommonDataModel>>,
                                            response: Response<ApiResponse<CommonDataModel>>) {
                        if (response.isSuccessful) {
                            additionalDetailsUpdate.value = Resource.success(response.body()?.data)
                        } else {
                            additionalDetailsUpdate.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<ApiResponse<CommonDataModel>>, throwable: Throwable) {
                        additionalDetailsUpdate.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }
}