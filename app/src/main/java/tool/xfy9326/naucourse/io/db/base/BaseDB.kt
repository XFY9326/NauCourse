package tool.xfy9326.naucourse.io.db.base

import androidx.annotation.CallSuper
import androidx.room.Room
import androidx.room.RoomDatabase
import tool.xfy9326.naucourse.App

abstract class BaseDB<T : RoomDatabase> {
    abstract val dbName: String
    abstract val dbClass: Class<T>

    private var db: T? = null

    @Synchronized
    fun getDB(): T {
        if (db == null) {
            db = Room.databaseBuilder(App.instance, dbClass, dbName).build()
        }
        return db!!
    }

    abstract class DB : RoomDatabase() {
        @CallSuper
        open fun clearAll() {
            clearAllTables()
        }
    }
}