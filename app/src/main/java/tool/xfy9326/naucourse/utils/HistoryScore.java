package tool.xfy9326.naucourse.utils;

public class HistoryScore extends BaseData {
    private String[] id;
    private String[] name;
    private String[] studyScore;
    private String[] score;
    private String[] creditWeight;
    private String[] term;
    private String[] courseProperty;
    private String[] courseType;
    private int courseAmount;

    public HistoryScore() {
        this.id = null;
        this.name = null;
        this.studyScore = null;
        this.score = null;
        this.creditWeight = null;
        this.term = null;
        this.courseProperty = null;
        this.courseType = null;
        this.courseAmount = 0;
    }

    public String[] getId() {
        return id;
    }

    public void setId(String[] id) {
        this.id = id;
    }

    public String[] getName() {
        return name;
    }

    public void setName(String[] name) {
        this.name = name;
    }

    public String[] getStudyScore() {
        return studyScore;
    }

    public void setStudyScore(String[] studyScore) {
        this.studyScore = studyScore;
    }

    public String[] getScore() {
        return score;
    }

    public void setScore(String[] score) {
        this.score = score;
    }

    public String[] getCreditWeight() {
        return creditWeight;
    }

    public void setCreditWeight(String[] creditWeight) {
        this.creditWeight = creditWeight;
    }

    public String[] getTerm() {
        return term;
    }

    public void setTerm(String[] term) {
        this.term = term;
    }

    public String[] getCourseProperty() {
        return courseProperty;
    }

    public void setCourseProperty(String[] courseProperty) {
        this.courseProperty = courseProperty;
    }

    public String[] getCourseType() {
        return courseType;
    }

    public void setCourseType(String[] courseType) {
        this.courseType = courseType;
    }

    public int getCourseAmount() {
        return courseAmount;
    }

    public void setCourseAmount(int courseAmount) {
        this.courseAmount = courseAmount;
    }
}
