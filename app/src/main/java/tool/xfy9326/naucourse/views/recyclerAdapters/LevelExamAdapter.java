package tool.xfy9326.naucourse.views.recyclerAdapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.utils.LevelExam;

public class LevelExamAdapter extends RecyclerView.Adapter<LevelExamAdapter.LevelExamViewHolder> {
    private final Context context;
    private LevelExam levelExam;

    public LevelExamAdapter(Context context) {
        this(context, new LevelExam());
    }

    public LevelExamAdapter(Context context, LevelExam levelExam) {
        this.context = context;
        this.levelExam = levelExam;
    }

    public void updateData(LevelExam levelExam) {
        this.levelExam = levelExam;
        notifyDataSetChanged();
    }

    public void clearAdapter() {
        this.levelExam.setExamAmount(0);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull LevelExamViewHolder holder, int position) {
        holder.textView_level_exam_name.setText(context.getString(R.string.level_exam_name, levelExam.getExamName()[holder.getAdapterPosition()]));
        holder.textView_level_exam_type.setText(context.getString(R.string.level_exam_type, levelExam.getExamType()[holder.getAdapterPosition()]));
        holder.textView_level_exam_term.setText(context.getString(R.string.level_exam_term, levelExam.getTerm()[holder.getAdapterPosition()]));
        holder.textView_level_exam_score_one.setText(context.getString(R.string.level_exam_score_one, levelExam.getScore1()[holder.getAdapterPosition()]));
        holder.textView_level_exam_score_two.setText(context.getString(R.string.level_exam_score_two, levelExam.getScore2()[holder.getAdapterPosition()]));
        holder.textView_level_exam_ticket.setText(context.getString(R.string.level_exam_ticket, levelExam.getTicketId()[holder.getAdapterPosition()]));
        holder.textView_level_exam_certificate.setText(context.getString(R.string.level_exam_certificate, levelExam.getCertificateId()[holder.getAdapterPosition()]));
    }

    @NonNull
    @Override
    public LevelExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_level_exam, parent, false);
        return new LevelExamViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return levelExam.getExamAmount();
    }

    static class LevelExamViewHolder extends RecyclerView.ViewHolder {
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
}
