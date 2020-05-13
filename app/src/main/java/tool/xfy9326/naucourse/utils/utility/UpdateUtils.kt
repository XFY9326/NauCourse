package tool.xfy9326.naucourse.utils.utility

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.update.UpdateChecker
import tool.xfy9326.naucourse.update.beans.UpdateInfo

object UpdateUtils {
    suspend fun checkUpdate(event: EventLiveData<UpdateInfo>, autoUpdateFunctionCheck: Boolean = false) = withContext(Dispatchers.IO) {
        if (!autoUpdateFunctionCheck || SettingsPref.AutoCheckUpdates) {
            UpdateChecker.getNewUpdateInfo()?.let {
                if (it.first) event.postEventValue(it.second!!)
            }
        }
    }
}