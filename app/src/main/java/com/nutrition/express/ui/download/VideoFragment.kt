package com.nutrition.express.ui.download

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.nutrition.express.R
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.common.CommonViewHolder
import com.nutrition.express.common.MyExoPlayer
import com.nutrition.express.databinding.FragmentDownloadVideoBinding
import com.nutrition.express.databinding.ItemDownloadVideoBinding
import com.nutrition.express.model.data.bean.LocalVideo
import com.nutrition.express.util.FileUtils.deleteFile
import com.nutrition.express.util.FileUtils.publicVideoDir
import com.nutrition.express.util.FileUtils.videoDir
import com.nutrition.express.util.getBoolean
import com.nutrition.express.util.putBoolean
import java.io.File

class VideoFragment : Fragment() {
    private val SHOW_USER_VIDEO = "SUV"
    private var adapter: CommonRVAdapter? = null

    private var binding: FragmentDownloadVideoBinding? = null

    private var userVideo: ArrayList<LocalVideo> = ArrayList()
    private var allVideo: ArrayList<LocalVideo> = ArrayList()
    private var videoList: ArrayList<LocalVideo> = ArrayList()
    private var showUserVideo: Boolean = false

    private var isChoiceState: Boolean = false
    private var checkedCount: Int = 0
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
                    checkAllVideos()
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
    }

    override fun onPause() {
        super.onPause()
        MyExoPlayer.pause()
        actionMode?.let {
            it.finish()
            adapter?.notifyDataSetChanged()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDownloadVideoBinding.inflate(inflater, container, false)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = CommonRVAdapter.adapter {
            addViewType(
                LocalVideo::class,
                R.layout.item_download_video,
                this@VideoFragment::VideoViewHolder
            )
        }
        adapter.resetData(videoList.toTypedArray(), false)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Fresco.getImagePipeline().resume()
                } else {
                    Fresco.getImagePipeline().pause()
                }
            }
        })
        this.binding = binding
        this.adapter = adapter

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        showUserVideo = getBoolean(SHOW_USER_VIDEO, false)
        if (showUserVideo) {
            initVideoDataUser()
        } else {
            initVideoDataAll()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        binding = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_download_video, menu)
        val menuItem = menu.findItem(R.id.show_user_video)
        menuItem.isChecked = showUserVideo
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.show_user_video) {
            item.isChecked = !item.isChecked
            showUserVideo = item.isChecked
            if (showUserVideo) {
                initVideoDataUser()
            } else {
                initVideoDataAll()
            }
            adapter?.resetData(videoList.toTypedArray(), false)
            putBoolean(SHOW_USER_VIDEO, showUserVideo)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initVideoDataUser() {
        if (userVideo.isEmpty()) {
            val userVideoDir = videoDir
            if (userVideoDir.isDirectory) {
                getVideoFile(userVideoDir, userVideo)
                sortPhotoData(userVideo)
            }
        }
        videoList = userVideo
    }

    private fun initVideoDataAll() {
        if (allVideo.isEmpty()) {
            val publicVideoDir = publicVideoDir
            if (publicVideoDir.isDirectory) {
                getVideoFile(publicVideoDir, allVideo)
                sortPhotoData(allVideo)
            }
        }
        videoList = allVideo
    }

    private fun getVideoFile(dir: File, list: MutableList<LocalVideo>) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                getVideoFile(file, list)
            } else {
                if (file.name.endsWith(".mp4")) {
                    list.add(LocalVideo(file))
                }
            }
        }
    }

    private fun sortPhotoData(videos: List<LocalVideo>) {
        videos.sortedWith { o1, o2 ->
            val x = o1.file.lastModified()
            val y = o2.file.lastModified()
            if (x < y) 1 else if (x == y) 0 else -1
        }
    }

    private fun startMultiChoice() {
        videoList.forEach { it.isChecked = false }
        actionMode = (activity as DownloadedActivity).startSupportActionMode(callback)
        checkedCount = 0
        isChoiceState = true
    }

    private fun finishMultiChoice() {
        actionMode?.finish()
        actionMode = null
        isChoiceState = false
    }

    private fun onItemChecked(localVideo: LocalVideo) {
        localVideo.isChecked = !localVideo.isChecked
        if (localVideo.isChecked) {
            checkedCount++
        } else {
            checkedCount--
        }
        actionMode?.title = checkedCount.toString()
    }

    private fun deleteCheckedVideos() {
        userVideo.removeAll {
            if (it.isChecked) {
                deleteFile(it.file)
                return@removeAll true
            }
            return@removeAll false
        }
        allVideo.removeAll {
            if (it.isChecked) {
                deleteFile(it.file)
                return@removeAll true
            }
            return@removeAll false
        }
        adapter?.resetData(videoList.toTypedArray(), false)
    }

    private fun checkAllVideos() {
        videoList.forEach { it.isChecked = true }
        checkedCount = videoList.size
        actionMode?.title = checkedCount.toString()
        adapter?.notifyDataSetChanged()
    }

    private fun showDeleteDialog() {
        context?.let {
            AlertDialog.Builder(it).run {
                setPositiveButton(R.string.delete_positive) { _, _ ->
                    deleteCheckedVideos()
                    finishMultiChoice()
                }
                setNegativeButton(R.string.pic_cancel, null)
                setTitle(R.string.download_video_delete_title)
                show()
            }
        }
    }

    fun scrollToTop() {
        binding?.recyclerView?.scrollToPosition(0)
    }

    inner class VideoViewHolder(view: View) : CommonViewHolder<LocalVideo>(view) {
        private val binding = ItemDownloadVideoBinding.bind(view)
        private lateinit var video: LocalVideo

        init {
            binding.playerView.setOnClickListener {
                if (isChoiceState) {
                    onItemChecked(video)
                    if (video.isChecked) {
                        binding.checkView.visibility = View.VISIBLE
                    } else {
                        binding.checkView.visibility = View.GONE
                    }
                } else {
                    binding.playerView.performPlayerClick()
                }
            }
            binding.playerView.setOnLongClickListener {
                if (actionMode == null) {
                    startMultiChoice()
                    onItemChecked(video)
                    binding.checkView.visibility = View.VISIBLE
                }
                return@setOnLongClickListener true
            }
        }

        override fun bindView(any: LocalVideo) {
            video = any
            binding.playerView.bindVideo(any)
            if (isChoiceState && video.isChecked) {
                binding.checkView.visibility = View.VISIBLE
            } else {
                binding.checkView.visibility = View.GONE
            }
        }

    }
}
