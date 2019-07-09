package tool.xfy9326.naucourse.views.recyclerAdapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.utils.CourseScore;
import tool.xfy9326.naucourse.utils.CreditCountCourse;

public class CreditCountAdapter extends RecyclerView.Adapter<CreditCountAdapter.CreditCountViewHolder> {
    private final Context context;
    private final ArrayList<CreditCountCourse> countCourses;
    private final boolean[] checkedList;

    public CreditCountAdapter(Context context, CourseScore courseScore) {
        this.context = context;
        this.countCourses = new ArrayList<>();
        courseScoreFix(courseScore);
        this.checkedList = new boolean[getItemCount()];
    }

    private void courseScoreFix(CourseScore courseScore) {
        for (int i = 0; i < courseScore.getCourseAmount(); i++) {
            if (!Objects.requireNonNull(courseScore.getScoreTotal())[i].contains("未")) {
                try {
                    CreditCountCourse course = new CreditCountCourse();
                    float score = Float.parseFloat(Objects.requireNonNull(courseScore.getScoreTotal())[i]);
                    float studyScore = Float.parseFloat(Objects.requireNonNull(courseScore.getScoreCourseXf())[i]);
                    course.setCourseId(Objects.requireNonNull(courseScore.getScoreCourseId())[i]);
                    course.setCourseName(Objects.requireNonNull(courseScore.getScoreCourseName())[i]);
                    course.setScore(score);
                    course.setStudyScore(studyScore);
                    course.setCreditWeight(1f);
                    countCourses.add(course);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<CreditCountCourse> getResult() {
        ArrayList<CreditCountCourse> result = new ArrayList<>();
        for (int i = 0; i < countCourses.size(); i++) {
            if (checkedList[i]) {
                result.add(countCourses.get(i));
            }
        }
        return result;
    }

    @Override
    public void onBindViewHolder(@NonNull CreditCountViewHolder holder, int position) {
        holder.cardViewItem.setOnClickListener(v -> holder.checkBoxCourse.setChecked(!holder.checkBoxCourse.isChecked()));
        final CreditCountCourse course = countCourses.get(holder.getAdapterPosition());
        if (course != null) {
            holder.checkBoxCourse.setText(course.getCourseName());
            holder.checkBoxCourse.setOnCheckedChangeListener((buttonView, isChecked) -> checkedList[holder.getAdapterPosition()] = isChecked);
            holder.editTextCreditWeight.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String str = s.toString();
                    try {
                        course.setCreditWeight(Float.parseFloat(str));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        Toast.makeText(context, R.string.credit_weight_needed, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public CreditCountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_credit_course, parent, false);
        return new CreditCountViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return countCourses.size();
    }

    static class CreditCountViewHolder extends RecyclerView.ViewHolder {
        final CheckBox checkBoxCourse;
        final EditText editTextCreditWeight;
        final CardView cardViewItem;

        CreditCountViewHolder(View view) {
            super(view);
            checkBoxCourse = view.findViewById(R.id.checkBox_credit_course);
            editTextCreditWeight = view.findViewById(R.id.editText_credit_weight);
            cardViewItem = view.findViewById(R.id.cardView_credit_course_item);
        }
    }
}
