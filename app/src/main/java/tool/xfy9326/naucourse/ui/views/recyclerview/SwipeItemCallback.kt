package tool.xfy9326.naucourse.ui.views.recyclerview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.GradientDrawable
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import tool.xfy9326.naucourse.utils.BaseUtils.dpToPx
import tool.xfy9326.naucourse.utils.views.ViewUtils
import kotlin.math.abs

// 滑动删除
class SwipeItemCallback<T : SwipeItemViewHolder>(private val listener: OnItemSwipedListener<T>) : ItemTouchHelper.Callback() {
    private var savedHasSwipedStatus = false
    private var hasChangedCorner = false
    private var hasAnimatedIcon = false

    companion object {
        private val foregroundSwipeCorner = 10f.dpToPx()
        private const val foregroundNotSwipeCorner = 0f
        private const val foregroundCornerDuration = 150L
        private const val backgroundCircularRevealDuration = 380L
        private const val swipeIconDuration = 100L
        private const val swipeIconScaleTo = 1.2f
    }

    @Volatile
    private var circularRevealAnimation: Animator? = null

    override fun isItemViewSwipeEnabled(): Boolean = true
    override fun isLongPressDragEnabled(): Boolean = false
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int = makeMovementFlags(0, ItemTouchHelper.START)

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        @Suppress("UNCHECKED_CAST")
        listener.onSwipedItem(viewHolder as T, viewHolder.adapterPosition)
    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = .3f

    override fun getSwipeVelocityThreshold(defaultValue: Float): Float = .8f

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        val swipeViewHolder = viewHolder as SwipeItemViewHolder
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            if (dX == 0f) {
                resetSwipeView(swipeViewHolder)
            }
            setupForegroundCorner(swipeViewHolder, dX)
            if (swipeViewHolder.imageViewSwipeIcon.drawable is AnimatedVectorDrawable
                || swipeViewHolder.imageViewSwipeIcon.drawable is AnimatedVectorDrawableCompat
            ) {
                setupSwipeIcon(swipeViewHolder, dX, isCurrentlyActive)
            }
            setupBackground(recyclerView, swipeViewHolder, dX)
        }
        getDefaultUIUtil().onDraw(c, recyclerView, swipeViewHolder.foregroundSwipeView, dX, dY, actionState, isCurrentlyActive)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        val swipeViewHolder = viewHolder as SwipeItemViewHolder
        resetSwipeView(swipeViewHolder)
        (swipeViewHolder.foregroundSwipeView.background as GradientDrawable).cornerRadius = 0f
        getDefaultUIUtil().clearView(viewHolder.foregroundSwipeView)
    }

    @Synchronized
    private fun resetSwipeView(viewHolder: SwipeItemViewHolder) {
        savedHasSwipedStatus = false
        viewHolder.backgroundShowSwipeView.visibility = View.INVISIBLE
    }

    @Synchronized
    private fun setupBackground(recyclerView: RecyclerView, viewHolder: SwipeItemViewHolder, dX: Float) {
        val notSwiped = recyclerView.width * getSwipeThreshold(viewHolder) > abs(dX)

        if (notSwiped) {
            if (savedHasSwipedStatus) {
                savedHasSwipedStatus = false

                animateSwipeIcon(viewHolder)
                animateTransitionBackground(viewHolder, dX, false)
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        } else {
            if (!savedHasSwipedStatus) {
                savedHasSwipedStatus = true

                animateSwipeIcon(viewHolder)
                animateTransitionBackground(viewHolder, dX, true)
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    @Synchronized
    private fun setupForegroundCorner(viewHolder: SwipeItemViewHolder, dX: Float) {
        if (abs(dX) < 1f) {
            if (hasChangedCorner) {
                hasChangedCorner = false
                animateForegroundCorner(viewHolder, foregroundSwipeCorner, foregroundNotSwipeCorner)
            }
        } else {
            if (!hasChangedCorner) {
                hasChangedCorner = true
                animateForegroundCorner(viewHolder, foregroundNotSwipeCorner, foregroundSwipeCorner)
            }
        }
    }

    @Synchronized
    private fun setupSwipeIcon(viewHolder: SwipeItemViewHolder, dX: Float, isCurrentlyActive: Boolean) {
        if (viewHolder.itemView.right + dX < viewHolder.imageViewSwipeIcon.left) {
            if (hasAnimatedIcon) {
                hasAnimatedIcon = false
                ViewUtils.tryStartAnimateDrawable(viewHolder.imageViewSwipeIcon.drawable)
            }
        } else {
            if (!hasAnimatedIcon) {
                hasAnimatedIcon = true
                if (isCurrentlyActive) {
                    ViewUtils.tryStartAnimateDrawable(viewHolder.imageViewSwipeIcon.drawable)
                }
            }
        }
    }

    private fun animateForegroundCorner(viewHolder: SwipeItemViewHolder, from: Float, to: Float) {
        ValueAnimator.ofFloat(from, to).apply {
            duration = foregroundCornerDuration
            addUpdateListener {
                val value = it.animatedValue as Float
                (viewHolder.foregroundSwipeView.background as GradientDrawable).cornerRadii =
                    floatArrayOf(0f, 0f, value, value, value, value, 0f, 0f)
            }
            start()
        }
    }

    private fun animateSwipeIcon(viewHolder: SwipeItemViewHolder) {
        ScaleAnimation(1f, swipeIconScaleTo, 1f, swipeIconScaleTo, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f).apply {
            duration = swipeIconDuration
            repeatCount = 1
            repeatMode = Animation.REVERSE
            viewHolder.imageViewSwipeIcon.startAnimation(this)
        }
    }

    private fun animateTransitionBackground(viewHolder: SwipeItemViewHolder, dX: Float, showSwipeBackground: Boolean) {
        val iconX = viewHolder.imageViewSwipeIcon.x + viewHolder.imageViewSwipeIcon.width / 2
        val iconY = viewHolder.imageViewSwipeIcon.y + viewHolder.imageViewSwipeIcon.height / 2
        val startRadius = if (showSwipeBackground) {
            0f
        } else {
            viewHolder.foregroundSwipeView.right + dX
        }
        val finalRadius = if (showSwipeBackground) {
            viewHolder.imageViewSwipeIcon.x
        } else {
            0f
        }

        circularRevealAnimation?.removeAllListeners()
        circularRevealAnimation?.cancel()

        circularRevealAnimation = ViewAnimationUtils.createCircularReveal(
            viewHolder.backgroundShowSwipeView, iconX.toInt(), iconY.toInt(), startRadius, finalRadius
        ).apply {
            duration = backgroundCircularRevealDuration
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    if (showSwipeBackground) {
                        viewHolder.backgroundShowSwipeView.visibility = View.VISIBLE
                    }
                }

                override fun onAnimationCancel(animation: Animator?) {
                    if (showSwipeBackground) {
                        viewHolder.backgroundShowSwipeView.visibility = View.VISIBLE
                    } else {
                        viewHolder.backgroundShowSwipeView.visibility = View.INVISIBLE
                    }
                }

                override fun onAnimationEnd(animation: Animator?) {
                    if (!showSwipeBackground) {
                        viewHolder.backgroundShowSwipeView.visibility = View.INVISIBLE
                    }
                }
            })
            start()
        }
    }

    interface OnItemSwipedListener<T : SwipeItemViewHolder> {
        fun onSwipedItem(viewHolder: T, position: Int)
    }
}