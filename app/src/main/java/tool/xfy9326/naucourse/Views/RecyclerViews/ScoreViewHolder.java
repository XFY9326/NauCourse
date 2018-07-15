package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tool.xfy9326.naucourse.R;

/**
 * Created by 10696 on 2018/3/2.
 */

class ScoreViewHolder extends RecyclerView.ViewHolder {
    final TextView textView_score_course_name;
    final TextView textView_score_course_xf;
    final TextView textView_score_common;
    final TextView textView_score_mid;
    final TextView textView_score_final;
    final TextView textView_score_total;

    ScoreViewHolder(@NonNull View view) {
        super(view);
        textView_score_course_name = view.findViewById(R.id.textView_score_course_name);
        textView_score_course_xf = view.findViewById(R.id.textView_score_course_xf);
        textView_score_common = view.findViewById(R.id.textView_score_common);
        textView_score_mid = view.findViewById(R.id.textView_score_mid);
        textView_score_final = view.findViewById(R.id.textView_score_final);
        textView_score_total = view.findViewById(R.id.textView_score_total);
    }
}
