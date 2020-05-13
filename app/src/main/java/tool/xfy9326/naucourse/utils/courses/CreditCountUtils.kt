package tool.xfy9326.naucourse.utils.courses

import tool.xfy9326.naucourse.beans.CreditCountItem
import tool.xfy9326.naucourse.providers.beans.jwc.CourseHistory
import tool.xfy9326.naucourse.providers.beans.jwc.CourseScore

object CreditCountUtils {
    fun countCredit(current: List<CreditCountItem>, history: List<CreditCountItem>) =
        getCredit(HashSet(current).apply {
            addAll(history)
        })

    private fun getCredit(courseSet: Set<CreditCountItem>): Float {
        var temp = 0f
        var totalStudyScore = 0f
        for (course in courseSet) {
            temp += if (course.score < 60) {
                0f
            } else {
                (course.score - 50) / 10f * course.creditWeight * course.credit
            }
            totalStudyScore += course.credit
        }
        return temp / totalStudyScore
    }

    fun getCountItemFromCourseHistory(arr: List<CourseHistory>) =
        ArrayList<CreditCountItem>(arr.size).apply {
            for (courseHistory in arr) {
                if (courseHistory.creditWeight > 0 && courseHistory.score != null) {
                    add(
                        CreditCountItem(
                            courseHistory.score,
                            courseHistory.credit,
                            courseHistory.courseId,
                            courseHistory.name,
                            courseHistory.creditWeight
                        )
                    )
                }
            }
        }

    fun getCountItemFromCourseScore(arr: List<CourseScore>) =
        ArrayList<CreditCountItem>(arr.size).apply {
            for (courseScore in arr) {
                if (!courseScore.notPublish && !courseScore.notMeasure && !courseScore.notEntry) {
                    add(CreditCountItem(courseScore.overAllGrades, courseScore.credit, courseScore.courseId, courseScore.name))
                }
            }
        }
}