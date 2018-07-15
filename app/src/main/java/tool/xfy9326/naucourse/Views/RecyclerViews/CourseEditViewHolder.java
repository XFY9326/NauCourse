package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import tool.xfy9326.naucourse.R;

class CourseEditViewHolder extends RecyclerView.ViewHolder {
    final Button button_delete;
    final CheckBox checkBox_course_edit_single_week;
    final CheckBox checkBox_course_edit_double_week;
    final EditText editText_course_edit_week_num;
    final Button button_course_edit_week_num;
    final Spinner spinner_course_edit_week;
    final EditText editText_course_edit_time;
    final Button button_course_edit_time;
    final EditText editText_course_edit_location;
    final Button button_course_edit_location;

    CourseEditViewHolder(View view) {
        super(view);
        button_delete = view.findViewById(R.id.button_course_edit_time_delete);
        checkBox_course_edit_single_week = view.findViewById(R.id.checkBox_course_edit_single_week);
        checkBox_course_edit_double_week = view.findViewById(R.id.checkBox_course_edit_double_week);
        editText_course_edit_week_num = view.findViewById(R.id.editText_course_edit_week_num);
        button_course_edit_week_num = view.findViewById(R.id.button_course_edit_week_num);
        spinner_course_edit_week = view.findViewById(R.id.spinner_course_edit_week);
        editText_course_edit_time = view.findViewById(R.id.editText_course_edit_time);
        button_course_edit_time = view.findViewById(R.id.button_course_edit_time);
        editText_course_edit_location = view.findViewById(R.id.editText_course_edit_location);
        button_course_edit_location = view.findViewById(R.id.button_course_edit_location);
    }
}
