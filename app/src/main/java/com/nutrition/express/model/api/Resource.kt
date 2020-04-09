package com.nutrition.express.model.api


/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
val InProgress = Resource.Loading(null)
sealed class Resource<out T: Any> {
    data class Success<out T: Any>(val data: T?) : Resource<T>()
    data class Error(val code: Int, val message: String) : Resource<Nothing>()
    data class Loading<out T: Any>(val data: T?) : Resource<T>()
}
