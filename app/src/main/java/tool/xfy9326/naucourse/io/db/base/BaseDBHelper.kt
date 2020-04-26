package tool.xfy9326.naucourse.io.db.base

import androidx.room.RoomDatabase

// 基础数据库帮助类
abstract class BaseDBHelper<T : RoomDatabase> {
    // 数据库
    protected abstract val db: T

    // 清空所有数据
    abstract fun clearAll()
}