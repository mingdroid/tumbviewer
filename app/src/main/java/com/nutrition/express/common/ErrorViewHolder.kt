package com.nutrition.express.common

import android.view.View
import android.widget.TextView
import com.nutrition.express.R

/**
 * Created by huang on 11/9/16.
 */
class ErrorViewHolder(view: View) : CommonViewHolder<Any>(view) {
    override fun bindView(any: Any) {
        if (itemView is TextView) {
            val text = if (any is String) any else itemView.getContext().getString(R.string.load_failed)
            itemView.text = text
        }
    }
}