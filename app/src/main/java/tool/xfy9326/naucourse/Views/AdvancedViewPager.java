package tool.xfy9326.naucourse.Views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by 10696 on 2018/2/27.
 * 实现无法滑屏滚动
 */

public class AdvancedViewPager extends ViewPager {
    private boolean scroll = true;

    public AdvancedViewPager(Context context) {
        super(context);
    }

    public AdvancedViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @SuppressWarnings("SameParameterValue")
    public void setScroll(boolean scroll) {
        this.scroll = scroll;
    }

    @Override
    public boolean onTouchEvent(MotionEvent arg0) {
        performClick();
        return scroll && super.onTouchEvent(arg0);
    }

    @Override
    public boolean performClick() {
        return scroll && super.performClick();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent arg0) {
        return scroll && super.onInterceptTouchEvent(arg0);
    }

}
