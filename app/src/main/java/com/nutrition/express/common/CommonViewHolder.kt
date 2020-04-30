package com.nutrition.express.common

import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by huang on 10/19/16.
 */
open class CommonViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {
    open fun bindView(any: T) {}
    open fun bindView(any: T, objects: List<Any>) {}
    open fun onAttach() {}
    open fun onDetach() {}
    open fun onViewDetachedFromWindow() {}
    open fun onViewAttachedToWindow() {}

}