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

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.utils.CreditCountCourse;

public class CreditCountAdapter extends RecyclerView.Adapter<CreditCountAdapter.CreditCountViewHolder> {
    private final Context context;
    private final ArrayList<CreditCountCourse> countCourses;
    private final boolean[] checkedList;

    public CreditCountAdapter(Context context, ArrayList<CreditCountCourse> countCourses) {
        this.context = context;
        this.countCourses = countCourses;
        this.checkedList = new boolean[getItemCount()];
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
