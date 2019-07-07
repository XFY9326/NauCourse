package tool.xfy9326.naucourse.views.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.utils.CourseScore;
import tool.xfy9326.naucourse.utils.CreditCountCourse;

public class CreditCountAdapter extends BaseAdapter {
    private final Context context;
    private final LayoutInflater inflater;
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
        this.checkedList = new boolean[getCount()];
        this.weightList = new float[getCount()];
        Arrays.fill(weightList, 1f);
        this.inflater = LayoutInflater.from(context);
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
        for (int i = 0; i < getCount(); i++) {
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
    public Object getItem(int position) {
        return nameList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        CreditCountViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new CreditCountViewHolder();
            convertView = inflater.inflate(R.layout.item_credit_course, null);
            viewHolder.checkBoxCourse = convertView.findViewById(R.id.checkBox_credit_course);
            viewHolder.editTextCreditWeight = convertView.findViewById(R.id.editText_credit_weight);
            viewHolder.cardViewItem = convertView.findViewById(R.id.cardView_credit_course_item);
            if (nameList != null && scoreList != null && studyScoreList != null) {
                viewHolder.cardViewItem.setOnClickListener(v -> viewHolder.checkBoxCourse.setChecked(!viewHolder.checkBoxCourse.isChecked()));
                viewHolder.checkBoxCourse.setText(nameList.get(position));
                viewHolder.checkBoxCourse.setOnCheckedChangeListener((buttonView, isChecked) -> checkedList[position] = isChecked);
                viewHolder.editTextCreditWeight.addTextChangedListener(new TextWatcher() {
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
                            weightList[position] = Float.parseFloat(str);
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            Toast.makeText(context, R.string.credit_weight_needed, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return nameList.size();
    }

    static class CreditCountViewHolder {
        CardView cardViewItem;
        CheckBox checkBoxCourse;
        EditText editTextCreditWeight;
    }
}
