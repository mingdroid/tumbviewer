package com.nutrition.express.ui.post.tagged

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nutrition.express.model.api.Status
import com.nutrition.express.model.api.repo.TaggedRepo

class TaggedViewModel : ViewModel() {
    private val taggedRepo = TaggedRepo(viewModelScope.coroutineContext)
    private val filter = "html"
    private val limit = 20

    private val _postData = MutableLiveData<TaggedRequest>()
    val postData = _postData.switchMap {
        taggedRepo.getTaggedPosts(it.tag, null, it.featuredTimestamp, limit)
    }

    fun retryIfFailed() {
        if (postData.value?.status == Status.ERROR) {
            _postData.value = _postData.value
        }
    }

    fun setTag(tag: String) {
        _postData.value = TaggedRequest(tag, null)
    }

    fun getNextPage(featuredTimestamp: Long?) {
        _postData.value?.let { _postData.value = TaggedRequest(it.tag, featuredTimestamp) }
    }

    data class TaggedRequest(val tag: String, val featuredTimestamp: Long?);

}