package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.RecyclerView;
import tool.xfy9326.naucourse.Activities.CourseEditActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.CourseEditMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.CourseDetail;

public class CourseEditAdapter extends RecyclerView.Adapter<CourseEditAdapter.CourseEditViewHolder> {
    private final CourseEditActivity activity;
    private ArrayList<CourseDetail> courseDetails;
    private String[] weekArray;

    public CourseEditAdapter(CourseEditActivity activity, ArrayList<CourseDetail> courseDetails) {
        this.activity = activity;
        this.courseDetails = courseDetails;
    }

    public void setData(ArrayList<CourseDetail> courseDetails) {
        this.courseDetails = courseDetails;
    }

    @Override
    public void onBindViewHolder(@NonNull final CourseEditViewHolder holder, int position) {
        //设置初始化的布局
        ArrayAdapter<String> adapter = new ArrayAdapter<>(activity, android.R.layout.simple_list_item_1, getWeekArray());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.spinner_course_edit_week.setAdapter(adapter);

        final CourseDetail courseDetail = courseDetails.get(holder.getAdapterPosition());
        if (courseDetail.getWeekDay() != 0) {
            holder.spinner_course_edit_week.setSelection(courseDetail.getWeekDay() - 1);
        } else {
            courseDetail.setWeekDay(1);
        }
        if (courseDetail.getWeeks() != null) {
            String week_num = Arrays.toString(courseDetail.getWeeks());
            holder.editText_course_edit_week_num.setText(week_num.substring(1, week_num.length() - 1));
        }
        if (courseDetail.getCourseTime() != null) {
            String course_time = Arrays.toString(courseDetail.getCourseTime());
            holder.editText_course_edit_time.setText(course_time.substring(1, course_time.length() - 1));
        }
        if (courseDetail.getLocation() != null) {
            holder.editText_course_edit_location.setText(courseDetail.getLocation());
        }
        switch (courseDetail.getWeekMode()) {
            case Config.COURSE_DETAIL_WEEKMODE_SINGLE:
                holder.checkBox_course_edit_single_week.setChecked(true);
                holder.checkBox_course_edit_double_week.setChecked(false);
                break;
            case Config.COURSE_DETAIL_WEEKMODE_DOUBLE:
                holder.checkBox_course_edit_single_week.setChecked(false);
                holder.checkBox_course_edit_double_week.setChecked(true);
                break;
            case Config.COURSE_DETAIL_WEEKMODE_ONCE_MORE:
                holder.checkBox_course_edit_single_week.setChecked(true);
                holder.checkBox_course_edit_double_week.setChecked(true);
                break;
            default:
                holder.checkBox_course_edit_single_week.setChecked(false);
                holder.checkBox_course_edit_double_week.setChecked(false);
                courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE);
                break;
        }

        holder.checkBox_course_edit_single_week.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (holder.checkBox_course_edit_double_week.isChecked()) {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE_MORE);
                    } else {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_SINGLE);
                    }
                } else {
                    if (holder.checkBox_course_edit_double_week.isChecked()) {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_DOUBLE);
                    } else {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE);
                    }
                }
                activity.setArrayChanged();
            }
        });
        holder.checkBox_course_edit_double_week.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (holder.checkBox_course_edit_single_week.isChecked()) {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE_MORE);
                    } else {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_DOUBLE);
                    }
                } else {
                    if (holder.checkBox_course_edit_single_week.isChecked()) {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_SINGLE);
                    } else {
                        courseDetail.setWeekMode(Config.COURSE_DETAIL_WEEKMODE_ONCE);
                    }
                }
                activity.setArrayChanged();
            }
        });

        holder.spinner_course_edit_week.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                courseDetail.setWeekDay(position + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        holder.button_course_edit_week_num.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNumberItem(holder.getAdapterPosition(), true);
            }
        });
        holder.button_course_edit_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editNumberItem(holder.getAdapterPosition(), false);
            }
        });
        holder.button_course_edit_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editLocation(holder.getAdapterPosition());
            }
        });
        holder.button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Snackbar.make(activity.findViewById(R.id.layout_course_edit_content), R.string.delete_confirm, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, new View.OnClickListener() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onClick(View v) {
                        courseDetails.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        activity.setArrayChanged();

                        FloatingActionButton floatingActionButton = activity.findViewById(R.id.floatingActionButton_course_edit_add);
                        if (floatingActionButton.getVisibility() != View.VISIBLE) {
                            floatingActionButton.setVisibility(View.VISIBLE);
                            ViewCompat.animate(floatingActionButton).scaleX(1.0F).scaleY(1.0F).alpha(1.0F)
                                    .setInterpolator(new FastOutSlowInInterpolator()).withLayer().setListener(null)
                                    .start();
                        }
                    }
                }).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return courseDetails.size();
    }

    @NonNull
    @Override
    public CourseEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_course_time_edit, parent, false);
        return new CourseEditViewHolder(view);
    }

    /**
     * 获取一个星期的数组
     *
     * @return 星期显示数组
     */
    private String[] getWeekArray() {
        if (weekArray == null) {
            weekArray = new String[Config.MAX_WEEK_DAY];
            String[] num = activity.getResources().getStringArray(R.array.week_number);
            for (int i = 0; i < Config.MAX_WEEK_DAY; i++) {
                weekArray[i] = activity.getString(R.string.week_day, num[i]);
            }
        }
        return weekArray;
    }

    //编辑上课地点
    private void editLocation(final int position) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_edit_text, (ViewGroup) activity.findViewById(R.id.layout_dialog_edit_text));

        final TextInputEditText editText = view.findViewById(R.id.editText_dialog_edit_text);
        if (courseDetails.get(position).getLocation() != null) {
            editText.setText(courseDetails.get(position).getLocation());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.course_edit_location);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Editable editable = editText.getText();
                if (editable != null) {
                    String str = editable.toString();
                    courseDetails.get(position).setLocation(str);
                    activity.setArrayChanged();
                    notifyItemChanged(position);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        builder.show();
    }

    //编辑上课时间与周数
    private void editNumberItem(final int position, final boolean isWeekNumber) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_course_time_set, (ViewGroup) activity.findViewById(R.id.layout_dialog_course_time_set));

        final EditText editText_show = view.findViewById(R.id.editText_course_edit_time_now_list);
        if (isWeekNumber) {
            if (courseDetails.get(position).getWeeks() != null) {
                String week_num = Arrays.toString(courseDetails.get(position).getWeeks());
                editText_show.setText(week_num.substring(1, week_num.length() - 1));
            }
        } else {
            if (courseDetails.get(position).getCourseTime() != null) {
                String course_time = Arrays.toString(courseDetails.get(position).getCourseTime());
                editText_show.setText(course_time.substring(1, course_time.length() - 1));
            }
        }

        Button button_backspace = view.findViewById(R.id.button_course_edit_time_backspace);
        button_backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = editText_show.getText().toString();
                if (str.isEmpty()) {
                    Toast.makeText(activity, R.string.no_item_to_delete, Toast.LENGTH_SHORT).show();
                } else {
                    if (str.contains(", ")) {
                        editText_show.setText(str.substring(0, str.lastIndexOf(", ")));
                    } else {
                        editText_show.setText("");
                    }
                }
            }
        });

        final TextInputEditText editText_start = view.findViewById(R.id.editText_course_edit_time_start_time);

        final TextInputEditText editText_end = view.findViewById(R.id.editText_course_edit_time_end_time);

        final CheckBox checkBox_single = view.findViewById(R.id.checkBox_course_edit_time_single_time);
        checkBox_single.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    editText_end.setText("");
                    editText_end.setEnabled(false);
                } else {
                    editText_end.setEnabled(true);
                }
            }
        });

        Button button_add = view.findViewById(R.id.button_course_edit_time_add);
        button_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable_start = editText_start.getText();
                Editable editable_end = editText_end.getText();
                if (editable_start != null && editable_end != null) {
                    String start = editable_start.toString();
                    String end = editable_end.toString();
                    boolean isSingle = checkBox_single.isChecked();
                    if (isSingle) {
                        if (start.isEmpty()) {
                            Toast.makeText(activity, R.string.input_less, Toast.LENGTH_SHORT).show();
                        } else {
                            String str = editText_show.getText().toString();
                            if (str.isEmpty()) {
                                editText_show.setText(start);
                            } else {
                                String output = str + ", " + start;
                                editText_show.setText(output);
                            }
                        }
                    } else {
                        if (start.isEmpty() || end.isEmpty()) {
                            Toast.makeText(activity, R.string.input_less, Toast.LENGTH_SHORT).show();
                        } else {
                            if (Integer.valueOf(start) >= Integer.valueOf(end)
                                    || Integer.valueOf(start) <= 0
                                    || Integer.valueOf(end) <= 0
                                    || ((isWeekNumber && Integer.valueOf(end) > Config.DEFAULT_MAX_WEEK)
                                    || (!isWeekNumber && Integer.valueOf(end) > Config.MAX_DAY_COURSE))) {
                                Toast.makeText(activity, R.string.input_error, Toast.LENGTH_SHORT).show();
                            } else {
                                String str = editText_show.getText().toString();
                                if (str.isEmpty()) {
                                    String output = start + "-" + end;
                                    editText_show.setText(output);
                                } else {
                                    String output = str + ", " + start + "-" + end;
                                    editText_show.setText(output);
                                }
                            }
                        }
                    }
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (isWeekNumber) {
            builder.setTitle(R.string.course_detail_edit_week_num);
        } else {
            builder.setTitle(R.string.course_detail_edit_time);
        }
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String str = editText_show.getText().toString();
                if (str.isEmpty()) {
                    if (isWeekNumber) {
                        courseDetails.get(position).setWeeks(null);
                    } else {
                        courseDetails.get(position).setCourseTime(null);
                    }
                } else {
                    String[] arr;
                    if (str.contains(", ")) {
                        arr = str.split(", ");
                    } else {
                        arr = new String[]{str};
                    }
                    boolean error = false;
                    for (int i = 0; i < arr.length && !error; i++) {
                        for (int j = 0; j < arr.length && !error; j++) {
                            if (i != j) {
                                if (!CourseEditMethod.checkWeekNumOrCourseTime(new String[]{arr[i]}, new String[]{arr[j]})) {
                                    error = true;
                                }
                            }
                        }
                    }
                    if (error) {
                        Toast.makeText(activity, R.string.input_error, Toast.LENGTH_SHORT).show();
                    } else {
                        if (isWeekNumber) {
                            courseDetails.get(position).setWeeks(arr);
                        } else {
                            courseDetails.get(position).setCourseTime(arr);
                        }
                    }
                }
                activity.setArrayChanged();
                notifyItemChanged(position);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setView(view);
        builder.show();
    }

    static class CourseEditViewHolder extends RecyclerView.ViewHolder {
        final Button button_delete;
        final CheckBox checkBox_course_edit_single_week;
        final CheckBox checkBox_course_edit_double_week;
        final EditText editText_course_edit_week_num;
        final Button button_course_edit_week_num;
        final Spinner spinner_course_edit_week;
        final EditText editText_course_edit_time;
        final Button button_course_edit_time;
        final EditText editText_course_edit_location;
        final Button button_course_edit_location;

        CourseEditViewHolder(View view) {
            super(view);
            button_delete = view.findViewById(R.id.button_course_edit_time_delete);
            checkBox_course_edit_single_week = view.findViewById(R.id.checkBox_course_edit_single_week);
            checkBox_course_edit_double_week = view.findViewById(R.id.checkBox_course_edit_double_week);
            editText_course_edit_week_num = view.findViewById(R.id.editText_course_edit_week_num);
            button_course_edit_week_num = view.findViewById(R.id.button_course_edit_week_num);
            spinner_course_edit_week = view.findViewById(R.id.spinner_course_edit_week);
            editText_course_edit_time = view.findViewById(R.id.editText_course_edit_time);
            button_course_edit_time = view.findViewById(R.id.button_course_edit_time);
            editText_course_edit_location = view.findViewById(R.id.editText_course_edit_location);
            button_course_edit_location = view.findViewById(R.id.button_course_edit_location);
        }
    }
}
