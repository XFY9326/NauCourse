package tool.xfy9326.naucourse.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

/**
 * 解决SwipeRefreshLayout嵌套ViewPager冲突的问题
 * From: https://blog.csdn.net/ding19972431/article/details/82114531
 */

public class ScoreSwipeRefreshLayout extends SwipeRefreshLayout {
    private final int mTouchSlop;
    private float startY;
    private float startX;
    private boolean mIsVpDragger;

    public ScoreSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startY = ev.getY();
                startX = ev.getX();
                mIsVpDragger = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsVpDragger) {
                    return false;
                }

                float endY = ev.getY();
                float endX = ev.getX();
                float distanceX = Math.abs(endX - startX);
                float distanceY = Math.abs(endY - startY);
                if (distanceX > mTouchSlop && distanceX > distanceY) {
                    mIsVpDragger = true;
                    return false;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsVpDragger = false;
                break;
            default:
        }
        return super.onInterceptTouchEvent(ev);
    }
}
