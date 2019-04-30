package tool.xfy9326.naucourse.Utils;

public class CourseSearchDetail {
    private String className;
    private String name;
    private String[] week;
    private String weekDes;
    private String term;
    private int weekDay;
    private int startCourseTime;
    private int endCourseTime;
    private int weekMode = 0;
    private String score;
    private String roomName;
    private String teacher;
    private String teacherGrade;
    private String combinedClassName;
    private String collage;
    private String teacherId;
    private int stuNum;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getTeacherGrade() {
        return teacherGrade;
    }

    public void setTeacherGrade(String teacherGrade) {
        this.teacherGrade = teacherGrade;
    }

    public String getCombinedClassName() {
        return combinedClassName;
    }

    public void setCombinedClassName(String combinedClassName) {
        this.combinedClassName = combinedClassName;
    }

    public String getCollage() {
        return collage;
    }

    public void setCollage(String collage) {
        this.collage = collage;
    }

    public int getStuNum() {
        return stuNum;
    }

    public void setStuNum(int stuNum) {
        this.stuNum = stuNum;
    }

    public String[] getWeeks() {
        return week;
    }

    public void setWeeks(String[] week) {
        this.week = week;
    }

    public int getWeekDay() {
        return weekDay;
    }

    public void setWeekDay(int weekDay) {
        this.weekDay = weekDay;
    }

    public int getStartCourseTime() {
        return startCourseTime;
    }

    public void setStartCourseTime(int startCourseTime) {
        this.startCourseTime = startCourseTime;
    }

    public int getEndCourseTime() {
        return endCourseTime;
    }

    public void setEndCourseTime(int endCourseTime) {
        this.endCourseTime = endCourseTime;
    }

    public int getWeekMode() {
        return weekMode;
    }

    public void setWeekMode(int weekMode) {
        this.weekMode = weekMode;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getWeekDes() {
        return weekDes;
    }

    public void setWeekDes(String weekDes) {
        this.weekDes = weekDes;
    }
}
