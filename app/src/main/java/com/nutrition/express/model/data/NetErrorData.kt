package com.nutrition.express.model.data

import androidx.lifecycle.MutableLiveData

object NetErrorData {
    val apiError401: MutableLiveData<Boolean> = MutableLiveData()
    val apiError429: MutableLiveData<Boolean> = MutableLiveData()

    fun setError401(boolean: Boolean) {
        apiError401.postValue(boolean)
    }

    fun setError429(boolean: Boolean) {
        apiError429.postValue(boolean)
    }
}