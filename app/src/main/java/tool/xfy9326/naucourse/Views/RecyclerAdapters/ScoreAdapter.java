package tool.xfy9326.naucourse.Views.RecyclerAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.CourseScore;

/**
 * Created by 10696 on 2018/3/2.
 */

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ScoreViewHolder> {
    private final Context context;
    private CourseScore courseScore;

    public ScoreAdapter(Context context, CourseScore courseScore) {
        this.context = context;
        this.courseScore = courseScore;
    }

    public void updateData(CourseScore courseScore) {
        this.courseScore = courseScore;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ScoreViewHolder holder, int position) {
        holder.textView_score_course_name.setText(context.getString(R.string.score_course_name, Objects.requireNonNull(courseScore.getScoreCourseName())[holder.getAdapterPosition()]));
        holder.textView_score_course_xf.setText(context.getString(R.string.course_card_score, courseScore.getScoreCourseXf()[holder.getAdapterPosition()]));
        holder.textView_score_common.setText(context.getString(R.string.score_common, Objects.requireNonNull(courseScore.getScoreCommon())[holder.getAdapterPosition()]));
        holder.textView_score_mid.setText(context.getString(R.string.score_mid, Objects.requireNonNull(courseScore.getScoreMid())[holder.getAdapterPosition()]));
        holder.textView_score_final.setText(context.getString(R.string.score_final, Objects.requireNonNull(courseScore.getScoreFinal())[holder.getAdapterPosition()]));
        holder.textView_score_total.setText(context.getString(R.string.score_total, Objects.requireNonNull(courseScore.getScoreTotal())[holder.getAdapterPosition()]));
    }

    @Override
    public int getItemCount() {
        return courseScore.getCourseAmount();
    }

    @NonNull
    @Override
    public ScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_score_card, parent, false);
        return new ScoreViewHolder(view);
    }

    /**
     * Created by 10696 on 2018/3/2.
     */

    static class ScoreViewHolder extends RecyclerView.ViewHolder {
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
}
