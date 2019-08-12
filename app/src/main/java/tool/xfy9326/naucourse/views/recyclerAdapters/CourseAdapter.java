package tool.xfy9326.naucourse.views.recyclerAdapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.CourseActivity;
import tool.xfy9326.naucourse.activities.CourseEditActivity;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.utils.Course;

public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private static final String COLOR_PICKER_DIALOG_TAG = "COLOR_PICKER_DIALOG";
    private static final int COLOR_PICKER_DIALOG_ID = 1;
    private final CourseActivity activity;
    private final SharedPreferences sharedPreferences;
    private final ArrayList<Course> courseArrayList;
    private boolean showCellColor;
    private boolean singleColor;
    private ColorPickerDialog colorPickerDialog;

    public CourseAdapter(CourseActivity activity) {
        this(activity, new ArrayList<>());
    }

    public CourseAdapter(CourseActivity activity, ArrayList<Course> courseArrayList) {
        this.activity = activity;
        this.courseArrayList = courseArrayList;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        updateColor();
    }

    public void updateList(ArrayList<Course> courseArrayList) {
        this.courseArrayList.clear();
        this.courseArrayList.addAll(courseArrayList);
        updateColor();
        notifyDataSetChanged();
    }

    private void updateColor() {
        showCellColor = sharedPreferences.getBoolean(Config.PREFERENCE_COURSE_TABLE_CELL_COLOR, Config.DEFAULT_PREFERENCE_COURSE_TABLE_CELL_COLOR);
        singleColor = sharedPreferences.getBoolean(Config.PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR, Config.DEFAULT_PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR);
    }

    @Override
    public void onBindViewHolder(@NonNull final CourseViewHolder holder, int position) {
        final Course course = courseArrayList.get(holder.getAdapterPosition());
        holder.textView_course_name.setText(course.getCourseName());
        holder.textView_course_edit_teacher.setText(activity.getString(R.string.course_card_teacher, courseArrayList.get(holder.getAdapterPosition()).getCourseTeacher()));
        holder.button_course_edit.setOnClickListener(v -> {
            Intent intent = new Intent(activity, CourseEditActivity.class);
            intent.putExtra(Config.INTENT_EDIT_COURSE, true);
            intent.putExtra(Config.INTENT_EDIT_COURSE_ITEM, course);
            activity.startActivityForResult(intent, CourseActivity.COURSE_EDIT_REQUEST_CODE);
        });
        if (showCellColor) {
            holder.button_course_color.setVisibility(View.VISIBLE);
            holder.button_course_color.setOnClickListener(v -> {
                if (showCellColor) {
                    if (!singleColor) {
                        editColor(holder.getAdapterPosition());
                    } else {
                        if (!activity.activityDestroy) {
                            Snackbar.make(activity.findViewById(R.id.layout_course_manage_content), R.string.single_color_warn, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        } else {
            holder.button_course_color.setVisibility(View.GONE);
        }
        holder.button_course_delete.setOnClickListener(v -> {
            if (!activity.activityDestroy) {
                Snackbar.make(activity.findViewById(R.id.layout_course_manage_content), R.string.delete_confirm, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, v1 -> {
                    if (!activity.activityDestroy && holder.getAdapterPosition() > -1) {
                        courseArrayList.remove(holder.getAdapterPosition());
                        notifyItemRemoved(holder.getAdapterPosition());
                        activity.setArrayChanged();

                        FloatingActionButton floatingActionButton = activity.findViewById(R.id.floatingActionButton_course_add);
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

        if (showCellColor) {
            int color = course.getCourseColor();
            if (color == -1 || singleColor) {
                color = activity.getResources().getColor(R.color.course_cell_background);
            }
            holder.cardView_course_edit.setCardBackgroundColor(color);
        }
    }

    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_course, parent, false);
        return new CourseViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return courseArrayList.size();
    }

    /**
     * 颜色编辑
     *
     * @param position 位置
     */
    private void editColor(final int position) {
        int default_color = courseArrayList.get(position).getCourseColor();
        if (default_color == -1) {
            default_color = activity.getResources().getColor(R.color.course_cell_background);
        }
        int[] preset = BaseMethod.getColorArray(activity);
        preset[preset.length - 1] = activity.getResources().getColor(R.color.course_cell_background);
        colorPickerDialog = ColorPickerDialog.newBuilder()
                .setColor(default_color)
                .setDialogTitle(R.string.course_color)
                .setDialogId(COLOR_PICKER_DIALOG_ID)
                .setPresets(preset)
                .setShowAlphaSlider(false)
                .create();
        colorPickerDialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
            @Override
            public void onColorSelected(int dialogId, @ColorInt int color) {
                if (dialogId == COLOR_PICKER_DIALOG_ID) {
                    courseArrayList.get(position).setCourseColor(color);
                }
            }

            @Override
            public void onDialogDismissed(int dialogId) {
                if (dialogId == COLOR_PICKER_DIALOG_ID) {
                    notifyItemChanged(position);
                    activity.setArrayChanged();
                    colorPickerDialog = null;
                }
            }
        });
        colorPickerDialog.show(activity.getSupportFragmentManager(), COLOR_PICKER_DIALOG_TAG);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull CourseViewHolder holder) {
        if (colorPickerDialog != null && colorPickerDialog.isVisible()) {
            colorPickerDialog.dismissAllowingStateLoss();
        }
        super.onViewDetachedFromWindow(holder);
    }

    static class CourseViewHolder extends RecyclerView.ViewHolder {
        final TextView textView_course_name;
        final Button button_course_edit;
        final Button button_course_delete;
        final Button button_course_color;
        final TextView textView_course_edit_teacher;
        final CardView cardView_course_edit;

        CourseViewHolder(View view) {
            super(view);
            textView_course_name = view.findViewById(R.id.textView_course_edit_name);
            button_course_edit = view.findViewById(R.id.button_course_edit);
            button_course_delete = view.findViewById(R.id.button_course_delete);
            button_course_color = view.findViewById(R.id.button_course_color);
            textView_course_edit_teacher = view.findViewById(R.id.textView_course_edit_teacher);
            cardView_course_edit = view.findViewById(R.id.cardView_course_edit_item);
        }
    }
}
