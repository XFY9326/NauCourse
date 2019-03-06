package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.gridlayout.widget.GridLayout;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Course;

/**
 * Created by 10696 on 2018/3/28.
 * 展示课程表的方法
 */

public class CourseViewMethod {
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private ArrayList<Course> courses;
    private int parent_width = 0;
    private int parent_height = 0;
    private GridLayout course_table_layout;
    @Nullable
    private String[][] table;
    @Nullable
    private String[][] id_table;
    @Nullable
    private boolean[][] this_week_no_show_table;
    @Nullable
    private OnCourseTableItemClickListener onCourseTableClick;

    public CourseViewMethod(Context context, ArrayList<Course> courses) {
        this.context = context;
        this.onCourseTableClick = null;
        this.table = null;
        this.id_table = null;
        this.courses = courses;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * 设置需要操作的视图
     *
     * @param gridLayout   GridLayout课程表主视图
     * @param parent_width 父控件的宽度
     */
    public void setTableView(GridLayout gridLayout, int parent_width, int parent_height) {
        this.course_table_layout = gridLayout;
        this.parent_width = parent_width;
        this.parent_height = parent_height;
    }

    /**
     * 更新课程表视图
     *
     * @param table     需要显示信息的二维数组
     * @param id_table  课程信息对应的ID的二维数组
     * @param checkSame 是否检查到相同数据就不更新视图
     */
    public void updateCourseTableView(ArrayList<Course> courses, @Nullable String[][] table, @Nullable String[][] id_table, @Nullable boolean[][] this_week_no_show_table, boolean checkSame, boolean hasCustomBackground) {
        this.courses = courses;
        if (table != null && id_table != null && this_week_no_show_table != null) {
            if (checkSame && Arrays.equals(table, this.table) && Arrays.equals(id_table, this.id_table) && Arrays.equals(this_week_no_show_table, this.this_week_no_show_table)) {
                return;
            } else {
                this.table = table;
                this.id_table = id_table;
                this.this_week_no_show_table = this_week_no_show_table;
            }
        }
        if (checkData()) {
            loadView(hasCustomBackground);
        }
    }

    /**
     * 设置对课程表课程信息点击的监听
     *
     * @param onCourseTableClick OnCourseTableItemClickListener
     */
    public void setOnCourseTableClickListener(OnCourseTableItemClickListener onCourseTableClick) {
        this.onCourseTableClick = onCourseTableClick;
    }

    private boolean checkData() {
        return course_table_layout != null && parent_width != 0 && table != null && id_table != null && this_week_no_show_table != null && courses != null;
    }

    synchronized private void loadView(boolean hasCustomBackground) {
        if (table != null) {
            //清空原来的数据
            course_table_layout.removeAllViews();

            boolean showCellColor = sharedPreferences.getBoolean(Config.PREFERENCE_COURSE_TABLE_CELL_COLOR, Config.DEFAULT_PREFERENCE_COURSE_TABLE_CELL_COLOR);
            boolean singleColor = sharedPreferences.getBoolean(Config.PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR, Config.DEFAULT_PREFERENCE_COURSE_TABLE_SHOW_SINGLE_COLOR);

            //设置行列数量
            boolean showWeekend = false;
            for (int i = table.length - 2; i < table.length; i++) {
                for (int j = 1; j < table[i].length; j++) {
                    if (table[i][j] != null) {
                        showWeekend = true;
                        break;
                    }
                }
            }
            boolean showWide = sharedPreferences.getBoolean(Config.PREFERENCE_SHOW_WIDE_TABLE, Config.DEFAULT_PREFERENCE_SHOW_WIDE_TABLE);

            int row_max = Config.MAX_DAY_COURSE + 1;
            int col_max = showWeekend ? Config.MAX_WEEK_DAY + 1 : Config.MAX_WEEK_DAY - 1;

            float alpha = sharedPreferences.getFloat(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY, Config.DEFAULT_PREFERENCE_CHANGE_TABLE_TRANSPARENCY);

            course_table_layout.setColumnCount(col_max);
            course_table_layout.setRowCount(row_max);
            course_table_layout.setMinimumWidth(parent_width);
            if (hasCustomBackground) {
                course_table_layout.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            }

            //设置每个单元格
            for (int col = 0; col < col_max; col++) {
                for (int row = 0; row < row_max; row++) {
                    int merge = 1;
                    String text = table[col][row];

                    //合并相同数据的单元格（仅限一列）
                    if (col > 0 && row > 0) {
                        if (text != null && !text.isEmpty()) {
                            for (merge = 1; row + merge < row_max && merge <= row_max; merge++) {
                                if (table[col][row + merge] == null || !table[col][row + merge].equalsIgnoreCase(text)) {
                                    break;
                                }
                            }
                        }
                        row += merge - 1;
                    }

                    int weight_col = ((col == 0) ? 1 : 2);
                    GridLayout.Spec col_merge = GridLayout.spec(GridLayout.UNDEFINED, 1, weight_col);
                    GridLayout.Spec row_merge = GridLayout.spec(GridLayout.UNDEFINED, merge, 1);
                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(row_merge, col_merge);
                    layoutParams.setGravity(Gravity.FILL);
                    layoutParams.setMargins(1, 1, 1, 1);

                    //添加单元格视图到表格
                    int width = ((col == 0) ? 0 : (parent_width / col_max));

                    //设置单元格与文字的颜色
                    int bgColor = Color.WHITE;
                    int textColor = Color.GRAY;
                    if (showCellColor && text != null && row != 0 && col != 0) {
                        bgColor = getCourseColorById(Objects.requireNonNull(id_table)[col][row]);
                        if (bgColor == -1 || singleColor) {
                            bgColor = context.getResources().getColor(R.color.course_cell_background);
                        }
                        if (!isLightColor(bgColor)) {
                            textColor = Color.WHITE;
                        }
                    }
                    if (this_week_no_show_table != null && this_week_no_show_table[col][row]) {
                        bgColor = context.getResources().getColor(R.color.light_light_grey);
                        textColor = context.getResources().getColor(R.color.light_grey);
                    }

                    View cellView = getCellView(text, bgColor, textColor, width, col, row, showWide, hasCustomBackground, alpha);
                    cellView.setLayoutParams(layoutParams);
                    if (row != 0) {
                        cellView.setMinimumHeight(parent_height / row_max);
                    }
                    course_table_layout.addView(cellView);
                }
            }
        }
    }

    /**
     * 判断是否是明亮的颜色
     *
     * @param color 颜色
     * @return 是否明亮
     */
    private boolean isLightColor(int color) {
        int red = (color & 0xff0000) >> 16;
        int green = (color & 0x00ff00) >> 8;
        int blue = (color & 0x0000ff);
        float total = red * 0.299f + green * 0.587f + blue * 0.114f;
        //DEFAULT 200
        return !(total < 220);
    }

    private int getCourseColorById(String id) {
        for (Course course : courses) {
            String courseId = course.getCourseId();
            if (courseId != null && courseId.equalsIgnoreCase(id)) {
                return course.getCourseColor();
            }
        }
        return -1;
    }

    //获取每个单元格的视图
    @NonNull
    private View getCellView(@Nullable String text, @ColorInt int bgColor, @ColorInt int textColor, int width, final int col, final int row, boolean showWide, boolean hasCustomBackground, float alpha) {
        LinearLayout linearLayout = new LinearLayout(context);
        //不同显示模式下的适配
        if (showWide) {
            linearLayout.setGravity(Gravity.CENTER);
        } else if (col != 0 && row != 0) {
            linearLayout.setGravity(Gravity.CENTER | Gravity.TOP);
        }
        if (col == 0 || row == 0) {
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
        if (hasCustomBackground) {
            linearLayout.setAlpha(alpha);
        }
        linearLayout.setBackgroundColor(bgColor);

        //文字显示设置
        if (text != null) {
            TextView textView = new TextView(context);
            textView.setPadding(5, 5, 5, 5);
            if (col != 0) {
                if (showWide) {
                    width *= 2;
                }
                textView.setLayoutParams(new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            if (col != 0 && row != 0 && !showWide) {
                textView.setGravity(Gravity.CENTER | Gravity.START);
            } else {
                textView.setGravity(Gravity.CENTER);
            }
            textView.setTextColor(textColor);

            if (col == 0 && row != 0) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                spannableStringBuilder.setSpan(new AbsoluteSizeSpan(11, true), 0, text.indexOf("\n"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spannableStringBuilder.setSpan(new AbsoluteSizeSpan(13, true), text.indexOf("\n") + 1, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                textView.setText(spannableStringBuilder);
            } else if (row == 0 && col != 0) {
                SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                spannableStringBuilder.setSpan(new AbsoluteSizeSpan(13, true), 0, text.indexOf("\n"), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                spannableStringBuilder.setSpan(new AbsoluteSizeSpan(11, true), text.indexOf("\n") + 1, text.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                textView.setText(spannableStringBuilder);
            } else {
                textView.setTextSize(13);
                textView.setText(text);
            }

            //单元格监听
            if (row != 0 && col != 0) {
                linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (Course course : courses) {
                            if (onCourseTableClick != null && Objects.requireNonNull(course.getCourseId()).equals(Objects.requireNonNull(id_table)[col][row])) {
                                onCourseTableClick.OnItemClick(course);
                                break;
                            }
                        }
                    }
                });
            }

            linearLayout.addView(textView);
        }
        return linearLayout;
    }

    public interface OnCourseTableItemClickListener {
        void OnItemClick(Course course);
    }
}
