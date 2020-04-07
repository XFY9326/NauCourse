package tool.xfy9326.naucourse.network.beans

import androidx.room.ColumnInfo
import androidx.room.Entity
import tool.xfy9326.naucourse.io.db.NetworkDBHelper

@Entity(tableName = NetworkDBHelper.NGX_COOKIES_TABLE_NAME, primaryKeys = [NetworkDBHelper.COLUMN_HOST, NetworkDBHelper.COLUMN_NAME])
data class NGXCookieData(
    @ColumnInfo(name = NetworkDBHelper.COLUMN_HOST)
    override val host: String,
    @ColumnInfo(name = NetworkDBHelper.COLUMN_NAME)
    override val name: String,
    override val value: String,
    override val expiresAt: Long,
    override val domain: String,
    override val path: String,
    override val secure: Boolean,
    override val httpOnly: Boolean,
    override val hostOnly: Boolean,
    override val persistent: Boolean
) : CookieData()