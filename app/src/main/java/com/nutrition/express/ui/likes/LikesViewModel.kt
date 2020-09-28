package com.nutrition.express.ui.likes

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nutrition.express.model.api.repo.BlogRepo
import com.nutrition.express.model.api.repo.UserRepo
import java.util.*

class LikesViewModel : ViewModel() {
    private val userRepo: UserRepo by lazy { UserRepo(viewModelScope.coroutineContext) }
    private val blogRepo: BlogRepo by lazy { BlogRepo(viewModelScope.coroutineContext) }
    private val _input = MutableLiveData<LikesRequest>()
    val likesPostsData = _input.switchMap {
        if (it.blogName != null) {
            val options = HashMap<String, String>(2)
            options["limit"] = (20).toString()
            options["before"] = it.before.toString()
            blogRepo.getBlogLikes(it.blogName, options)
        } else {
            userRepo.getLikes(20, it.before)
        }
    }

    fun fetchLikesPosts(blogName: String?) {
        _input.value = LikesRequest(blogName, System.currentTimeMillis() / 1000)
    }

    fun fetchNextPage(before: Long) {
        _input.value?.let { _input.value = LikesRequest(it.blogName, before) }
    }

    data class LikesRequest(val blogName: String?, val before: Long)
}