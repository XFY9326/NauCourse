package tool.xfy9326.naucourse.methods.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.beans.course.Course;

/**
 * Created by 10696 on 2018/3/28.
 * 展示课程表的方法
 */

public class CourseViewMethod {
    private final Context context;
    private final SharedPreferences sharedPreferences;
    private final int defaultCourseBackground;
    private final int defaultTextColor;
    private final String notThisWeekText;
    private ArrayList<Course> courses;
    private int parentWidth = 0;
    private int parentHeight = 0;
    private GridLayout courseTableLayout;
    @Nullable
    private String[][] table;
    @Nullable
    private String[][] idTable;
    @Nullable
    private boolean[][] thisWeekNoShowTable;
    @Nullable
    private OnCourseTableItemClickListener onCourseTableClick;

    public CourseViewMethod(Context context, ArrayList<Course> courses) {
        this.context = context;
        this.onCourseTableClick = null;
        this.table = null;
        this.idTable = null;
        this.courses = courses;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.defaultCourseBackground = ResourcesCompat.getColor(context.getResources(), R.color.course_item_background, context.getTheme());
        this.defaultTextColor = ResourcesCompat.getColor(context.getResources(), R.color.colorSecondaryText, context.getTheme());
        this.notThisWeekText = context.getString(R.string.not_this_week);
    }

    /**
     * 设置需要操作的视图
     *
     * @param gridLayout   GridLayout课程表主视图
     * @param parentWidth  父控件的宽度
     * @param parentHeight 父控件的高度
     */
    public void setTableView(GridLayout gridLayout, int parentWidth, int parentHeight) {
        this.courseTableLayout = gridLayout;
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
    }

    /**
     * 更新表格大小
     *
     * @param parentWidth  父控件的宽度
     * @param parentHeight 父控件的高度
     */
    public void updateTableSize(int parentWidth, int parentHeight) {
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
    }

    /**
     * 更新课程表视图
     *
     * @param table     需要显示信息的二维数组
     * @param idTable   课程信息对应的ID的二维数组
     * @param checkSame 是否检查到相同数据就不更新视图
     */
    public void updateCourseTableView(ArrayList<Course> courses, @Nullable String[][] table, @Nullable String[][] idTable, @Nullable boolean[][] thisWeekNoShowTable, boolean checkSame, boolean hasCustomBackground) {
        this.courses = courses;
        if (table != null && idTable != null && thisWeekNoShowTable != null) {
            if (checkSame && Arrays.equals(table, this.table) && Arrays.equals(idTable, this.idTable) && Arrays.equals(thisWeekNoShowTable, this.thisWeekNoShowTable)) {
                return;
            } else {
                this.table = table;
                this.idTable = idTable;
                this.thisWeekNoShowTable = thisWeekNoShowTable;
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
        return courseTableLayout != null && parentWidth != 0 && table != null && idTable != null && thisWeekNoShowTable != null && courses != null;
    }

    synchronized private void loadView(boolean hasCustomBackground) {
        if (table != null && thisWeekNoShowTable != null) {
            //清空原来的数据
            courseTableLayout.removeAllViews();

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

            int rowMax = Config.MAX_DAY_COURSE + 1;
            int colMax = showWeekend ? Config.MAX_WEEK_DAY + 1 : Config.MAX_WEEK_DAY - 1;

            float alpha = sharedPreferences.getFloat(Config.PREFERENCE_CHANGE_TABLE_TRANSPARENCY, Config.DEFAULT_PREFERENCE_CHANGE_TABLE_TRANSPARENCY);

            courseTableLayout.setColumnCount(colMax);
            courseTableLayout.setRowCount(rowMax);
            courseTableLayout.setMinimumWidth(parentWidth);
            if (hasCustomBackground) {
                courseTableLayout.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), android.R.color.transparent, context.getTheme()));
            } else {
                courseTableLayout.setBackgroundColor(ResourcesCompat.getColor(context.getResources(), R.color.table_background, context.getTheme()));
            }

            //设置每个单元格
            for (int col = 0; col < colMax; col++) {
                for (int row = 0; row < rowMax; row++) {
                    int merge = 1;
                    String text = table[col][row];

                    //合并相同数据的单元格（仅限一列）
                    if (col > 0 && row > 0) {
                        if (text != null && !text.isEmpty()) {
                            for (merge = 1; row + merge < rowMax && merge <= rowMax; merge++) {
                                if (table[col][row + merge] == null || !table[col][row + merge].equalsIgnoreCase(text)) {
                                    break;
                                }
                            }
                        }
                        row += merge - 1;
                    }

                    int weightCol = ((col == 0) ? 1 : 2);
                    GridLayout.Spec colMerge = GridLayout.spec(GridLayout.UNDEFINED, 1, weightCol);
                    GridLayout.Spec rowMerge = GridLayout.spec(GridLayout.UNDEFINED, merge, 1);
                    GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(rowMerge, colMerge);
                    layoutParams.setGravity(Gravity.FILL);
                    layoutParams.setMargins(1, 1, 1, 1);

                    //添加单元格视图到表格
                    int width = ((col == 0) ? 0 : (parentWidth / colMax));

                    //设置单元格与文字的颜色
                    int bgColor = defaultCourseBackground;
                    int textColor = defaultTextColor;
                    if (showCellColor && text != null && row != 0 && col != 0) {
                        bgColor = getCourseColorById(Objects.requireNonNull(idTable)[col][row]);
                        if (bgColor == -1 || singleColor) {
                            bgColor = ResourcesCompat.getColor(context.getResources(), R.color.course_cell_background, context.getTheme());
                        }
                        if (!isLightColor(bgColor)) {
                            textColor = Color.WHITE;
                        }
                    }

                    //非本周课程颜色更改
                    if (thisWeekNoShowTable[col][row]) {
                        text = notThisWeekText + "\n" + text;
                        bgColor = ResourcesCompat.getColor(context.getResources(), R.color.course_cell_no_this_week, context.getTheme());
                        textColor = ResourcesCompat.getColor(context.getResources(), R.color.light_grey, context.getTheme());
                    }

                    View cellView = getCellView(text, bgColor, textColor, width, col, row, showWide, hasCustomBackground, alpha, thisWeekNoShowTable[col][row]);
                    cellView.setLayoutParams(layoutParams);
                    if (row != 0) {
                        cellView.setMinimumHeight(parentHeight / rowMax);
                    }
                    courseTableLayout.addView(cellView);
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
        return total >= 220;
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
    private View getCellView(@Nullable String text, @ColorInt int bgColor, @ColorInt int textColor, int width, final int col, final int row, boolean showWide, boolean hasCustomBackground, float alpha, boolean noThisWeek) {
        LinearLayout cellLayout = new LinearLayout(context);
        //不同显示模式下的适配
        if (showWide) {
            cellLayout.setGravity(Gravity.CENTER);
        } else if (col != 0 && row != 0) {
            cellLayout.setGravity(Gravity.CENTER | Gravity.TOP);
        }
        if (col == 0 || row == 0) {
            cellLayout.setGravity(Gravity.CENTER);
            cellLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        }
        if (hasCustomBackground) {
            cellLayout.setAlpha(alpha);
        }
        cellLayout.setBackgroundColor(bgColor);

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
                if (noThisWeek) {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
                    spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, notThisWeekText.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    textView.setText(spannableStringBuilder);
                } else {
                    textView.setText(text);
                }
                textView.setTextSize(13);
            }

            //单元格监听
            if (row != 0 && col != 0) {
                cellLayout.setOnClickListener(v -> {
                    for (Course course : courses) {
                        if (onCourseTableClick != null && Objects.requireNonNull(course.getCourseId()).equals(Objects.requireNonNull(idTable)[col][row])) {
                            onCourseTableClick.onItemClick(course);
                            break;
                        }
                    }
                });
            }

            cellLayout.addView(textView);
        }
        return cellLayout;
    }

    public interface OnCourseTableItemClickListener {
        void onItemClick(Course course);
    }
}
