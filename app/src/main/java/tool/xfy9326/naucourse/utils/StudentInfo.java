package tool.xfy9326.naucourse.utils;

import androidx.annotation.Nullable;

/**
 * Created by xfy9326 on 18-2-21.
 * 学生信息
 */

public class StudentInfo extends BaseData {
    @Nullable
    private String std_id = null;
    @Nullable
    private String std_name = null;
    @Nullable
    private String std_grade = null;
    @Nullable
    private String std_collage = null;
    @Nullable
    private String std_major = null;
    @Nullable
    private String std_direction = null;
    @Nullable
    private String std_class = null;

    public StudentInfo() {
    }

    @Nullable
    public String getStd_name() {
        return std_name;
    }

    public void setStd_name(@Nullable String std_name) {
        this.std_name = std_name;
    }

    @Nullable
    public String getStd_id() {
        return std_id;
    }

    public void setStd_id(@Nullable String std_id) {
        this.std_id = std_id;
    }

    @Nullable
    public String getStd_grade() {
        return std_grade;
    }

    public void setStd_grade(@Nullable String std_grade) {
        this.std_grade = std_grade;
    }

    @Nullable
    public String getStd_collage() {
        return std_collage;
    }

    public void setStd_collage(@Nullable String std_collage) {
        this.std_collage = std_collage;
    }

    @Nullable
    public String getStd_major() {
        return std_major;
    }

    public void setStd_major(@Nullable String std_major) {
        this.std_major = std_major;
    }

    @Nullable
    public String getStd_direction() {
        return std_direction;
    }

    public void setStd_direction(@Nullable String std_direction) {
        this.std_direction = std_direction;
    }

    @Nullable
    public String getStd_class() {
        return std_class;
    }

    public void setStd_class(@Nullable String std_class) {
        this.std_class = std_class;
    }

}
