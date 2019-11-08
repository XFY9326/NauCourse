package tool.xfy9326.naucourse.views.recyclerAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.beans.score.CourseScore;

/**
 * Created by 10696 on 2018/3/2.
 */

public class CurrentScoreAdapter extends RecyclerView.Adapter<CurrentScoreAdapter.CurrentScoreViewHolder> {
    private final Context context;
    private CourseScore courseScore;

    public CurrentScoreAdapter(Context context) {
        this(context, new CourseScore());
    }

    public CurrentScoreAdapter(Context context, CourseScore courseScore) {
        this.context = context;
        this.courseScore = courseScore;
    }

    public void updateData(CourseScore courseScore) {
        this.courseScore = courseScore;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentScoreViewHolder holder, int position) {
        int index = holder.getAdapterPosition();
        holder.textView_score_course_name.setText(context.getString(R.string.score_course_name, Objects.requireNonNull(courseScore.getScoreCourseName())[index]));
        holder.textView_score_course_xf.setText(context.getString(R.string.course_card_score, Objects.requireNonNull(courseScore.getScoreCourseXf())[index]));
        holder.textView_score_common.setText(context.getString(R.string.score_common, Objects.requireNonNull(courseScore.getScoreCommon())[index]));
        holder.textView_score_mid.setText(context.getString(R.string.score_mid, Objects.requireNonNull(courseScore.getScoreMid())[index]));
        holder.textView_score_final.setText(context.getString(R.string.score_final, Objects.requireNonNull(courseScore.getScoreFinal())[index]));
        holder.textView_score_total.setText(context.getString(R.string.score_total, Objects.requireNonNull(courseScore.getScoreTotal())[index]));
    }

    @Override
    public int getItemCount() {
        return courseScore.getCourseAmount();
    }

    @NonNull
    @Override
    public CurrentScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_current_score_card, parent, false);
        return new CurrentScoreViewHolder(view);
    }

    /**
     * Created by 10696 on 2018/3/2.
     */

    static class CurrentScoreViewHolder extends RecyclerView.ViewHolder {
        final TextView textView_score_course_name;
        final TextView textView_score_course_xf;
        final TextView textView_score_common;
        final TextView textView_score_mid;
        final TextView textView_score_final;
        final TextView textView_score_total;

        CurrentScoreViewHolder(@NonNull View view) {
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
