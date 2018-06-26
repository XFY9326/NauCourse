package tool.xfy9326.naucourse.Views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tool.xfy9326.naucourse.Activities.SuspendCourseActivity;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.SuspendCourse;

public class SuspendCourseAdapter extends RecyclerView.Adapter<SuspendCourseViewHolder> {
    private final SuspendCourseActivity activity;
    private SuspendCourse suspendCourse;

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
}
