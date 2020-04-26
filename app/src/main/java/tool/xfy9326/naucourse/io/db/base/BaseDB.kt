package tool.xfy9326.naucourse.io.db.base

import androidx.annotation.CallSuper
import androidx.room.Room
import androidx.room.RoomDatabase
import tool.xfy9326.naucourse.App

// 基础数据库
abstract class BaseDB<T : RoomDatabase> {
    // 数据库名称
    abstract val dbName: String

    // 数据库Class
    abstract val dbClass: Class<T>

    // 数据库
    private var db: T? = null

    // 获取数据库
    @Synchronized
    fun getDB(): T {
        if (db == null) {
            db = Room.databaseBuilder(App.instance, dbClass, dbName).build()
        }
        return db!!
    }

    abstract class DB : RoomDatabase() {
        // 清空所有数据
        @CallSuper
        open fun clearAll() {
            clearAllTables()
        }
    }
}