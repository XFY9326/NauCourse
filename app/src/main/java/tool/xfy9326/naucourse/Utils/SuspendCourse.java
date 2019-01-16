package tool.xfy9326.naucourse.Utils;

@SuppressWarnings("unused")
public class SuspendCourse extends BaseUtils {
    private String term;
    private String termStart;
    private String termEnd;
    private int count = 0;
    private String[] name;
    private String[] course;
    private String[] teacher;

    private String[][] detail_type;
    private String[][] detail_class;
    private String[][] detail_location;
    private String[][] detail_date;

    public SuspendCourse() {

    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String[][] getDetail_type() {
        return detail_type;
    }

    public void setDetail_type(String[][] detail_type) {
        this.detail_type = detail_type;
    }

    public String[][] getDetail_class() {
        return detail_class;
    }

    public void setDetail_class(String[][] detail_class) {
        this.detail_class = detail_class;
    }

    public String[][] getDetail_location() {
        return detail_location;
    }

    public void setDetail_location(String[][] detail_location) {
        this.detail_location = detail_location;
    }

    public String[][] getDetail_date() {
        return detail_date;
    }

    public void setDetail_date(String[][] detail_date) {
        this.detail_date = detail_date;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getTermStart() {
        return termStart;
    }

    public void setTermStart(String termStart) {
        this.termStart = termStart;
    }

    public String getTermEnd() {
        return termEnd;
    }

    public void setTermEnd(String termEnd) {
        this.termEnd = termEnd;
    }

    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public String[] getCourse() {
        return course;
    }

    public void setCourse(String[] course) {
        this.course = course;
    }

    public String[] getTeacher() {
        return teacher;
    }

    public void setTeacher(String[] teacher) {
        this.teacher = teacher;
    }
}
