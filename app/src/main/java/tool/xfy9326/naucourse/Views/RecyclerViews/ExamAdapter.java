package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Exam;

/**
 * Created by 10696 on 2018/3/3.
 */

public class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.ExamViewHolder> {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm", Locale.CHINA);
    private final Context context;
    private Exam exam;

    public ExamAdapter(Context context, Exam exam) {
        this.context = context;
        this.exam = exam;
        updateTimeText();
    }

    public void updateData(Exam exam) {
        this.exam = exam;
        updateTimeText();
        notifyDataSetChanged();
    }

    private void updateTimeText() {
        if (exam.getExamMount() > 0) {
            long now = System.currentTimeMillis() / 1000L;
            String[] lastTime = new String[exam.getExamMount()];
            String[] lastTimeUnit = new String[exam.getExamMount()];
            for (int i = 0; i < exam.getExamMount(); i++) {
                String time = Objects.requireNonNull(exam.getExamTime())[i];
                if (time.contains("-")) {
                    time = time.substring(0, time.indexOf("-"));
                    try {
                        long examTime = simpleDateFormat.parse(time).getTime() / 1000L;
                        if (examTime > now) {
                            long day = (examTime - now) / (3600 * 24);
                            if (day > 0) {
                                lastTime[i] = String.valueOf(day);
                                lastTimeUnit[i] = context.getString(R.string.day);
                            } else {
                                long hour = (examTime - now) / 3600;
                                if (hour > 0) {
                                    lastTime[i] = String.valueOf(hour);
                                    lastTimeUnit[i] = context.getString(R.string.hour);
                                } else {
                                    long minute = (examTime - now) / 60;
                                    if (minute > 0) {
                                        lastTime[i] = String.valueOf(minute);
                                        lastTimeUnit[i] = context.getString(R.string.minute);
                                    } else {
                                        lastTime[i] = String.valueOf(0);
                                        lastTimeUnit[i] = context.getString(R.string.minute);
                                    }
                                }
                            }
                        } else {
                            lastTime[i] = String.valueOf(0);
                            lastTimeUnit[i] = context.getString(R.string.minute);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            this.exam.setLast_time(lastTime);
            this.exam.setLast_time_unit(lastTimeUnit);
            System.gc();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ExamViewHolder holder, int position) {
        holder.textView_exam_name.setText(context.getString(R.string.score_course_name, Objects.requireNonNull(exam.getExamName())[holder.getAdapterPosition()]));
        holder.textView_exam_type.setText(context.getString(R.string.exam_type, Objects.requireNonNull(exam.getExamType())[holder.getAdapterPosition()]));
        holder.textView_exam_score.setText(context.getString(R.string.course_card_score, Objects.requireNonNull(exam.getExamScore())[holder.getAdapterPosition()]));
        holder.textView_exam_time.setText(context.getString(R.string.exam_time, Objects.requireNonNull(exam.getExamTime())[holder.getAdapterPosition()]));
        holder.textView_exam_location.setText(context.getString(R.string.exam_location, Objects.requireNonNull(exam.getExamLocation())[holder.getAdapterPosition()]));

        String time = Objects.requireNonNull(exam.getLast_time())[holder.getAdapterPosition()];
        String unit = Objects.requireNonNull(exam.getLast_time_unit())[holder.getAdapterPosition()];
        if (time != null && unit != null) {
            holder.textView_exam_last_time.setText(time);
            holder.textView_exam_last_time_unit.setText(unit);
            holder.linearLayout_exam_last_time.setVisibility(View.VISIBLE);
        } else {
            holder.linearLayout_exam_last_time.setVisibility(View.GONE);
        }
    }

    @NonNull
    @Override
    public ExamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_exam_card, parent, false);
        return new ExamViewHolder(view);
    }

    public void clearAdapter() {
        this.exam.setExamMount(0);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return exam.getExamMount();
    }

    /**
     * Created by 10696 on 2018/3/3.
     */

    static class ExamViewHolder extends RecyclerView.ViewHolder {
        final TextView textView_exam_name;
        final TextView textView_exam_type;
        final TextView textView_exam_score;
        final TextView textView_exam_time;
        final TextView textView_exam_location;
        final TextView textView_exam_last_time;
        final TextView textView_exam_last_time_unit;
        final LinearLayout linearLayout_exam_last_time;

        ExamViewHolder(@NonNull View view) {
            super(view);
            textView_exam_name = view.findViewById(R.id.textView_exam_name);
            textView_exam_type = view.findViewById(R.id.textView_exam_type);
            textView_exam_score = view.findViewById(R.id.textView_exam_score);
            textView_exam_time = view.findViewById(R.id.textView_exam_time);
            textView_exam_location = view.findViewById(R.id.textView_exam_location);
            textView_exam_last_time = view.findViewById(R.id.textView_exam_last_time);
            textView_exam_last_time_unit = view.findViewById(R.id.textView_exam_last_time_unit);
            linearLayout_exam_last_time = view.findViewById(R.id.layout_exam_last_time);
        }
    }
}
