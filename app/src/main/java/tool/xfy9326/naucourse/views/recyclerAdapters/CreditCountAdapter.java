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
import java.util.Arrays;
import java.util.Objects;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.utils.CourseScore;
import tool.xfy9326.naucourse.utils.CreditCountCourse;

public class CreditCountAdapter extends RecyclerView.Adapter<CreditCountAdapter.CreditCountViewHolder> {
    private final Context context;
    private final ArrayList<String> nameList;
    private final ArrayList<Float> scoreList;
    private final ArrayList<Float> studyScoreList;
    private final boolean[] checkedList;
    private final float[] weightList;

    public CreditCountAdapter(Context context, CourseScore courseScore) {
        this.context = context;
        nameList = new ArrayList<>();
        scoreList = new ArrayList<>();
        studyScoreList = new ArrayList<>();
        courseScoreFix(courseScore);
        this.checkedList = new boolean[getItemCount()];
        this.weightList = new float[getItemCount()];
        Arrays.fill(weightList, 1f);
    }

    private void courseScoreFix(CourseScore courseScore) {
        for (int i = 0; i < courseScore.getCourseAmount(); i++) {
            if (!Objects.requireNonNull(courseScore.getScoreTotal())[i].contains("未")) {
                try {
                    float score = Float.parseFloat(Objects.requireNonNull(courseScore.getScoreTotal())[i]);
                    float studyScore = Float.parseFloat(Objects.requireNonNull(courseScore.getScoreCourseXf())[i]);
                    nameList.add(Objects.requireNonNull(courseScore.getScoreCourseName())[i]);
                    scoreList.add(score);
                    studyScoreList.add(studyScore);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<CreditCountCourse> getResult() {
        ArrayList<CreditCountCourse> courses = new ArrayList<>();
        for (int i = 0; i < getItemCount(); i++) {
            if (checkedList[i]) {
                CreditCountCourse course = new CreditCountCourse();
                course.setScore(scoreList.get(i));
                course.setStudyScore(studyScoreList.get(i));
                course.setCreditWeight(weightList[i]);
                courses.add(course);
            }
        }
        return courses;
    }

    @Override
    public void onBindViewHolder(@NonNull CreditCountViewHolder holder, int position) {
        holder.cardViewItem.setOnClickListener(v -> holder.checkBoxCourse.setChecked(!holder.checkBoxCourse.isChecked()));
        if (nameList != null && scoreList != null && studyScoreList != null) {
            holder.checkBoxCourse.setText(nameList.get(holder.getAdapterPosition()));
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
                        weightList[holder.getAdapterPosition()] = Float.parseFloat(str);
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
        return nameList.size();
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
