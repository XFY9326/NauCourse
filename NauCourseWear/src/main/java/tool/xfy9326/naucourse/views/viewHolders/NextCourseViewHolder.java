package tool.xfy9326.naucourse.views.viewHolders;

import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import tool.xfy9326.naucourse.R;

public class NextCourseViewHolder extends RecyclerView.ViewHolder {
    public static final int VIEW_TYPE = 1;
    public final AppCompatTextView textView_next_course_name;
    public final AppCompatTextView textView_next_course_location;
    public final AppCompatTextView textView_next_course_time;

    public NextCourseViewHolder(View view) {
        super(view);
        textView_next_course_name = view.findViewById(R.id.textView_next_course_name);
        textView_next_course_location = view.findViewById(R.id.textView_next_course_location);
        textView_next_course_time = view.findViewById(R.id.textView_next_course_time);
    }
}
