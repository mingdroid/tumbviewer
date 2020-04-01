package com.nutrition.express.ui.reblog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nutrition.express.model.api.repo.ReblogRepo
import java.util.*

class ReblogViewModel : ViewModel() {
    private val reblogRepo = ReblogRepo(viewModelScope.coroutineContext)
    private val _reblogData = MutableLiveData<ReblogRequest>()
    val reblogResult = _reblogData.switchMap {
        val hashMap = HashMap<String, String>(4)
        hashMap["type"] = it.blogType
        hashMap["id"] = it.blogId
        hashMap["reblog_key"] = it.blogkey
        if (!it.comment.isNullOrEmpty()) {
            hashMap["comment"] = it.comment
        }
        reblogRepo.reblogPost(it.blogName, hashMap)
    }

    fun reblog(blogName: String, blogId: String, blogkey: String, blogType: String, comment: String?) {
        _reblogData.value = ReblogRequest(blogName, blogId, blogkey, blogType, comment)
    }

    data class ReblogRequest(val blogName: String, val blogId: String, val blogkey: String,
                             val blogType: String, val comment: String?)
}