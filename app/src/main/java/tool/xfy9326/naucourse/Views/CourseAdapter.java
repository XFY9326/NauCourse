package tool.xfy9326.naucourse.Views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Activities.CourseActivity;
import tool.xfy9326.naucourse.Activities.CourseEditActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Course;

public class CourseAdapter extends RecyclerView.Adapter<CourseViewHolder> {
    private static final String COLOR_PICKER_DIALOG_TAG = "course_color_picker";
    private static final int COLOR_PICKER_DIALOG_ID = 1;
    private final CourseActivity activity;
    private ArrayList<Course> courseArrayList;
    private final SharedPreferences sharedPreferences;
    private boolean showCellColor;
    private boolean singleColor;

    public CourseAdapter(CourseActivity activity, ArrayList<Course> courseArrayList) {
        this.activity = activity;
        this.courseArrayList = courseArrayList;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        updateColor();
    }

    public void updateList(ArrayList<Course> courseArrayList) {
        this.courseArrayList = courseArrayList;
        updateColor();
    }

    private void updateColor() {
        showCellColor = sharedPreferences.getBoolean(Config.PREFERENCE_COURSE_TABLE_CELL_COLOR, Config.DEFAULT_PREFERENCE_COURSE_TABLE_CELL_COLOR);
        singleColor = sharedPreferences.getBoolean(Config.PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR, Config.DEFAULT_PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR);
    }

    @Override
    public void onBindViewHolder(@NonNull final CourseViewHolder holder, int position) {
        holder.textView_course_name.setText(courseArrayList.get(holder.getAdapterPosition()).getCourseName());
        holder.textView_course_edit_teacher.setText(activity.getString(R.string.course_card_teacher, courseArrayList.get(holder.getAdapterPosition()).getCourseTeacher()));
        holder.button_course_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, CourseEditActivity.class);
                intent.putExtra(Config.INTENT_EDIT_COURSE, true);
                intent.putExtra(Config.INTENT_EDIT_COURSE_ITEM, courseArrayList.get(holder.getAdapterPosition()));
                activity.startActivityForResult(intent, CourseActivity.COURSE_EDIT_REQUEST_CODE);
            }
        });
        if (showCellColor) {
            holder.button_course_color.setVisibility(View.VISIBLE);
            holder.button_course_color.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (showCellColor) {
                        if (!singleColor) {
                            editColor(holder.getAdapterPosition());
                        } else {
                            if (!activity.activityDestroy) {
                                Snackbar.make(activity.findViewById(R.id.layout_course_manage_content), R.string.single_color_warn, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
            });
        } else {
            holder.button_course_color.setVisibility(View.GONE);
        }
        holder.button_course_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!activity.activityDestroy) {
                    Snackbar.make(activity.findViewById(R.id.layout_course_manage_content), R.string.delete_confirm, Snackbar.LENGTH_LONG).setActionTextColor(Color.RED).setAction(android.R.string.yes, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!activity.activityDestroy) {
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
                        }
                    }).show();
                }
            }
        });

        if (showCellColor) {
            int color = courseArrayList.get(holder.getAdapterPosition()).getCourseColor();
            if (color == -1 || singleColor) {
                color = activity.getResources().getColor(R.color.course_cell_background);
            }
            holder.cardView_course_edit.setBackgroundColor(color);
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
        int[] preset = ColorPickerDialog.MATERIAL_COLORS;
        preset[preset.length - 1] = activity.getResources().getColor(R.color.course_cell_background);
        ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder()
                .setColor(default_color)
                .setDialogTitle(R.string.course_color)
                .setDialogId(COLOR_PICKER_DIALOG_ID)
                .setPresets(preset)
                .setShowAlphaSlider(false)
                .create();
        colorPickerDialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
            @Override
            public void onColorSelected(int dialogId, int color) {
                if (dialogId == COLOR_PICKER_DIALOG_ID) {
                    courseArrayList.get(position).setCourseColor(color);
                }
            }

            @Override
            public void onDialogDismissed(int dialogId) {
                if (dialogId == COLOR_PICKER_DIALOG_ID) {
                    notifyItemChanged(position);
                    activity.setArrayChanged();
                }
            }
        });
        colorPickerDialog.show(activity.getFragmentManager(), COLOR_PICKER_DIALOG_TAG);
    }
}
