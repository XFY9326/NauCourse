package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tool.xfy9326.naucourse.R;

class LevelExamViewHolder extends RecyclerView.ViewHolder {
    final TextView textView_level_exam_name;
    final TextView textView_level_exam_type;
    final TextView textView_level_exam_term;
    final TextView textView_level_exam_score_one;
    final TextView textView_level_exam_score_two;
    final TextView textView_level_exam_ticket;
    final TextView textView_level_exam_certificate;

    LevelExamViewHolder(@NonNull View view) {
        super(view);
        textView_level_exam_name = view.findViewById(R.id.textView_level_exam_name);
        textView_level_exam_type = view.findViewById(R.id.textView_level_exam_type);
        textView_level_exam_term = view.findViewById(R.id.textView_level_exam_term);
        textView_level_exam_score_one = view.findViewById(R.id.textView_level_exam_score_one);
        textView_level_exam_score_two = view.findViewById(R.id.textView_level_exam_score_two);
        textView_level_exam_ticket = view.findViewById(R.id.textView_level_exam_ticket);
        textView_level_exam_certificate = view.findViewById(R.id.textView_level_exam_certificate);

    }
}
