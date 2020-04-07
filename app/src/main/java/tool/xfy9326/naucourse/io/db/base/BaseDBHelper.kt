package tool.xfy9326.naucourse.io.db.base

import androidx.room.RoomDatabase

abstract class BaseDBHelper<T : RoomDatabase> {
    protected abstract val db: T
    abstract fun clearAll()
}