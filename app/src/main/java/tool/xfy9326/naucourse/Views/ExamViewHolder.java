package tool.xfy9326.naucourse.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tool.xfy9326.naucourse.R;

/**
 * Created by 10696 on 2018/3/3.
 */

class ExamViewHolder extends RecyclerView.ViewHolder {
    final TextView textView_exam_name;
    final TextView textView_exam_type;
    final TextView textView_exam_score;
    final TextView textView_exam_time;
    final TextView textView_exam_location;

    ExamViewHolder(View view) {
        super(view);
        textView_exam_name = view.findViewById(R.id.textView_exam_name);
        textView_exam_type = view.findViewById(R.id.textView_exam_type);
        textView_exam_score = view.findViewById(R.id.textView_exam_score);
        textView_exam_time = view.findViewById(R.id.textView_exam_time);
        textView_exam_location = view.findViewById(R.id.textView_exam_location);
    }
}
