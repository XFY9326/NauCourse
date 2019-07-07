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
import tool.xfy9326.naucourse.utils.HistoryScore;

public class HistoryScoreAdapter extends RecyclerView.Adapter<HistoryScoreAdapter.HistoryScoreViewHolder> {
    private final Context context;
    private HistoryScore historyScore;

    public HistoryScoreAdapter(Context context) {
        this(context, new HistoryScore());
    }

    public HistoryScoreAdapter(Context context, HistoryScore historyScore) {
        this.context = context;
        this.historyScore = historyScore;
    }

    public void updateData(HistoryScore historyScore) {
        this.historyScore = historyScore;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryScoreViewHolder holder, int position) {
        int index = holder.getAdapterPosition();
        holder.textView_score_course_name.setText(context.getString(R.string.score_course_name, Objects.requireNonNull(historyScore.getName())[index]));
        holder.textView_score_course_xf.setText(context.getString(R.string.course_card_score, Objects.requireNonNull(historyScore.getStudyScore())[index]));
        String type = Objects.requireNonNull(historyScore.getCourseProperty())[index] + " " + Objects.requireNonNull(historyScore.getCourseType())[index];
        holder.textView_score_course_type.setText(context.getString(R.string.course_card_type, type));
        holder.textView_score_credit_weight.setText(context.getString(R.string.credit_weight, Objects.requireNonNull(historyScore.getCreditWeight())[index]));
        holder.textView_score_total.setText(context.getString(R.string.score_total, Objects.requireNonNull(historyScore.getScore())[index]));
        holder.textView_score_term.setText(context.getString(R.string.course_term, Objects.requireNonNull(historyScore.getTerm())[index]));
    }

    @Override
    public int getItemCount() {
        return historyScore.getCourseAmount();
    }

    @NonNull
    @Override
    public HistoryScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_history_score_card, parent, false);
        return new HistoryScoreViewHolder(view);
    }

    static class HistoryScoreViewHolder extends RecyclerView.ViewHolder {
        final TextView textView_score_course_name;
        final TextView textView_score_course_xf;
        final TextView textView_score_course_type;
        final TextView textView_score_credit_weight;
        final TextView textView_score_total;
        final TextView textView_score_term;

        HistoryScoreViewHolder(@NonNull View view) {
            super(view);
            textView_score_course_name = view.findViewById(R.id.textView_score_course_name);
            textView_score_course_xf = view.findViewById(R.id.textView_score_course_xf);
            textView_score_course_type = view.findViewById(R.id.textView_score_course_type);
            textView_score_credit_weight = view.findViewById(R.id.textView_score_credit_weight);
            textView_score_total = view.findViewById(R.id.textView_score_total);
            textView_score_term = view.findViewById(R.id.textView_score_term);
        }
    }
}
