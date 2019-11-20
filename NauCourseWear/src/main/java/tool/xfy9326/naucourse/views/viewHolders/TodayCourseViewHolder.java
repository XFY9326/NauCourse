package tool.xfy9326.naucourse.views.viewHolders;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.wear.widget.BoxInsetLayout;

import tool.xfy9326.naucourse.R;

public class TodayCourseViewHolder extends RecyclerView.ViewHolder {
    public static final int VIEW_TYPE = 0;
    public final AppCompatTextView textView_course_info;
    public final AppCompatTextView textView_course_time;
    public final BoxInsetLayout layout_box_border;

    public TodayCourseViewHolder(View view) {
        super(view);
        textView_course_info = view.findViewById(R.id.textView_course_info);
        textView_course_time = view.findViewById(R.id.textView_course_time);
        layout_box_border = view.findViewById(R.id.layout_box_border);
    }
}
