package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import tool.xfy9326.naucourse.R;

class CourseViewHolder extends RecyclerView.ViewHolder {
    final TextView textView_course_name;
    final Button button_course_edit;
    final Button button_course_delete;
    final Button button_course_color;
    final TextView textView_course_edit_teacher;
    final CardView cardView_course_edit;

    CourseViewHolder(View view) {
        super(view);
        textView_course_name = view.findViewById(R.id.textView_course_edit_name);
        button_course_edit = view.findViewById(R.id.button_course_edit);
        button_course_delete = view.findViewById(R.id.button_course_delete);
        button_course_color = view.findViewById(R.id.button_course_color);
        textView_course_edit_teacher = view.findViewById(R.id.textView_course_edit_teacher);
        cardView_course_edit = view.findViewById(R.id.cardView_course_edit_item);
    }
}
