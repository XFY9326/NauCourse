package tool.xfy9326.naucourse.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.wear.widget.WearableRecyclerView;

/**
 * @author xfy9326
 */

public class AdvancedRecyclerView extends WearableRecyclerView {
    private View emptyView;
    final private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            checkIfEmpty();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            checkIfEmpty();
        }
    };
    private boolean useBottomListener = true;
    private OnBottomCallback mOnBottomCallback;

    public AdvancedRecyclerView(Context context) {
        super(context);
    }

    public AdvancedRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AdvancedRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setOnBottomCallback(OnBottomCallback onBottomCallback) {
        this.mOnBottomCallback = onBottomCallback;
    }

    @SuppressWarnings("SameParameterValue")
    private boolean isSlideToNearBottom(int offset) {
        return this.computeVerticalScrollExtent() + this.computeVerticalScrollOffset() + offset >= this.computeVerticalScrollRange();
    }

    @Override
    public void setAdapter(Adapter adapter) {
        final Adapter oldAdapter = getAdapter();
        if (oldAdapter != null) {
            oldAdapter.unregisterAdapterDataObserver(observer);
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer);
        }

        checkIfEmpty();
    }

    @Override
    public void onScrolled(int dx, int dy) {
        if (useBottomListener && mOnBottomCallback != null) {
            if (isSlideToNearBottom(5)) {
                mOnBottomCallback.onBottom();
            } else {
                mOnBottomCallback.onNotBottom();
            }
        }
        super.onScrolled(dx, dy);
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
        checkIfEmpty();
    }

    private void checkIfEmpty() {
        if (emptyView != null && getAdapter() != null) {
            final boolean emptyViewVisible = getAdapter().getItemCount() == 0;
            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    public void setUseBottomListener(boolean useBottomListener) {
        this.useBottomListener = useBottomListener;
    }

    public interface OnBottomCallback {
        void onBottom();

        void onNotBottom();
    }
}
