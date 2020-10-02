package com.nutrition.express.ui.downloading

import android.graphics.drawable.ClipDrawable
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nutrition.express.R
import com.nutrition.express.application.BaseActivity
import com.nutrition.express.common.CommonRVAdapter
import com.nutrition.express.common.CommonViewHolder
import com.nutrition.express.databinding.ActivityDownloadingBinding
import com.nutrition.express.databinding.ItemDownloadBinding
import com.nutrition.express.model.download.ProgressResponseBody
import com.nutrition.express.model.download.Record
import com.nutrition.express.model.download.RxDownload
import java.util.*

class DownloadingActivity : BaseActivity() {
    private lateinit var binding: ActivityDownloadingBinding
    private lateinit var adapter: CommonRVAdapter
    private val listeners = HashMap<String, ProgressResponseBody.ProgressListener>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.downloading)

        adapter = CommonRVAdapter.adapter {
            addViewType(
                Record::class,
                R.layout.item_download,
                this@DownloadingActivity::RxDownloadVH
            )
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        adapter.append(RxDownload.getInstance().records.toTypedArray(), false)
    }

    override fun onDestroy() {
        super.onDestroy()
        for ((key, value) in listeners) {
            RxDownload.getInstance().removeProgressListener(key, value)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    inner class RxDownloadVH(view: View) : CommonViewHolder<Record>(view),
        ProgressResponseBody.ProgressListener {
        private val binding = ItemDownloadBinding.bind(view)
        private val progressDrawable: ClipDrawable = itemView.background as ClipDrawable
        private lateinit var record: Record

        override fun bindView(any: Record) {
            record = any
            binding.url.text = record.url
        }

        override fun onAttach() {
            RxDownload.getInstance().start(record.url, this)
            listeners[record.url] = this
        }

        override fun onDetach() {
            RxDownload.getInstance().removeProgressListener(record.url, this)
            listeners.remove(record.url)
        }

        override fun update(bytesRead: Long, contentLength: Long, done: Boolean) {
            if (done) {
                if (adapterPosition == RecyclerView.NO_POSITION) return
                adapter.remove(adapterPosition)
            } else {
                val percent = (100 * bytesRead / contentLength).toInt()
                progressDrawable.level = percent * 100
                binding.progress.text = "$percent%"
            }
        }
    }
}
