package com.nutrition.express.ui.post.blog

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.nutrition.express.model.api.repo.BlogRepo
import java.util.*

class BlogViewModel : ViewModel() {
    private val blogRepo = BlogRepo(viewModelScope.coroutineContext)
    private val _deletePostData = MutableLiveData<DeleteRequest>()
    val deletePostData = _deletePostData.switchMap { blogRepo.deletePost(it.blogName, it.postId) }
    private val _blogPosts = MutableLiveData<PostsRequest>()
    val blogPostsData = _blogPosts.switchMap {
        val para = HashMap<String, String>()
        para["limit"] = (20).toString()
        para["offset"] = it.offset.toString()
        blogRepo.getBlogPosts(it.blogName, it.type, para)
    }

    fun deletePost(blogName: String, postId: String) {
        _deletePostData.value = DeleteRequest(blogName, postId)
    }

    fun fetchBlogPosts(blogName: String, type: String, offset: Int) {
        _blogPosts.value = PostsRequest(blogName, type, offset)
    }

    data class DeleteRequest(val blogName: String, val postId: String)
    data class PostsRequest(val blogName: String, val type: String, val offset: Int)

}