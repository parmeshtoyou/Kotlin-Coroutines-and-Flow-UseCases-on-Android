package com.lukaslechner.coroutineusecasesonandroid.usecases.coroutines.usecase2.callbacks

import androidx.room.util.recursiveFetchLongSparseArray
import com.lukaslechner.coroutineusecasesonandroid.base.BaseViewModel
import com.lukaslechner.coroutineusecasesonandroid.mock.AndroidVersion
import com.lukaslechner.coroutineusecasesonandroid.mock.VersionFeatures
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SequentialNetworkRequestsCallbacksViewModel(
    private val mockApi: CallbackMockApi = mockApi()
) : BaseViewModel<UiState>() {

    private lateinit var getAndroidVersionsCall: Call<List<AndroidVersion>>
    private lateinit var getAndroidFeaturesCall: Call<VersionFeatures>

    fun perform2SequentialNetworkRequest() {
        uiState.value = UiState.Loading

        getAndroidVersionsCall = mockApi.getRecentAndroidVersions()
        getAndroidVersionsCall.enqueue(object : Callback<List<AndroidVersion>> {
            override fun onResponse(
                call: Call<List<AndroidVersion>>,
                response: Response<List<AndroidVersion>>
            ) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        val mostRecentVersion = it.last()
                        getAndroidFeaturesCall =
                            mockApi.getAndroidVersionFeatures(mostRecentVersion.apiLevel)
                        getAndroidFeaturesCall.enqueue(object: Callback<VersionFeatures>{
                            override fun onResponse(
                                call: Call<VersionFeatures>,
                                response: Response<VersionFeatures>
                            ) {
                                if (response.isSuccessful) {
                                    response.body()?.let { features ->
                                        uiState.value = UiState.Success(features)
                                    }
                                } else {
                                    uiState.value = UiState.Error("Network request failed!")
                                }
                            }

                            override fun onFailure(p0: Call<VersionFeatures>, p1: Throwable) {
                                uiState.value = UiState.Error(message = "Network request failed")
                            }
                        })
                    }
                }
            }

            override fun onFailure(p0: Call<List<AndroidVersion>>, p1: Throwable) {
                uiState.value = UiState.Error("Something went Wrong!")
            }

        })
    }

    override fun onCleared() {
        super.onCleared()
        getAndroidVersionsCall.cancel()
        getAndroidFeaturesCall.cancel()
    }
}