package tool.xfy9326.naucourse.methods;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.methods.netInfoMethods.CourseSearchMethod;
import tool.xfy9326.naucourse.methods.netInfoMethods.TableMethod;
import tool.xfy9326.naucourse.utils.Course;
import tool.xfy9326.naucourse.utils.CourseDetail;
import tool.xfy9326.naucourse.utils.CourseSearchDetail;

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
        courseArrayList = combineCourseList(courseSearchArrayList, courseArrayList, termCheck, true, true);
        boolean detailTimeCheck = true;
        for (Course course : courseArrayList) {
            if (!checkCourseDetail(course.getCourseDetail())) {
                detailTimeCheck = false;
                break;
            }
        }
        CourseCheckResult courseCheckResult;
        if (detailTimeCheck) {
            courseCheckResult = checkCourseList(courseArrayList);
        } else {
            courseCheckResult = new CourseCheckResult();
            courseCheckResult.setHasError();
        }
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
     * @param combineColor     是否合并颜色
     * @return 课程信息列表
     */
    public static ArrayList<Course> combineCourseList(ArrayList<Course> combineCourses, ArrayList<Course> combineToCourses, boolean termCheck, boolean combineDetail, boolean combineColor) {
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
                        if (combineColor) {
                            course.setCourseColor(result.get(i).getCourseColor());
                        }
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
        try {
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
                                        if (isCourseDetailTimeError(courseDetail, courseDetail_check)) {
                                            courseCheckResult.setHasError();
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
        } catch (Exception e) {
            e.printStackTrace();
            courseCheckResult.setHasError();
        }
        return courseCheckResult;
    }

    /**
     * 检查课程详细信息是否有重复
     *
     * @param courseDetails 课程详细信息列表
     * @return 是否有重复
     */
    public static boolean checkCourseDetail(@Nullable CourseDetail[] courseDetails) {
        if (courseDetails != null) {
            for (int i = 0; i < courseDetails.length; i++) {
                for (int j = 0; j < courseDetails.length; j++) {
                    if (i != j) {
                        if (isCourseDetailTimeError(courseDetails[i], courseDetails[j])) {
                            return false;
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
     * @param courseDetail1 CourseDetail
     * @param courseDetail2 CourseDetail
     * @return 是否有重复
     */
    private static boolean isCourseDetailTimeError(@Nullable CourseDetail courseDetail1, @Nullable CourseDetail courseDetail2) {
        if (courseDetail1 == null || courseDetail2 == null) {
            return true;
        }
        int weekMode1 = courseDetail1.getWeekMode();
        int weekMode2 = courseDetail2.getWeekMode();
        if (weekMode1 == Config.COURSE_DETAIL_WEEKMODE_SINGLE && weekMode2 == Config.COURSE_DETAIL_WEEKMODE_DOUBLE ||
                weekMode2 == Config.COURSE_DETAIL_WEEKMODE_SINGLE && weekMode1 == Config.COURSE_DETAIL_WEEKMODE_DOUBLE) {
            return false;
        }
        if (courseDetail1.getWeekDay() != courseDetail2.getWeekDay()) {
            return false;
        }
        boolean noSameTime = checkNoSameTime(courseDetail1.getCourseTime(), courseDetail2.getCourseTime());
        if (noSameTime) {
            return false;
        }

        int[] week1 = convertWeekToArr(courseDetail1.getWeeks(), weekMode1);
        int[] week2 = convertWeekToArr(courseDetail2.getWeeks(), weekMode2);
        if (week1 == null || week2 == null) {
            return true;
        }

        for (int i = 0; i < week1.length && i < week2.length; i++) {
            if (week1[i] == 1 && week2[i] == 1) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private static int[] convertWeekToArr(@Nullable String[] weeks, int mode) {
        int[] result = new int[Config.DEFAULT_MAX_WEEK];
        if (weeks == null) {
            return null;
        }
        try {
            for (String week : weeks) {
                int min, max;
                if (week.contains("-")) {
                    String[] arr = week.split("-");
                    min = Integer.valueOf(arr[0]);
                    max = Integer.valueOf(arr[1]);
                } else {
                    min = max = Integer.valueOf(week);
                }
                for (int i = min; i <= max; i++) {
                    if (mode == Config.COURSE_DETAIL_WEEKMODE_SINGLE) {
                        if (i % 2 != 0) {
                            result[i] = 1;
                        }
                    } else if (mode == Config.COURSE_DETAIL_WEEKMODE_DOUBLE) {
                        if (i % 2 == 0) {
                            result[i] = 1;
                        }
                    } else {
                        result[i] = 1;
                    }
                }
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
        return result;
    }

    public static boolean checkNoSameTime(String[] time1, String[] time2) {
        if (time1 != null && time2 != null) {
            System.out.println("1: " + Arrays.toString(time1));
            System.out.println("2: " + Arrays.toString(time2));
            try {
                for (String t1 : time1) {
                    for (String t2 : time2) {
                        if (t1.contains("-")) {
                            String[] arr1 = t1.split("-");
                            int min1 = Integer.valueOf(arr1[0]);
                            int max1 = Integer.valueOf(arr1[1]);
                            if (t2.contains("-")) {
                                String[] arr2 = t2.split("-");
                                int min2 = Integer.valueOf(arr2[0]);
                                int max2 = Integer.valueOf(arr2[1]);
                                if (min1 > min2) {
                                    if (min1 == max2) {
                                        return false;
                                    } else if (min1 < max2) {
                                        return false;
                                    }
                                } else if (min2 > min1) {
                                    if (min2 == max1) {
                                        return false;
                                    } else if (min2 < max1) {
                                        return false;
                                    }
                                } else {
                                    return false;
                                }
                            } else {
                                int s2 = Integer.valueOf(t2);
                                if (min1 <= s2 && s2 <= max1) {
                                    return false;
                                }
                            }
                        } else {
                            int s1 = Integer.valueOf(t1);
                            if (t2.contains("-")) {
                                String[] arr2 = t2.split("-");
                                int min2 = Integer.valueOf(arr2[0]);
                                int max2 = Integer.valueOf(arr2[1]);
                                if (min2 <= s1 && s1 <= max2) {
                                    return false;
                                }
                            } else {
                                int s2 = Integer.valueOf(t2);
                                if (s1 == s2) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
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

        void setHasError() {
            this.hasError = true;
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
