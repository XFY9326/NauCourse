package tool.xfy9326.naucourse.methods;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import tool.xfy9326.naucourse.utils.CreditCountCourse;
import tool.xfy9326.naucourse.utils.HistoryScore;

public class CreditCountMethod {
    public static ArrayList<CreditCountCourse> getHistoryCreditCourse(HistoryScore historyScore) {
        ArrayList<CreditCountCourse> courses = new ArrayList<>();
        for (int i = 0; i < historyScore.getCourseAmount(); i++) {
            float creditWeight = Float.parseFloat(historyScore.getCreditWeight()[i]);
            if (creditWeight > 0 && isDouble(historyScore.getScore()[i])) {
                CreditCountCourse creditCountCourse = new CreditCountCourse();
                creditCountCourse.setCreditWeight(creditWeight);
                creditCountCourse.setScore(Float.parseFloat(historyScore.getScore()[i]));
                creditCountCourse.setStudyScore(Float.parseFloat(historyScore.getStudyScore()[i]));
                courses.add(creditCountCourse);
            }
        }
        return courses;
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
        Pattern pattern = Pattern.compile("^[-+]?[.\\d]*$");
        return pattern.matcher(str).matches();
    }
}
