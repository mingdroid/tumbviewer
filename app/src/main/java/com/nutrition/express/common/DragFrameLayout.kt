package com.nutrition.express.common

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.nutrition.express.util.SwipeGestureDetector

class DragFrameLayout : FrameLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    private var dismissListener: OnDismissListener? = null
    private var initHeight //child view's original height;
            = 0
    private var initWidth = 0
    private var initLeft = 0
    private var initTop = 0
    private val swipeGestureDetector: SwipeGestureDetector = SwipeGestureDetector(context,
        object : SwipeGestureDetector.OnSwipeGestureListener {
            override fun onSwipeTopBottom(deltaX: Float, deltaY: Float) {
                dragChildView(deltaX, deltaY)
            }

            override fun onSwipeLeftRight(deltaX: Float, deltaY: Float) {
            }

            override fun onFinish(direction: Int, distanceX: Float, distanceY: Float) {
                dismissListener?.let {
                    if (direction == SwipeGestureDetector.DIRECTION_TOP_BOTTOM) {
                        if (distanceY > initHeight / 10) {
                            it.onDismiss()
                        } else {
                            it.onCancel()
                            reset()
                        }
                    }
                }
            }
        })

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return swipeGestureDetector.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return swipeGestureDetector.onTouchEvent(event)
    }

    private fun dragChildView(deltaX: Float, deltaY: Float) {
        val count = childCount
        if (count > 0) {
            val view = getChildAt(0)
            scaleAndMove(view, deltaX, deltaY)
        }
    }

    private fun scaleAndMove(view: View, deltaX: Float, deltaY: Float) {
        val params =
            (view.layoutParams ?: MarginLayoutParams(view.width, view.height)) as MarginLayoutParams
        if (params.width <= 0 && params.height <= 0) {
            params.width = view.width
            params.height = view.height
        }
        if (initHeight <= 0) {
            initHeight = view.height
            initWidth = view.width
            initLeft = params.leftMargin
            initTop = params.topMargin
        }
        val percent = deltaY / height
        val scaleX = (initWidth * percent).toInt()
        val scaleY = (initHeight * percent).toInt()
        params.width = params.width - scaleX
        params.height = params.height - scaleY
        params.leftMargin += deltaX.toInt() + scaleX / 2
        params.topMargin += deltaY.toInt() + scaleY / 2
        view.layoutParams = params
        dismissListener?.onScaleProgress(percent)
    }

    private fun reset() {
        val count = childCount
        if (count > 0) {
            val view = getChildAt(0)
            val params = view.layoutParams as MarginLayoutParams
            params.width = initWidth
            params.height = initHeight
            params.leftMargin = initLeft
            params.topMargin = initTop
            view.layoutParams = params
        }
    }

    fun setDismissListener(dismissListener: OnDismissListener) {
        this.dismissListener = dismissListener
    }

    interface OnDismissListener {
        fun onScaleProgress(scale: Float)
        fun onDismiss()
        fun onCancel()
    }

}