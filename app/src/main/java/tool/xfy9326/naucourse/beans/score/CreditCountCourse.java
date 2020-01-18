package tool.xfy9326.naucourse.beans.score;

import androidx.annotation.Nullable;

@SuppressWarnings("unused")
public class CreditCountCourse {
    private String courseId;
    private String courseName;
    private float creditWeight = 1;
    private float score;
    private float studyScore;

    public float getCreditWeight() {
        return creditWeight;
    }

    public void setCreditWeight(float creditWeight) {
        this.creditWeight = creditWeight;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getStudyScore() {
        return studyScore;
    }

    public void setStudyScore(float studyScore) {
        this.studyScore = studyScore;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof CreditCountCourse) {
            CreditCountCourse course = (CreditCountCourse) obj;
            return course.courseId.equalsIgnoreCase(courseId) && course.courseName.equalsIgnoreCase(courseName);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return (courseId + courseName).hashCode();
    }
}
