package tool.xfy9326.naucourse.Methods;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.CourseSearchMethod;
import tool.xfy9326.naucourse.Methods.NetInfoMethods.TableMethod;
import tool.xfy9326.naucourse.Utils.Course;
import tool.xfy9326.naucourse.Utils.CourseDetail;
import tool.xfy9326.naucourse.Utils.CourseSearchDetail;

public class CourseEditMethod {

    /**
     * 添加并保存搜索到的课程
     *
     * @param context            Context
     * @param termCheck          新学期的课程强制替换(以最新的学期为准，课表中只会存在一种学期的课程)
     * @param courseSearchDetail 搜索课程的信息
     * @return 添加结果
     */
    public static AddSearchCourseResult addSearchCourse(Context context, boolean termCheck, CourseSearchDetail courseSearchDetail) {
        AddSearchCourseResult addSearchCourseResult = new AddSearchCourseResult();
        ArrayList<Course> courseSearchArrayList = new ArrayList<>();
        courseSearchArrayList.add(CourseSearchMethod.convertToCourse(context, courseSearchDetail));
        ArrayList<Course> courseArrayList = DataMethod.getOfflineTableData(context);
        if (courseArrayList == null) {
            courseArrayList = new ArrayList<>();
        }
        courseArrayList = combineCourseList(courseSearchArrayList, courseArrayList, termCheck, true);
        CourseCheckResult courseCheckResult = checkCourseList(courseArrayList);

        if (!courseCheckResult.isHasError()) {
            addSearchCourseResult.setSaveSuccess(DataMethod.saveOfflineData(context, courseArrayList, TableMethod.FILE_NAME, false, TableMethod.IS_ENCRYPT));
        }
        addSearchCourseResult.setCourseCheckResult(courseCheckResult);
        addSearchCourseResult.setAddSuccess(!courseCheckResult.isHasError());

        return addSearchCourseResult;
    }

    /**
     * 课程信息列表按照ID拼接
     *
     * @param combineCourses   需要并入的课表
     * @param combineToCourses 原课表
     * @param termCheck        新学期的课程强制替换(以最新的学期为准，课表中只会存在一种学期的课程)
     * @param combineDetail    是否合并课程时间等信息
     * @return 课程信息列表
     */
    public static ArrayList<Course> combineCourseList(ArrayList<Course> combineCourses, ArrayList<Course> combineToCourses, boolean termCheck, boolean combineDetail) {
        long newTerm = 0;
        if (termCheck) {
            //获取最新的学期
            long newTerm1 = 0;
            long newTerm2 = 0;
            for (Course course : combineCourses) {
                long courseTerm = Long.valueOf(course.getCourseTerm());
                if (courseTerm > newTerm1) {
                    newTerm1 = courseTerm;
                }
            }
            for (Course course : combineToCourses) {
                long courseTerm = Long.valueOf(course.getCourseTerm());
                if (courseTerm > newTerm2) {
                    newTerm2 = courseTerm;
                }
            }
            if (newTerm1 > newTerm2) {
                newTerm = newTerm1;
            } else {
                newTerm = newTerm2;
            }
        }

        ArrayList<Course> result = new ArrayList<>(combineToCourses);
        for (Course course : combineCourses) {
            if (result.isEmpty()) {
                result.add(course);
            } else {
                boolean found = false;
                for (int i = 0; i < result.size() && !found; i++) {
                    String id = course.getCourseId();
                    if (id != null && id.equalsIgnoreCase(result.get(i).getCourseId())) {
                        course.setCourseColor(result.get(i).getCourseColor());
                        if (combineDetail) {
                            CourseDetail[] resultCourseDetail = result.get(i).getCourseDetail();
                            if (resultCourseDetail != null) {
                                if (course.getCourseDetail() != null) {
                                    ArrayList<CourseDetail> courseDetails = new ArrayList<>(Arrays.asList(course.getCourseDetail()));
                                    courseDetails.addAll(Arrays.asList(resultCourseDetail));
                                    course.setCourseDetail(courseDetails.toArray(new CourseDetail[]{}));
                                } else {
                                    course.setCourseDetail(result.get(i).getCourseDetail());
                                }
                            }
                        }
                        result.set(i, course);
                        found = true;
                    }
                }
                if (!found) {
                    result.add(course);
                }
            }
        }
        combineCourses.clear();
        if (termCheck) {
            Iterator<Course> iterator = result.iterator();
            while (iterator.hasNext()) {
                long courseTerm = Long.valueOf(iterator.next().getCourseTerm());
                if (courseTerm == 0L || courseTerm < newTerm) {
                    iterator.remove();
                }
            }
        }
        return result;
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
                    if (courses.get(i).getCourseTerm() != null && !courses.get(i).getCourseTerm().equals(courses.get(j).getCourseTerm())) {
                        continue;
                    }
                    CourseDetail[] courseDetail_list_check = courses.get(i).getCourseDetail();
                    if (courseDetail_list_check != null) {
                        for (CourseDetail courseDetail_check : courseDetail_list_check) {
                            if (courseDetail_check == null) {
                                continue;
                            }
                            CourseDetail[] courseDetail_list = courses.get(j).getCourseDetail();
                            if (courseDetail_list != null) {
                                for (CourseDetail courseDetail : courseDetail_list) {
                                    if (courseDetail == null) {
                                        continue;
                                    }
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

    public static class AddSearchCourseResult {
        private CourseCheckResult courseCheckResult;
        private boolean saveSuccess;
        private boolean addSuccess;

        AddSearchCourseResult() {
            this.saveSuccess = false;
            this.addSuccess = false;
        }

        public CourseCheckResult getCourseCheckResult() {
            return courseCheckResult;
        }

        void setCourseCheckResult(CourseCheckResult courseCheckResult) {
            this.courseCheckResult = courseCheckResult;
        }

        public boolean isSaveSuccess() {
            return saveSuccess;
        }

        void setSaveSuccess(boolean saveSuccess) {
            this.saveSuccess = saveSuccess;
        }

        public boolean isAddSuccess() {
            return addSuccess;
        }

        void setAddSuccess(boolean addSuccess) {
            this.addSuccess = addSuccess;
        }
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
