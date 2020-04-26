package tool.xfy9326.naucourse.beans

import java.io.Serializable

// 绩点计算项目
data class CreditCountItem(
    // 总评成绩
    val score: Float,
    // 课程学分
    val credit: Float,
    // 课程ID  用于对比重复课程
    val courseId: String?,
    // 课程名称  用于对比重复课程，显示课程选择项
    val courseName: String?,
    // 绩点权值
    val creditWeight: Float = 1f,
    // 是否被选中  用于课程选择项
    var isSelected: Boolean = true
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CreditCountItem

        if (courseId != other.courseId) return false
        if (courseName != other.courseName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = courseId?.hashCode() ?: 0
        result = 31 * result + (courseName?.hashCode() ?: 0)
        return result
    }
}