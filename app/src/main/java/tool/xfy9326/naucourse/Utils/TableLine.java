package tool.xfy9326.naucourse.Utils;

/**
 * Created by xfy9326 on 18-2-22.
 */

@SuppressWarnings("unused")
public class TableLine {
    private String courseTime = null;
    private String courseMo = null;
    private String courseTu = null;
    private String courseWe = null;
    private String courseTh = null;
    private String courseFr = null;

    public TableLine(String courseTime, String courseMo, String courseTu, String courseWe, String courseTh, String courseFr) {
        this.courseTime = courseTime;
        this.courseMo = courseMo;
        this.courseTu = courseTu;
        this.courseWe = courseWe;
        this.courseTh = courseTh;
        this.courseFr = courseFr;
    }

    public String getCourseTime() {
        return courseTime;
    }

    public void setCourseTime(String courseTime) {
        this.courseTime = courseTime;
    }

    public String getCourseMo() {
        return courseMo;
    }

    public void setCourseMo(String courseMo) {
        this.courseMo = courseMo;
    }

    public String getCourseTu() {
        return courseTu;
    }

    public void setCourseTu(String courseTu) {
        this.courseTu = courseTu;
    }

    public String getCourseWe() {
        return courseWe;
    }

    public void setCourseWe(String courseWe) {
        this.courseWe = courseWe;
    }

    public String getCourseTh() {
        return courseTh;
    }

    public void setCourseTh(String courseTh) {
        this.courseTh = courseTh;
    }

    public String getCourseFr() {
        return courseFr;
    }

    public void setCourseFr(String courseFr) {
        this.courseFr = courseFr;
    }
}
