package tool.xfy9326.naucourse.views.viewHolders;

import android.view.View;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;

import tool.xfy9326.naucourse.R;

public class PreScrollViewHolder extends RecyclerView.ViewHolder {
    public static final int VIEW_TYPE = 3;
    public final FrameLayout layout_empty;

    public PreScrollViewHolder(View view) {
        super(view);
        layout_empty = view.findViewById(R.id.layout_empty);
    }
}
