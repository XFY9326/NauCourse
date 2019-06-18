package tool.xfy9326.naucourse.views.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.SuspendCourseActivity;
import tool.xfy9326.naucourse.utils.SuspendCourse;

public class SuspendCourseAdapter extends RecyclerView.Adapter<SuspendCourseAdapter.SuspendCourseViewHolder> {
    private final SuspendCourseActivity activity;
    private SuspendCourse suspendCourse;

    public SuspendCourseAdapter(SuspendCourseActivity activity) {
        this(activity, new SuspendCourse());
    }

    public SuspendCourseAdapter(SuspendCourseActivity activity, SuspendCourse suspendCourse) {
        this.activity = activity;
        this.suspendCourse = suspendCourse;
    }

    public void clearAdapter() {
        this.suspendCourse.setCount(0);
        notifyDataSetChanged();
    }

    public void updateSuspendCourse(SuspendCourse suspendCourse) {
        this.suspendCourse = suspendCourse;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull SuspendCourseViewHolder holder, int position) {
        holder.textViewSuspendCourseName.setText(suspendCourse.getName()[holder.getAdapterPosition()]);
        holder.textViewSuspendCourseClass.setText(activity.getString(R.string.suspend_course_class_class, suspendCourse.getCourse()[holder.getAdapterPosition()]));
        holder.textViewSuspendCourseTeacher.setText(activity.getString(R.string.suspend_course_class_teacher, suspendCourse.getTeacher()[holder.getAdapterPosition()]));

        String[] type = suspendCourse.getDetail_type()[holder.getAdapterPosition()];

        if (type.length >= 2) {
            String[] course = suspendCourse.getDetail_class()[holder.getAdapterPosition()];
            String[] date = suspendCourse.getDetail_date()[holder.getAdapterPosition()];
            String[] location = suspendCourse.getDetail_location()[holder.getAdapterPosition()];

            holder.textViewSuspendCourseType1.setText(type[0]);
            holder.textViewSuspendCourseDate1.setText(activity.getString(R.string.suspend_course_class_date, date[0]));
            holder.textViewSuspendCourseTime1.setText(activity.getString(R.string.suspend_course_class_time, course[0]));
            holder.textViewSuspendCourseLocation1.setText(activity.getString(R.string.suspend_course_class_location, location[0]));

            holder.textViewSuspendCourseType2.setText(type[1]);
            holder.textViewSuspendCourseDate2.setText(activity.getString(R.string.suspend_course_class_date, date[1]));
            holder.textViewSuspendCourseTime2.setText(activity.getString(R.string.suspend_course_class_time, course[1]));
            holder.textViewSuspendCourseLocation2.setText(activity.getString(R.string.suspend_course_class_location, location[1]));
        }
    }

    @NonNull
    @Override
    public SuspendCourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_suspend_course, parent, false);
        return new SuspendCourseViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return suspendCourse.getCount();
    }

    static class SuspendCourseViewHolder extends RecyclerView.ViewHolder {
        final TextView textViewSuspendCourseName;
        final TextView textViewSuspendCourseClass;
        final TextView textViewSuspendCourseTeacher;
        final TextView textViewSuspendCourseType1;
        final TextView textViewSuspendCourseDate1;
        final TextView textViewSuspendCourseTime1;
        final TextView textViewSuspendCourseLocation1;
        final TextView textViewSuspendCourseType2;
        final TextView textViewSuspendCourseDate2;
        final TextView textViewSuspendCourseTime2;
        final TextView textViewSuspendCourseLocation2;

        SuspendCourseViewHolder(View view) {
            super(view);
            textViewSuspendCourseName = view.findViewById(R.id.textView_suspend_course_name);
            textViewSuspendCourseClass = view.findViewById(R.id.textView_suspend_course_class);
            textViewSuspendCourseTeacher = view.findViewById(R.id.textView_suspend_course_teacher);
            textViewSuspendCourseType1 = view.findViewById(R.id.textView_suspend_course_type_1);
            textViewSuspendCourseDate1 = view.findViewById(R.id.textView_suspend_course_date_1);
            textViewSuspendCourseTime1 = view.findViewById(R.id.textView_suspend_course_time_1);
            textViewSuspendCourseLocation1 = view.findViewById(R.id.textView_suspend_course_location_1);
            textViewSuspendCourseType2 = view.findViewById(R.id.textView_suspend_course_type_2);
            textViewSuspendCourseDate2 = view.findViewById(R.id.textView_suspend_course_date_2);
            textViewSuspendCourseTime2 = view.findViewById(R.id.textView_suspend_course_time_2);
            textViewSuspendCourseLocation2 = view.findViewById(R.id.textView_suspend_course_location_2);
        }
    }
}
