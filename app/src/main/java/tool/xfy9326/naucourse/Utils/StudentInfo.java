package tool.xfy9326.naucourse.Utils;

/**
 * Created by xfy9326 on 18-2-21.
 * 学生信息
 */

public class StudentInfo {
    private String std_id = null;
    private String std_name = null;
    private String std_grade = null;
    private String std_collage = null;
    private String std_major = null;
    private String std_direction = null;
    private String std_class = null;
    private int dataVersionCode = 0;

    public StudentInfo() {
    }

    public String getStd_name() {
        return std_name;
    }

    public void setStd_name(String std_name) {
        this.std_name = std_name;
    }

    public String getStd_id() {
        return std_id;
    }

    public void setStd_id(String std_id) {
        this.std_id = std_id;
    }

    public String getStd_grade() {
        return std_grade;
    }

    public void setStd_grade(String std_grade) {
        this.std_grade = std_grade;
    }

    public String getStd_collage() {
        return std_collage;
    }

    public void setStd_collage(String std_collage) {
        this.std_collage = std_collage;
    }

    public String getStd_major() {
        return std_major;
    }

    public void setStd_major(String std_major) {
        this.std_major = std_major;
    }

    public String getStd_direction() {
        return std_direction;
    }

    public void setStd_direction(String std_direction) {
        this.std_direction = std_direction;
    }

    public String getStd_class() {
        return std_class;
    }

    public void setStd_class(String std_class) {
        this.std_class = std_class;
    }

    public int getDataVersionCode() {
        return dataVersionCode;
    }

    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }

}
