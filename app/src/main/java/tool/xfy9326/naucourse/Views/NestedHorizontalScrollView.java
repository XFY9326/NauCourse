package tool.xfy9326.naucourse.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

public class NestedHorizontalScrollView extends HorizontalScrollView {
    private boolean mCanScroll = true;
    private boolean fitWidthNestedScroll = false;
    private float mDownX;

    public NestedHorizontalScrollView(Context context) {
        super(context);
    }

    public NestedHorizontalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedHorizontalScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setFitWidthNestedScroll(boolean fitWidthNestedScroll) {
        this.fitWidthNestedScroll = fitWidthNestedScroll;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!fitWidthNestedScroll) {
            return super.onTouchEvent(ev);
        } else {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_UP:
                    mCanScroll = true;
                    performClick();
                    break;
                case MotionEvent.ACTION_DOWN:
                    mDownX = ev.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int scrollX = getScrollX();
                    if ((scrollX == 0 && mDownX - ev.getX() <= -10) || (getChildAt(0).getMeasuredWidth() <= (scrollX + ((ViewGroup) getParent()).getWidth()) && mDownX - ev.getX() >= 10)) {
                        mCanScroll = false;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mCanScroll = true;
                    break;
            }

            if (this.mCanScroll) {
                getParent().requestDisallowInterceptTouchEvent(true);
                return super.onTouchEvent(ev);
            } else {
                getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        }
    }
}
