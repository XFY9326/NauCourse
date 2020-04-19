package tool.xfy9326.naucourse.update.beans

import java.io.Serializable

data class UpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val forceUpdate: Boolean,
    val changeLog: String,
    val downloadSource: Array<DownloadSource>
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UpdateInfo

        if (versionCode != other.versionCode) return false
        if (versionName != other.versionName) return false
        if (forceUpdate != other.forceUpdate) return false
        if (changeLog != other.changeLog) return false
        if (!downloadSource.contentEquals(other.downloadSource)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = versionCode
        result = 31 * result + versionName.hashCode()
        result = 31 * result + forceUpdate.hashCode()
        result = 31 * result + changeLog.hashCode()
        result = 31 * result + downloadSource.contentHashCode()
        return result
    }
}