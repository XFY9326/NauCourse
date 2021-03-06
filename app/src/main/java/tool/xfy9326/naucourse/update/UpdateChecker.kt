package tool.xfy9326.naucourse.update

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.Response
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.constants.NetworkConst
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.network.SimpleNetworkManager
import tool.xfy9326.naucourse.update.beans.UpdateIndex
import tool.xfy9326.naucourse.update.beans.UpdateInfo
import tool.xfy9326.naucourse.utils.BaseUtils
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils

object UpdateChecker {
    private const val UPDATE_SERVER = "update.xfy9326.top"
    private const val UPDATE_TYPE = "NauCourse"
    private const val UPDATE_VERSION = "Version"
    private const val UPDATE_LATEST = "Latest"
    private const val UPDATE_INDEX = "Index"

    private val LATEST_VERSION_CHECK_URL =
        HttpUrl.Builder().scheme(NetworkConst.HTTPS).host(UPDATE_SERVER).addPathSegment(UPDATE_TYPE).addPathSegment(UPDATE_LATEST).build()
    private val INDEX_VERSION_URL =
        HttpUrl.Builder().scheme(NetworkConst.HTTPS).host(UPDATE_SERVER).addPathSegment(UPDATE_TYPE).addPathSegment(UPDATE_INDEX).build()

    suspend fun getNewUpdateInfo(forceCheck: Boolean = false): Pair<Boolean, UpdateInfo?>? = withContext(Dispatchers.IO) {
        if (BaseUtils.isBeta()) {
            return@withContext Pair(false, null)
        }
        try {
            getLatestVersionInfo()?.let {
                if (it.versionCode > BuildConfig.VERSION_CODE) {
                    val fixedData = if (!it.forceUpdate) {
                        it.copy(forceUpdate = checkForceUpdate())
                    } else {
                        it
                    }
                    if (fixedData.forceUpdate) {
                        AppPref.ForceUpdateVersionCode = fixedData.versionCode
                    } else {
                        AppPref.remove(AppPref.FORCE_UPDATE_VERSION_CODE)
                        if (!forceCheck && it.versionCode == AppPref.IgnoreUpdateVersionCode) {
                            return@withContext Pair(false, null)
                        }
                    }
                    return@withContext Pair(true, fixedData)
                } else {
                    AppPref.remove(AppPref.FORCE_UPDATE_VERSION_CODE)
                }
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<UpdateChecker>(e)
            return@withContext null
        }
        return@withContext Pair(false, null)
    }

    fun clearOldUpdatePref() {
        AppPref.remove(AppPref.IGNORE_UPDATE_VERSION_CODE)
        AppPref.remove(AppPref.FORCE_UPDATE_VERSION_CODE)
    }

    private fun checkForceUpdate(): Boolean {
        val index = getUpdateIndex()
        for (i in index) {
            if (i.version > BuildConfig.VERSION_CODE && i.forceUpdate) {
                return true
            }
        }
        return false
    }

    private fun getLatestVersionInfo(): UpdateInfo? {
        SimpleNetworkManager.getClient().newClientCall(LATEST_VERSION_CHECK_URL).use {
            return parseUpdateInfo(it)
        }
    }

    private fun getUpdateIndex(): Array<UpdateIndex> {
        SimpleNetworkManager.getClient().newClientCall(INDEX_VERSION_URL).use {
            return parseIndexInfo(it)
        }
    }

    @Suppress("unused")
    fun getSpecificVersionInfo(version: Int): UpdateInfo? {
        try {
            SimpleNetworkManager.getClient().newClientCall(buildSpecificVersionInfoUrl(version)).use {
                return parseUpdateInfo(it)
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<UpdateChecker>(e)
        }
        return null
    }

    private fun parseIndexInfo(response: Response): Array<UpdateIndex> {
        if (response.isSuccessful && response.code != 404) {
            response.body?.string()?.let {
                try {
                    return Gson().fromJson(it, Array<UpdateIndex>::class.java)
                } catch (e: Exception) {
                    ExceptionUtils.printStackTrace<UpdateChecker>(e)
                }
            }
        }
        return emptyArray()
    }

    private fun parseUpdateInfo(response: Response): UpdateInfo? {
        if (response.isSuccessful && response.code != 404) {
            response.body?.string()?.let {
                try {
                    return Gson().fromJson(it, UpdateInfo::class.java)
                } catch (e: Exception) {
                    ExceptionUtils.printStackTrace<UpdateChecker>(e)
                }
            }
        }
        return null
    }

    private fun buildSpecificVersionInfoUrl(version: Int) =
        HttpUrl.Builder().scheme(NetworkConst.HTTPS).host(UPDATE_SERVER).addPathSegment(UPDATE_TYPE)
            .addPathSegment(UPDATE_VERSION).addPathSegment(version.toString()).build()
}