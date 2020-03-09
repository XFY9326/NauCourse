package tool.xfy9326.naucourses.io.json

import tool.xfy9326.naucourses.beans.CourseArrange
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.beans.CourseTable
import tool.xfy9326.naucourses.beans.UUIDContent
import tool.xfy9326.naucourses.providers.beans.jwc.StudentInfo

enum class GsonStoreType {
    STUDENT_INFO {
        override val versionCode: Int = 1
        override val storeClass: Class<*> = StudentInfo::class.java
    },
    COURSE_TABLE {
        override val versionCode: Int = 1
        override val storeClass: Class<*> = Array<CourseTable>::class.java
    },
    COURSE_STYLE {
        override val versionCode: Int = 1
        override val storeClass: Class<*> = Array<CourseCellStyle>::class.java
    },
    UUID {
        override val versionCode: Int = 1
        override val storeClass: Class<*> = UUIDContent::class.java
    },
    COURSE_ARRANGE {
        override val versionCode: Int = 1
        override val storeClass: Class<*> = CourseArrange::class.java
    };

    abstract val storeClass: Class<*>
    abstract val versionCode: Int
}