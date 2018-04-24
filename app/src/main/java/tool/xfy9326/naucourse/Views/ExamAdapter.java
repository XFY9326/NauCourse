package tool.xfy9326.naucourse.Views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Exam;

/**
 * Created by 10696 on 2018/3/3.
 */

public class ExamAdapter extends RecyclerView.Adapter<ExamViewHolder> {
    private final Context context;
    private Exam exam;

    public ExamAdapter(Context context, Exam exam) {
        this.context = context;
        this.exam = exam;
    }

    public void updateData(Exam exam) {
        this.exam = exam;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        holder.textView_exam_name.setText(context.getString(R.string.score_course_name, Objects.requireNonNull(exam.getExamName())[holder.getAdapterPosition()]));
        holder.textView_exam_type.setText(context.getString(R.string.exam_type, Objects.requireNonNull(exam.getExamType())[holder.getAdapterPosition()]));
        holder.textView_exam_score.setText(context.getString(R.string.course_card_score, Objects.requireNonNull(exam.getExamScore())[holder.getAdapterPosition()]));
        holder.textView_exam_time.setText(context.getString(R.string.exam_time, Objects.requireNonNull(exam.getExamTime())[holder.getAdapterPosition()]));
        holder.textView_exam_location.setText(context.getString(R.string.exam_location, Objects.requireNonNull(exam.getExamLocation())[holder.getAdapterPosition()]));
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_exam_card, parent, false);
        return new ExamViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return exam.getExamMount();
    }
}
