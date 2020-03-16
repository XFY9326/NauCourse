package tool.xfy9326.naucourses.io.gson

import tool.xfy9326.naucourses.beans.*
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
    },
    CARD_BALANCE {
        override val versionCode: Int = 1
        override val storeClass: Class<*> = CardBalance::class.java
    };

    abstract val storeClass: Class<*>
    abstract val versionCode: Int
}