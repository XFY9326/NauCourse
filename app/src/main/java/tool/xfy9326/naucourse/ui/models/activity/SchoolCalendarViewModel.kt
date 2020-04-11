package tool.xfy9326.naucourse.ui.models.activity

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.CalendarItem
import tool.xfy9326.naucourse.beans.ImageOperationType
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.network.SimpleNetworkManager
import tool.xfy9326.naucourse.providers.contents.methods.www.SchoolCalendarImage
import tool.xfy9326.naucourse.providers.contents.methods.www.SchoolCalendarList
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.debug.LogUtils
import tool.xfy9326.naucourse.utils.utility.ImageUtils

class SchoolCalendarViewModel : BaseViewModel() {
    var calendarHasSet = false

    private val imageOperationMutex = Mutex()
    private val imageLoadMutex = Mutex()

    val imageOperation = EventLiveData<ImageOperationType>()
    val imageShareUri = EventLiveData<Uri>()

    val calendarList = EventLiveData<Array<CalendarItem>>()
    val calendarImage = EventLiveData<Bitmap?>()
    val calendarLoadStatus = EventLiveData<CalendarLoadStatus>()

    enum class CalendarLoadStatus {
        IMAGE_LOAD_FAILED,
        IMAGE_LIST_LOAD_FAILED,
        LOADING_IMAGE_LIST
    }

    override fun onInitView(isRestored: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            val bitmap = tryLoadCalendarFromLocal()
            if (bitmap != null) {
                calendarImage.postEventValue(bitmap)
            } else {
                LogUtils.d<SchoolCalendarViewModel>("School Calendar Image Temp Load Failed!")
            }
            val currentUseUrl = AppPref.CurrentSchoolCalendarUrl?.toHttpUrlOrNull() ?: SchoolCalendarImage.CURRENT_TERM_CALENDAR_PAGE_URL
            loadCalendarImage(currentUseUrl, bitmap != null)
        }
    }

    fun restoreToDefaultImage() {
        viewModelScope.launch(Dispatchers.Default) {
            imageLoadMutex.withLock {
                AppPref.remove(AppPref.CURRENT_SCHOOL_CALENDAR_URL)
                AppPref.remove(AppPref.CURRENT_SCHOOL_CALENDAR_IMAGE_URL)
                loadCalendarImage(SchoolCalendarImage.CURRENT_TERM_CALENDAR_PAGE_URL, false)
            }
        }
    }

    fun loadCalendarImage(url: HttpUrl, tempLoadSuccess: Boolean = true) {
        viewModelScope.launch(Dispatchers.Default) {
            imageLoadMutex.withLock {
                SchoolCalendarImage.getContentData(url).let {
                    if (it.isSuccess) {
                        val imageUrl = it.contentData!!.toString()
                        val storeUrl = AppPref.CurrentSchoolCalendarImageUrl
                        if (!tempLoadSuccess || imageUrl != storeUrl) {
                            val bitmap = SimpleNetworkManager.getClient().getBitmapFromUrl(it.contentData)
                            if (bitmap != null) {
                                val uri = ImageUtils.saveImage(
                                    Constants.Image.SCHOOL_CALENDAR_IMAGE_NAME,
                                    bitmap,
                                    recycle = false,
                                    compressFormat = Bitmap.CompressFormat.WEBP,
                                    dirName = Constants.Image.DIR_APP_IMAGE
                                )
                                if (uri == null) {
                                    LogUtils.d<SchoolCalendarViewModel>("School Calendar Image Save Failed!")
                                }
                                AppPref.CurrentSchoolCalendarUrl = url.toString()
                                AppPref.CurrentSchoolCalendarImageUrl = imageUrl
                                calendarImage.postEventValue(bitmap)
                            } else {
                                LogUtils.d<SchoolCalendarViewModel>("School Calendar Download Failed!")
                                calendarLoadStatus.postEventValue(CalendarLoadStatus.IMAGE_LOAD_FAILED)
                            }
                        }
                    } else {
                        LogUtils.d<SchoolCalendarViewModel>("School Calendar Load Failed! Reason: ${it.contentErrorResult}")
                        calendarLoadStatus.postEventValue(CalendarLoadStatus.IMAGE_LOAD_FAILED)
                    }
                }
            }
        }
    }

    private suspend fun tryLoadCalendarFromLocal(): Bitmap? = withContext(Dispatchers.IO) {
        imageLoadMutex.withLock {
            return@withContext if (AppPref.contains(AppPref.CURRENT_SCHOOL_CALENDAR_IMAGE_URL)) {
                val bitmap = if (ImageUtils.localImageExists(Constants.Image.SCHOOL_CALENDAR_IMAGE_NAME, Constants.Image.DIR_APP_IMAGE)) {
                    ImageUtils.readLocalImage(Constants.Image.SCHOOL_CALENDAR_IMAGE_NAME, Constants.Image.DIR_APP_IMAGE)
                } else {
                    null
                }
                if (bitmap == null) {
                    AppPref.remove(AppPref.CURRENT_SCHOOL_CALENDAR_IMAGE_URL)
                }
                bitmap
            } else {
                null
            }
        }
    }

    fun getCalendarList() {
        viewModelScope.launch(Dispatchers.Default) {
            calendarLoadStatus.postEventValue(CalendarLoadStatus.LOADING_IMAGE_LIST)
            SchoolCalendarList.getContentData().let {
                if (it.isSuccess) {
                    calendarList.postEventValue(it.contentData!!)
                } else {
                    LogUtils.d<SchoolCalendarViewModel>("School Calendar List Load Failed! Reason: ${it.contentErrorResult}")
                    calendarLoadStatus.postEventValue(CalendarLoadStatus.IMAGE_LIST_LOAD_FAILED)
                }
            }
        }
    }

    fun saveImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            imageOperationMutex.withLock {
                val uri = ImageUtils.saveImage(
                    Constants.Image.SCHOOL_CALENDAR_IMAGE_NAME,
                    bitmap,
                    recycle = false,
                    saveToLocal = false,
                    dirName = Constants.Image.DIR_APP_IMAGE,
                    addFileNameTypePrefix = true
                )
                if (uri == null) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_FAILED)
                } else {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_SUCCESS)
                }
            }
        }
    }

    fun shareImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            imageOperationMutex.withLock {
                val uri = ImageUtils.saveImage(
                    Constants.Image.SCHOOL_CALENDAR_IMAGE_NAME,
                    bitmap,
                    recycle = false,
                    dirName = Constants.Image.DIR_SHARE_TEMP_IMAGE,
                    fileProviderUri = true,
                    addFileNameTypePrefix = true
                )
                if (uri == null) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SHARE_FAILED)
                } else {
                    imageShareUri.postEventValue(uri)
                }
            }
        }
    }
}