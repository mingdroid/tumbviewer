package com.nutrition.express.model.api


/**
 * A generic class that holds a value with its loading status.
 * @param <T>
</T> */
private const val empty = ""
data class Resource<out T>(val status: Status, val data: T?, val code: Int = 0, val message: String = empty) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(Status.SUCCESS, data)
        }

        fun <T> error(code: Int, msg: String, data: T?): Resource<T> {
            return Resource(Status.ERROR, data, code, msg)
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(Status.LOADING, data)
        }
    }
}
