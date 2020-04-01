package com.nutrition.express.common

import android.graphics.Color
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.nutrition.express.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.set


private const val TYPE_DATA_BASE = 1000
private const val TYPE_UNKNOWN = 0 //未知类型

private const val EMPTY = 10 /*显示EMPTY VIEW*/
private const val LOADING = 11 /*显示LOADING VIEW*/
private const val LOADING_FAILURE = 12 /*显示LOADING FAILURE VIEW*/
private const val LOADING_NEXT = 13 /*显示LOADING NEXT VIEW*/
private const val LOADING_NEXT_FAILURE = 14 /*显示LOADING NEXT FAILURE VIEW*/
private const val LOADING_FINISH = 15 //显示LOADING FINISH VIEW

typealias Creator = (View) -> CommonViewHolder

class CommonRVAdapter private constructor(builder: Builder) : RecyclerView.Adapter<CommonViewHolder>() {

    /* 状态 */

    private var state = LOADING_FINISH

    private var isFinishViewEnabled = false
    private var isEmptyViewEnabled = false
    private var isLoadingViewEnabled = false

    //保存了layout_id与MultiType键值对
    private var typeArray: SparseArray<MultiType>

    //保存了数据类型名称与layout_id的键值对
    private lateinit var typeMap: HashMap<String, Int>
    private var loadListener: OnLoadListener? = null
    private lateinit var data: MutableList<Any>
    private var extra: Any? = null

    private val onRetryListener = View.OnClickListener {
        loadListener?.let {
            it.retry()
            state = if (data.isNotEmpty()) LOADING_NEXT else LOADING
            notifyItemChanged(data.size)
        }
    }

    init {
        isFinishViewEnabled = builder.isFinishViewEnabled
        isEmptyViewEnabled = builder.isEmptyViewEnabled
        isLoadingViewEnabled = builder.isLoadingViewEnabled
        typeArray = builder.typeArray
        typeMap = builder.typeMap
        loadListener = builder.loadListener
        data = builder.data ?: ArrayList()
        if (builder.data == null) {
            state = LOADING
        } else if (data.isEmpty()) {
            state = EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val type: MultiType? = typeArray[viewType]
        return if (type == null) {     //check if unknown type
            val textView = inflater
                    .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
            textView.text = "Error!!! Unknown type!!!"
            textView.setTextColor(Color.parseColor("#ff0000"))
            CommonViewHolder(textView)
        } else {
            val view: View = inflater.inflate(type.layout, parent, false)
            if (viewType == LOADING_FAILURE) {
                view.setOnClickListener(onRetryListener)
            } else if (viewType == LOADING_NEXT_FAILURE) {
                view.setOnClickListener(onRetryListener)
            }
            type.creator.invoke(view)
        }
    }

    override fun onViewAttachedToWindow(holder: CommonViewHolder) {
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: CommonViewHolder) {
        holder.onViewDetachedFromWindow()
    }

    override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
        bindViewHolder(holder, position, null)
    }

    override fun onBindViewHolder(holder: CommonViewHolder, position: Int, payloads: List<Any>) {
        bindViewHolder(holder, position, payloads)
    }

    private fun bindViewHolder(holder: CommonViewHolder, position: Int, payloads: List<Any>?) {
        if (position < data.size) {
            if (payloads.isNullOrEmpty()) {
                holder.bindView(data[position])
            } else {
                holder.bindView(data[position], payloads)
            }
        } else {
            holder.bindView(extra ?: "")
            if (state == LOADING_NEXT) {
                loadListener?.loadNextPage()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == data.size) {
            state
        } else {
            val type = typeMap[data[position].javaClass.name]
            type ?: TYPE_UNKNOWN
        }
    }

    override fun getItemCount(): Int {
        if (data.size > 0) {
            if (state == EMPTY) {
                state = LOADING_FINISH
            }
        } else if (state == LOADING_FINISH) {
            state = EMPTY
        }
        return if (state == EMPTY && !isEmptyViewEnabled || state == LOADING_FINISH && !isFinishViewEnabled) {
            data.size
        } else {
            data.size + 1
        }
    }

    fun size(): Int {
        return data.size
    }

    fun getData(): List<Any> {
        return data
    }

    fun append(array: Array<Any>?, autoLoadingNext: Boolean) {
        if (array.isNullOrEmpty()) {
            if (data.size > 0) {
                //all data has been loaded
                state = LOADING_FINISH
                if (isFinishViewEnabled) {
                    notifyItemChanged(this.data.size)
                } else {
                    notifyItemRemoved(this.data.size)
                }
            } else {
                //no data, show empty view
                state = EMPTY
                notifyDataSetChanged()
            }
        } else {
            val lastDataIndex = data.size
            var size = array.size
            data.addAll(array)
            if (autoLoadingNext) {
                state = LOADING_NEXT
                size++
            } else {
                state = LOADING_FINISH
            }
            if (lastDataIndex == 0) {
                notifyDataSetChanged()
            } else {
                notifyItemRangeChanged(lastDataIndex, size)
            }
        }
    }

    fun remove(position: Int) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    fun remove(start: Int, count: Int) {
        data.subList(start, start + count).clear();
        notifyItemRangeRemoved(start, count);
    }

    fun insert(start: Int, array: Array<Any>) {
        data.addAll(start, array.toMutableList())
        notifyItemRangeInserted(start, array.size);
    }

    fun move(fromPosition: Int, toPosition: Int) {
        Collections.swap(data, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    fun replace(index: Int, any: Any) {
        if (index >= 0 && index < data.size) {
            data[index] = any;
            notifyItemChanged(index);
        }
    }

    fun clear() {
        data.clear()
        state = if (isLoadingViewEnabled) LOADING else EMPTY
        notifyDataSetChanged()
    }

    fun resetData(data: Array<Any>?, autoLoadingNext: Boolean) {
        this.data.clear()
        append(data, autoLoadingNext)
    }

    fun showLoadingFinish() {
        state = LOADING_FINISH
        notifyItemChanged(data.size)
    }

    fun showLoadingFailure(error: Any?) {
        extra = error
        state = if (data.isEmpty()) LOADING_FAILURE else LOADING_NEXT_FAILURE
        notifyItemChanged(data.size)
    }

    fun showEmptyView() {
        isEmptyViewEnabled = true
        state = EMPTY
        data.clear()
        notifyDataSetChanged()
    }

    fun showLoadingView() {
        state = LOADING
        notifyDataSetChanged()
    }

    fun showReloading() {
        state = LOADING
        data.clear()
        notifyDataSetChanged()
    }

    interface OnLoadListener {
        fun retry()
        fun loadNextPage()
    }

    data class MultiType(val layout: Int, val creator: Creator)

    class Builder {
        private var emptyView = R.layout.item_empty
        private var loadingView = R.layout.item_loading
        private var failureView = R.layout.item_loading_failure
        private var nextView = R.layout.item_loading
        private var nextFailureView = R.layout.item_loading_failure
        private var finishView = R.layout.item_finish
        private var emptyCreator: Creator? = null
        private var loadingCreator: Creator? = null
        private var failureCreator: Creator? = null
        private var nextCreator: Creator? = null
        private var nextFailureCreator: Creator? = null
        private var finishCreator: Creator? = null
        var isFinishViewEnabled = false
        var isEmptyViewEnabled = false
        var isLoadingViewEnabled = false
        var loadListener: OnLoadListener? = null
        var data: MutableList<Any>? = null
        internal var typeArray = SparseArray<MultiType>()
        internal var typeMap = HashMap<String, Int>()
        private var base = TYPE_DATA_BASE
        private var defaultCreator: Creator = { CommonViewHolder(it) }
        private var errorCreator: Creator = { ErrorViewHolder(it) }

        fun setEmptyView(@LayoutRes emptyView: Int, creator: Creator): Builder {
            this.emptyView = emptyView
            this.emptyCreator = creator
            return this
        }

        fun setLoadingView(@LayoutRes loadingView: Int, creator: Creator): Builder {
            this.loadingView = loadingView
            this.loadingCreator = creator
            return this
        }

        fun setFailureView(@LayoutRes failureView: Int, creator: Creator): Builder {
            this.failureView = failureView
            this.failureCreator = creator
            return this
        }

        fun setNextView(@LayoutRes nextView: Int, creator: Creator): Builder {
            this.nextView = nextView
            this.nextCreator = creator
            return this
        }

        fun setNextFailureView(@LayoutRes nextFailureView: Int, creator: Creator): Builder {
            this.nextFailureView = nextFailureView
            this.nextFailureCreator = creator
            return this
        }

        fun setFinishView(@LayoutRes finishView: Int, creator: Creator): Builder {
            this.finishView = finishView
            this.finishCreator = creator
            return this
        }

        fun addItemType(c: Class<*>, layout: Int, create: Creator): Builder {
            typeArray.put(base, MultiType(layout, create))
            typeMap[c.name] = base
            base++
            return this
        }

        fun build(): CommonRVAdapter {
            addStateType()
            return CommonRVAdapter(this)
        }

        private fun addStateType() {
            typeArray.put(EMPTY, MultiType(emptyView, emptyCreator ?: defaultCreator))
            typeArray.put(LOADING, MultiType(loadingView, loadingCreator ?: defaultCreator))
            typeArray.put(LOADING_FAILURE, MultiType(failureView, failureCreator ?: errorCreator))
            typeArray.put(LOADING_NEXT, MultiType(nextView, nextCreator ?: defaultCreator))
            typeArray.put(LOADING_NEXT_FAILURE, MultiType(nextFailureView, nextFailureCreator ?: errorCreator))
            typeArray.put(LOADING_FINISH, MultiType(finishView, finishCreator ?: defaultCreator))
        }
    }
}
