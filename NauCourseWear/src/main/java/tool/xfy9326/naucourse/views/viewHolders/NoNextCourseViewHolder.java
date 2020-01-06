package tool.xfy9326.naucourse.views.viewHolders;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.BoxInsetLayout;

import tool.xfy9326.naucourse.R;

public class NoNextCourseViewHolder extends RecyclerView.ViewHolder {
    public static final int VIEW_TYPE = 2;
    public final BoxInsetLayout layout_box_border;

    public NoNextCourseViewHolder(View view) {
        super(view);
        layout_box_border = view.findViewById(R.id.layout_box_border);
    }
}
