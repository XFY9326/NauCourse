package tool.xfy9326.naucourse.Utils;

/**
 * Created by xfy9326 on 18-2-22.
 * 表格内所有数据整合
 */

@SuppressWarnings("unused")
public class TableLine {
    private String courseTime = null;
    private String courseMo = null;
    private String courseTu = null;
    private String courseWe = null;
    private String courseTh = null;
    private String courseFr = null;
    private String courseSa = null;
    private String courseSu = null;

    public TableLine(String courseTime, String courseMo, String courseTu, String courseWe, String courseTh, String courseFr, String courseSa, String courseSu) {
        this.courseTime = courseTime;
        this.courseMo = courseMo;
        this.courseTu = courseTu;
        this.courseWe = courseWe;
        this.courseTh = courseTh;
        this.courseFr = courseFr;
        this.courseSa = courseSa;
        this.courseSu = courseSu;
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

    public String getCourseSa() {
        return courseSa;
    }

    public void setCourseSa(String courseSa) {
        this.courseSa = courseSa;
    }

    public String getCourseSu() {
        return courseSu;
    }

    public void setCourseSu(String courseSu) {
        this.courseSu = courseSu;
    }
}
