package tool.xfy9326.naucourse.Methods;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;

public class CourseEditMethod {

    /**
     * 课程信息列表按照ID拼接
     *
     * @param courses         需要并入的课表
     * @param courseArrayList 原课表
     * @return 课程信息列表
     */
    public static ArrayList<Course> combineCourseList(ArrayList<Course> courses, ArrayList<Course> courseArrayList) {
        for (Course course : courses) {
            if (courseArrayList.isEmpty()) {
                courseArrayList.add(course);
            } else {
                boolean found = false;
                for (int i = 0; i < courseArrayList.size() && !found; i++) {
                    String id = course.getCourseId();
                    if (id != null && id.equalsIgnoreCase(courseArrayList.get(i).getCourseId())) {
                        courseArrayList.set(i, course);
                        found = true;
                    }
                }
                if (!found) {
                    courseArrayList.add(course);
                }
            }
        }
        return courseArrayList;
    }

    /**
     * 课程时间重复查重
     *
     * @param courses 课程信息列表
     * @return 检测结果
     */
    public static CourseCheckResult checkCourseList(ArrayList<Course> courses) {
        CourseCheckResult courseCheckResult = new CourseCheckResult();
        for (int i = 0; i < courses.size(); i++) {
            for (int j = 0; j < courses.size(); j++) {
                if (i != j) {
                    CourseDetail[] courseDetail_list_check = courses.get(i).getCourseDetail();
                    if (courseDetail_list_check != null) {
                        for (CourseDetail courseDetail_check : courseDetail_list_check) {
                            CourseDetail[] courseDetail_list = courses.get(j).getCourseDetail();
                            if (courseDetail_list != null) {
                                for (CourseDetail courseDetail : courseDetail_list) {
                                    if (!((courseDetail_check.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_SINGLE
                                            && courseDetail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_DOUBLE)
                                            || (courseDetail_check.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_DOUBLE
                                            && courseDetail.getWeekMode() == Config.COURSE_DETAIL_WEEKMODE_SINGLE))) {

                                        boolean weekDayCorrect = checkWeekDay(courseDetail_check.getWeekDay(), courseDetail.getWeekDay());
                                        boolean weekNumCorrect = checkWeekNumOrCourseTime(courseDetail_check.getWeeks(), courseDetail.getWeeks());
                                        boolean courseTimeCorrect = checkWeekNumOrCourseTime(courseDetail_check.getCourseTime(), courseDetail.getCourseTime());

                                        if (!weekDayCorrect && !weekNumCorrect && !courseTimeCorrect) {
                                            courseCheckResult.setHasError(true);
                                            courseCheckResult.setCheckCourseName(courses.get(i).getCourseName());
                                            courseCheckResult.setConflictCourseName(courses.get(j).getCourseName());
                                            return courseCheckResult;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        courseCheckResult.setHasError(false);
        return courseCheckResult;
    }

    /**
     * 检查课程详细信息是否有重复
     *
     * @param courseDetails 课程详细信息列表
     * @return 是否有重复
     */
    public static boolean checkCourseDetail(CourseDetail[] courseDetails) {
        if (courseDetails != null) {
            for (int i = 0; i < courseDetails.length; i++) {
                for (int j = 0; j < courseDetails.length; j++) {
                    if (i != j) {
                        if (!checkWeekNumOrCourseTime(courseDetails[i].getWeeks(), courseDetails[j].getWeeks())) {
                            if (!checkWeekNumOrCourseTime(courseDetails[i].getCourseTime(), courseDetails[j].getCourseTime())) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * 检查周数与上课时间重复
     *
     * @param check 需要检查的对象
     * @param find  对比的对象
     * @return 是否有重复
     */
    public static boolean checkWeekNumOrCourseTime(String[] check, String[] find) {
        if (check == null || find == null) {
            return false;
        }
        for (String check_str : check) {
            int max_check;
            int min_check;
            if (check_str.contains("-")) {
                String[] check_arr = check_str.split("-");
                min_check = Integer.valueOf(check_arr[0]);
                max_check = Integer.valueOf(check_arr[1]);
            } else {
                min_check = max_check = Integer.valueOf(check_str);
            }

            for (String find_str : find) {
                if (find_str.contains("-")) {
                    String[] find_arr = find_str.split("-");
                    if (Integer.valueOf(find_arr[0]) <= max_check && Integer.valueOf(find_arr[1]) >= min_check) {
                        return false;
                    }
                } else {
                    if (Integer.valueOf(find_str) >= min_check && Integer.valueOf(find_str) <= max_check) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * 检查上课周数是否有重复
     *
     * @param check 需要检查的
     * @param find  对比的
     * @return 是否有重复
     */
    private static boolean checkWeekDay(int check, int find) {
        return check != find;
    }

    public static class CourseCheckResult {
        private boolean hasError;
        private String checkCourseName;
        private String conflictCourseName;

        CourseCheckResult() {
            this.hasError = false;
            this.checkCourseName = null;
            this.conflictCourseName = null;
        }

        public boolean isHasError() {
            return hasError;
        }

        void setHasError(boolean hasError) {
            this.hasError = hasError;
        }

        public String getCheckCourseName() {
            return checkCourseName;
        }

        void setCheckCourseName(String checkCourseName) {
            this.checkCourseName = checkCourseName;
        }

        public String getConflictCourseName() {
            return conflictCourseName;
        }

        void setConflictCourseName(String conflictCourseName) {
            this.conflictCourseName = conflictCourseName;
        }
    }
}
