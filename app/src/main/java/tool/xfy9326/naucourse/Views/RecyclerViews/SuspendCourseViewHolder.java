package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tool.xfy9326.naucourse.R;

class SuspendCourseViewHolder extends RecyclerView.ViewHolder {
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
