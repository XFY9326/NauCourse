package tool.xfy9326.naucourse.Utils;

import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by xfy9326 on 18-2-21.
 * 学生信息
 */

public class StudentInfo implements Serializable {
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
    private int dataVersionCode = 0;

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

    @SuppressWarnings("unused")
    public int getDataVersionCode() {
        return dataVersionCode;
    }

    @SuppressWarnings("SameParameterValue")
    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }

}
