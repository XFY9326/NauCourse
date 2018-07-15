package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.LevelExam;

public class LevelExamAdapter extends RecyclerView.Adapter<LevelExamViewHolder> {
    private final Context context;
    private LevelExam levelExam;

    public LevelExamAdapter(Context context, LevelExam levelExam) {
        this.context = context;
        this.levelExam = levelExam;
    }

    public void updateData(LevelExam levelExam) {
        this.levelExam = levelExam;
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

}
