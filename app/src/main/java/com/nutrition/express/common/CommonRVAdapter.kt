package com.nutrition.express.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nutrition.express.R
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.KClass


private const val TYPE_UNKNOWN = 0 //未知类型

private const val EMPTY = 10 /*显示EMPTY VIEW*/
private const val LOADING = 11 /*显示LOADING VIEW*/
private const val LOADING_FAILURE = 12 /*显示LOADING FAILURE VIEW*/
private const val LOADING_NEXT = 13 /*显示LOADING NEXT VIEW*/
private const val LOADING_NEXT_FAILURE = 14 /*显示LOADING NEXT FAILURE VIEW*/
private const val LOADING_FINISH = 15 //显示LOADING FINISH VIEW

class CommonRVAdapter private constructor(builder: Builder) :
    RecyclerView.Adapter<CommonViewHolder<Any>>() {

    /* 状态 */

    private var state = LOADING_FINISH

    private var isFinishViewEnabled = false
    private var isEmptyViewEnabled = false
    private var isLoadingViewEnabled = false
    private val loadListener: OnLoadListener?
    private val data: MutableList<Any>
    private val itemFactory: ItemViewHolderFactory
    private val stateFactory: StateViewHolderFactory

    private var extra: Any? = null

    private var onRetryListener: View.OnClickListener

    init {
        isFinishViewEnabled = builder.isFinishViewEnabled
        isEmptyViewEnabled = builder.isEmptyViewEnabled
        isLoadingViewEnabled = builder.isLoadingViewEnabled
        loadListener = builder.loadListener
        data = builder.data ?: ArrayList()
        itemFactory = builder.itemFactory
        stateFactory = builder.stateFactory

        onRetryListener = View.OnClickListener {
            loadListener?.let {
                it.retry()
                state = if (data.isNotEmpty()) LOADING_NEXT else LOADING
                notifyItemChanged(data.size)
            }
        }
        if (builder.data == null) {
            state = LOADING
        } else if (data.isEmpty()) {
            state = EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder<Any> {
        val vh = if (viewType in 10..15)
            stateFactory.createViewHolder(parent, viewType)
        else
            itemFactory.createViewHolder(parent, viewType)
        return if (vh == null) {     //check if unknown type
            val inflater = LayoutInflater.from(parent.context)
            val textView = inflater
                .inflate(android.R.layout.simple_list_item_1, parent, false) as TextView
            textView.text = "Error!!! Unknown type!!!"
            textView.setTextColor(Color.parseColor("#ff0000"))
            CommonViewHolder(textView)
        } else {
            if (viewType == LOADING_FAILURE) {
                vh.itemView.setOnClickListener(onRetryListener)
            } else if (viewType == LOADING_NEXT_FAILURE) {
                vh.itemView.setOnClickListener(onRetryListener)
            }
            vh as CommonViewHolder<Any>
        }
    }

    override fun onViewAttachedToWindow(holder: CommonViewHolder<Any>) {
        holder.onViewAttachedToWindow()
    }

    override fun onViewDetachedFromWindow(holder: CommonViewHolder<Any>) {
        holder.onViewDetachedFromWindow()
    }

    override fun onBindViewHolder(holder: CommonViewHolder<Any>, position: Int) {
        bindViewHolder(holder, position, null)
    }

    override fun onBindViewHolder(
        holder: CommonViewHolder<Any>,
        position: Int,
        payloads: List<Any>
    ) {
        bindViewHolder(holder, position, payloads)
    }

    private fun bindViewHolder(holder: CommonViewHolder<Any>, position: Int, payloads: List<Any>?) {
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
            itemFactory.getItemViewType(data[position]) ?: TYPE_UNKNOWN
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
        data.remove(position)
        notifyItemRemoved(position)
    }

    fun remove(start: Int, count: Int) {
        data.subList(start, start + count).clear()
        notifyItemRangeRemoved(start, count)
    }

    fun insert(start: Int, array: Array<Any>) {
        data.addAll(start, array.toMutableList())
        notifyItemRangeInserted(start, array.size)
    }

    fun move(fromPosition: Int, toPosition: Int) {
        Collections.swap(data, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun replace(index: Int, any: Any) {
        if (index >= 0 && index < data.size) {
            data[index] = any
            notifyItemChanged(index)
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

    interface ItemViewHolderFactory {
        fun getItemViewType(data: Any): Int? //the return viewType should not less than 1000
        fun createViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder<*>?
    }

    open class StateViewHolderFactory {
        fun createViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder<*>? {
            val inflater = LayoutInflater.from(parent.context)
            return when (viewType) {
                EMPTY ->
                    createEmptyView(inflater, parent)
                LOADING_FINISH ->
                    createFinishView(inflater, parent)
                LOADING, LOADING_NEXT ->
                    createLoadingView(inflater, parent)
                LOADING_FAILURE, LOADING_NEXT_FAILURE ->
                    createFailureView(inflater, parent)
                else -> null
            }
        }

        open fun createEmptyView(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): CommonViewHolder<Any> {
            return CommonViewHolder(inflater.inflate(R.layout.item_empty, parent, false))
        }

        open fun createFinishView(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): CommonViewHolder<Any> {
            return CommonViewHolder(inflater.inflate(R.layout.item_finish, parent, false))
        }

        open fun createLoadingView(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): CommonViewHolder<Any> {
            return CommonViewHolder(inflater.inflate(R.layout.item_loading, parent, false))
        }

        open fun createFailureView(
            inflater: LayoutInflater,
            parent: ViewGroup
        ): CommonViewHolder<Any> {
            return ErrorViewHolder(inflater.inflate(R.layout.item_loading_failure, parent, false))
        }
    }

    companion object {
        private val defaultStateFactory = StateViewHolderFactory()
        private const val base = 1000

        fun adapter(init: Builder.() -> Unit): CommonRVAdapter {
            val builder = Builder()
            builder.init()
            return CommonRVAdapter(builder)
        }
    }

    class Builder {
        private val classList = ArrayList<KClass<*>>()
        private val layoutList = ArrayList<Int>()
        private val viewHolderList = ArrayList<(View) -> CommonViewHolder<*>>()

        var isFinishViewEnabled = false
        var isEmptyViewEnabled = false
        var isLoadingViewEnabled = false
        var loadListener: OnLoadListener? = null
        var data: MutableList<Any>? = null
        var stateFactory: StateViewHolderFactory = defaultStateFactory
        val itemFactory: ItemViewHolderFactory = object : ItemViewHolderFactory {
            override fun getItemViewType(data: Any): Int? {
                for (i in classList.indices) {
                    if (classList[i].isInstance(data)) {
                        return base + i
                    }
                }
                return null
            }

            override fun createViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder<*>? {
                val index = viewType - base
                if (index in 0 until layoutList.size) {
                    val view = LayoutInflater.from(parent.context)
                        .inflate(layoutList[index], parent, false)
                    return viewHolderList[index].invoke(view)
                }
                return null
            }
        }

        fun addViewType(clazz: KClass<*>, layout: Int, creator: (View) -> CommonViewHolder<*>) {
            classList.add(clazz)
            layoutList.add(layout)
            viewHolderList.add(creator)
        }

    }
}
