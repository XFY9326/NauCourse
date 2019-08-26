package tool.xfy9326.naucourse.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import tool.xfy9326.naucourse.utils.CourseScore;
import tool.xfy9326.naucourse.utils.CreditCountCourse;
import tool.xfy9326.naucourse.utils.HistoryScore;

public class CreditCountMethod {
    private static final Pattern DOUBLE_PATTERN = Pattern.compile("^[-+]?[.\\d]*$");

    public static ArrayList<CreditCountCourse> getCreditCountCourse(CourseScore courseScore) {
        ArrayList<CreditCountCourse> countCourses = new ArrayList<>();
        for (int i = 0; i < courseScore.getCourseAmount(); i++) {
            if (!Objects.requireNonNull(courseScore.getScoreTotal())[i].contains("未")) {
                try {
                    CreditCountCourse course = new CreditCountCourse();
                    float score = Float.parseFloat(Objects.requireNonNull(courseScore.getScoreTotal())[i]);
                    float studyScore = Float.parseFloat(Objects.requireNonNull(courseScore.getScoreCourseXf())[i]);
                    course.setCourseId(Objects.requireNonNull(courseScore.getScoreCourseId())[i]);
                    course.setCourseName(Objects.requireNonNull(courseScore.getScoreCourseName())[i]);
                    course.setScore(score);
                    course.setStudyScore(studyScore);
                    course.setCreditWeight(1f);
                    countCourses.add(course);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return countCourses;
    }

    public static ArrayList<CreditCountCourse> getHistoryCreditCourse(HistoryScore historyScore) {
        ArrayList<CreditCountCourse> courses = new ArrayList<>();
        for (int i = 0; i < historyScore.getCourseAmount(); i++) {
            float creditWeight = Float.parseFloat(historyScore.getCreditWeight()[i]);
            if (creditWeight > 0 && isDouble(historyScore.getScore()[i])) {
                CreditCountCourse creditCountCourse = new CreditCountCourse();
                creditCountCourse.setCourseId(historyScore.getId()[i]);
                creditCountCourse.setCourseName(historyScore.getName()[i]);
                creditCountCourse.setCreditWeight(creditWeight);
                creditCountCourse.setScore(Float.parseFloat(historyScore.getScore()[i]));
                creditCountCourse.setStudyScore(Float.parseFloat(historyScore.getStudyScore()[i]));
                courses.add(creditCountCourse);
            }
        }
        return courses;
    }

    public static ArrayList<CreditCountCourse> combineCreditCourse(List<CreditCountCourse> a, List<CreditCountCourse> b) {
        ArrayList<CreditCountCourse> result = new ArrayList<>(a);
        for (CreditCountCourse bCourse : b) {
            boolean foundSame = false;
            for (CreditCountCourse aCourse : a) {
                if (bCourse.getCourseId() != null && aCourse.getCourseId() != null && bCourse.getCourseId().equals(aCourse.getCourseId())) {
                    foundSame = true;
                    break;
                }
                if (bCourse.getCourseName() != null && aCourse.getCourseName() != null && bCourse.getCourseName().equals(aCourse.getCourseName())) {
                    foundSame = true;
                    break;
                }
            }
            if (!foundSame) {
                result.add(bCourse);
            }
        }
        return result;
    }

    public static float getCredit(List<CreditCountCourse> courseList) {
        float temp = 0;
        float totalStudyScore = 0;
        for (int i = 0; i < courseList.size(); i++) {
            CreditCountCourse course = courseList.get(i);
            if (course.getScore() < 60) {
                temp += 0;
            } else {
                temp += ((course.getScore() - 50) / 10f) * course.getCreditWeight() * course.getStudyScore();
            }
            totalStudyScore += course.getStudyScore();
        }
        return temp / totalStudyScore;
    }

    private static boolean isDouble(String str) {
        if (null == str || "".equals(str)) {
            return false;
        }
        return DOUBLE_PATTERN.matcher(str).matches();
    }
}
