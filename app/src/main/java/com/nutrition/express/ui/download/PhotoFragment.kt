package com.nutrition.express.ui.download

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.facebook.drawee.backends.pipeline.Fresco
import com.nutrition.express.R
import com.nutrition.express.application.TumbApp.Companion.app
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.common.CommonViewHolder
import com.nutrition.express.databinding.FragmentDownloadPhotoBinding
import com.nutrition.express.databinding.ItemDownloadPhotoBinding
import com.nutrition.express.imageviewer.PhotoViewActivity
import com.nutrition.express.model.data.bean.LocalPhoto
import com.nutrition.express.util.FileUtils.deleteFile
import com.nutrition.express.util.FileUtils.imageDir
import com.nutrition.express.util.FileUtils.publicImageDir
import com.nutrition.express.util.dp2Pixels
import com.nutrition.express.util.getBoolean
import com.nutrition.express.util.putBoolean
import java.io.File

class PhotoFragment : Fragment() {
    private val SHOW_USER_PHOTO = "SUP"
    private val halfWidth = app.width / 2
    private var adapter: CommonRVAdapter? = null

    private var binding: FragmentDownloadPhotoBinding? = null

    private var userPhoto: ArrayList<LocalPhoto> = ArrayList()
    private var allPhoto: ArrayList<LocalPhoto> = ArrayList()
    private var photoList: ArrayList<LocalPhoto> = ArrayList()
    private var showUserPhoto = false

    private var isChoiceState = false
    private var checkedCount = 0
    private var actionMode: ActionMode? = null
    private val callback: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.menuInflater.inflate(R.menu.menu_multi_choice, menu)
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            when (item.itemId) {
                R.id.delete -> {
                    showDeleteDialog()
                    return true
                }
                R.id.select_all -> {
                    checkAllPhotos()
                    adapter?.notifyDataSetChanged()
                    return true
                }
            }
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            finishMultiChoice()
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        showUserPhoto = getBoolean(SHOW_USER_PHOTO, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDownloadPhotoBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Fresco.getImagePipeline().resume()
                } else {
                    Fresco.getImagePipeline().pause()
                }
            }
        })
        if (showUserPhoto) {
            initPhotoDataUser()
        } else {
            initPhotoDataAll()
        }
        val adapter = CommonRVAdapter.adapter {
            addViewType(
                LocalPhoto::class,
                R.layout.item_download_photo,
                this@PhotoFragment::PhotoViewHolder
            )
        }
        adapter.resetData(photoList.toTypedArray(), false)
        binding.recyclerView.adapter = adapter

        this.binding = binding
        this.adapter = adapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_download_photo, menu)
        val menuItem = menu.findItem(R.id.show_user_photo)
        menuItem.isChecked = showUserPhoto
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.show_user_photo) {
            item.isChecked = !item.isChecked
            showUserPhoto = item.isChecked
            if (showUserPhoto) {
                initPhotoDataUser()
            } else {
                initPhotoDataAll()
            }
            adapter?.resetData(photoList.toTypedArray(), false)
            putBoolean(SHOW_USER_PHOTO, showUserPhoto)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initPhotoDataUser() {
        if (userPhoto.isEmpty()) {
            val userPhotoDir = imageDir
            if (userPhotoDir.isDirectory) {
                getPhotoFile(userPhotoDir, userPhoto)
                sortPhotoData(userPhoto)
            }
        }
        photoList = userPhoto
    }

    private fun initPhotoDataAll() {
        if (allPhoto.isEmpty()) {
            allPhoto = ArrayList()
            val publicPhotoDir = publicImageDir
            if (publicPhotoDir.isDirectory) {
                getPhotoFile(publicPhotoDir, allPhoto)
                sortPhotoData(allPhoto)
            }
        }
        photoList = allPhoto
    }

    private fun getPhotoFile(dir: File, list: MutableList<LocalPhoto>) {
        val files = dir.listFiles() ?: return
        var tmp: LocalPhoto
        for (file in files) {
            if (file.isDirectory) {
                getPhotoFile(file, list)
            } else {
                tmp = LocalPhoto(file)
                if (tmp.isValid) {
                    list.add(tmp)
                }
            }
        }
    }

    private fun sortPhotoData(photos: MutableList<LocalPhoto>) {
        photos.sortWith { o1, o2 ->
            val x = o1.file.lastModified()
            val y = o2.file.lastModified()
            if (x < y) 1 else if (x == y) 0 else -1
        }
    }

    private fun startMultiChoice() {
        photoList.forEach { it.isChecked = false }
        actionMode = (activity as DownloadedActivity).startSupportActionMode(callback)
        checkedCount = 0
        isChoiceState = true
    }

    private fun finishMultiChoice() {
        actionMode?.finish()
        actionMode = null
        isChoiceState = false
    }

    private fun onItemChecked(localPhoto: LocalPhoto) {
        localPhoto.isChecked = !localPhoto.isChecked
        if (localPhoto.isChecked) {
            checkedCount++
        } else {
            checkedCount--
        }
        actionMode?.title = checkedCount.toString()
    }

    private fun deleteCheckedPhotos() {
        userPhoto.removeAll {
            if (it.isChecked) {
                deleteFile(it.file)
                return@removeAll true
            }
            return@removeAll false
        }
        allPhoto.removeAll {
            if (it.isChecked) {
                deleteFile(it.file)
                return@removeAll true
            }
            return@removeAll false
        }

        adapter?.resetData(photoList.toTypedArray(), false)
    }

    private fun checkAllPhotos() {
        photoList.forEach { it.isChecked = true }
        checkedCount = photoList.size
        actionMode?.title = checkedCount.toString()
        adapter?.notifyDataSetChanged()
    }

    private fun showDeleteDialog() {
        context?.let {
            AlertDialog.Builder(it).run {
                setPositiveButton(R.string.delete_positive) { _, _ ->
                    deleteCheckedPhotos()
                    finishMultiChoice()
                }
                setNegativeButton(R.string.pic_cancel, null)
                setTitle(R.string.download_photo_delete_title)
                show()
            }
        }
    }

    fun scrollToTop() {
        binding?.recyclerView?.scrollToPosition(0)
    }

    inner class PhotoViewHolder constructor(view: View) : CommonViewHolder<LocalPhoto>(view) {
        private val binding = ItemDownloadPhotoBinding.bind(view)
        private val defaultWidth = halfWidth - dp2Pixels(view.context, 8)
        private lateinit var photo: LocalPhoto

        init {
            binding.photoView.setOnClickListener {
                if (isChoiceState) {
                    onItemChecked(photo)
                    if (photo.isChecked) {
                        binding.checkView.visibility = View.VISIBLE
                    } else {
                        binding.checkView.visibility = View.GONE
                    }
                    return@setOnClickListener
                }
                val intent = Intent(activity, PhotoViewActivity::class.java)
                val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(
                    activity, binding.photoView, photo.uri.path
                )
                intent.putExtra("transition_name", photo.uri.path)
                intent.putExtra("photo_source", photo.uri)
                startActivity(intent, options.toBundle())
            }
            binding.photoView.setOnLongClickListener {
                if (actionMode != null) {
                    return@setOnLongClickListener true
                }
                startMultiChoice()
                onItemChecked(photo)
                binding.checkView.visibility = View.VISIBLE
                return@setOnLongClickListener true
            }
        }

        override fun bindView(any: LocalPhoto) {
            photo = any
            val height: Int = any.height * defaultWidth / any.width
            val params: ViewGroup.LayoutParams =
                binding.photoView.layoutParams ?: ViewGroup.LayoutParams(defaultWidth, height)
            params.width = defaultWidth
            params.height = height
            binding.photoView.layoutParams = params
            binding.photoView.setImageURI(any.uri, context)
            if (isChoiceState && photo.isChecked) {
                binding.checkView.visibility = View.VISIBLE
            } else {
                binding.checkView.visibility = View.GONE
            }
        }
    }
}
