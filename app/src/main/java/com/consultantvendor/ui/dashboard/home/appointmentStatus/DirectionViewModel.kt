package com.consultantvendor.ui.dashboard.home.appointmentStatus

import androidx.lifecycle.ViewModel
import com.consultantvendor.data.apis.WebService
import com.consultantvendor.data.models.responses.directions.Direction
import com.consultantvendor.data.network.responseUtil.ApiUtils
import com.consultantvendor.data.network.responseUtil.Resource
import com.consultantvendor.di.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import javax.inject.Inject

class DirectionViewModel @Inject constructor(private val webService: WebService) : ViewModel() {

    val directions by lazy { SingleLiveEvent<Resource<Direction>>() }

    fun directions(hashMap: HashMap<String, String>) {
        directions.value = Resource.loading()

        webService.directions(hashMap)
                .enqueue(object : Callback<Direction> {

                    override fun onResponse(call: Call<Direction>,
                                            response: Response<Direction>) {
                        if (response.isSuccessful) {
                            directions.value = Resource.success(response.body())
                        } else {
                            directions.value = Resource.error(
                                    ApiUtils.getError(response.code(),
                                            response.errorBody()?.string()))
                        }
                    }

                    override fun onFailure(call: Call<Direction>, throwable: Throwable) {
                        directions.value = Resource.error(ApiUtils.failure(throwable))
                    }

                })
    }
}