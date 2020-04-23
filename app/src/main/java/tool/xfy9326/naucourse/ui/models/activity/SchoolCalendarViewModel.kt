package tool.xfy9326.naucourse.ui.models.activity

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.beans.CalendarItem
import tool.xfy9326.naucourse.beans.ImageOperationType
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.providers.contents.methods.www.SchoolCalendarImage
import tool.xfy9326.naucourse.providers.contents.methods.www.SchoolCalendarList
import tool.xfy9326.naucourse.tools.livedata.EventLiveData
import tool.xfy9326.naucourse.ui.models.base.BaseViewModel
import tool.xfy9326.naucourse.utils.utility.ImageUtils

class SchoolCalendarViewModel : BaseViewModel() {
    private val imageOperationMutex = Mutex()
    private val imageLoadMutex = Mutex()

    val imageOperation = EventLiveData<ImageOperationType>()
    val imageShareUri = EventLiveData<Uri>()

    val calendarList = EventLiveData<Array<CalendarItem>>()
    val calendarImageUrl = MutableLiveData<String>()
    val calendarLoadStatus = EventLiveData<CalendarLoadStatus>()

    enum class CalendarLoadStatus {
        IMAGE_LOAD_FAILED,
        IMAGE_LIST_LOAD_FAILED,
        LOADING_IMAGE_LIST
    }

    override fun onInitView(isRestored: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            if (tryInit()) {
                val imageUrl = AppPref.CurrentSchoolCalendarImageUrl
                if (imageUrl != null) {
                    calendarImageUrl.postValue(imageUrl)
                }
            }
            loadCalendarImage(AppPref.CurrentSchoolCalendarUrl?.toHttpUrlOrNull() ?: getDefaultCalendarUrl())
        }
    }

    fun restoreToDefaultImage() {
        viewModelScope.launch(Dispatchers.Default) {
            imageLoadMutex.withLock {
                AppPref.remove(AppPref.CURRENT_SCHOOL_CALENDAR_URL)
                AppPref.remove(AppPref.CURRENT_SCHOOL_CALENDAR_IMAGE_URL)
                loadCalendarImage(getDefaultCalendarUrl(), true)
            }
        }
    }

    // 可能以后地址会改变或者有其他动态获取地址的情况，另外考虑，目前不做修改
    private fun getDefaultCalendarUrl(): HttpUrl {
        return SchoolCalendarImage.CURRENT_TERM_CALENDAR_PAGE_URL
    }

    fun loadCalendarImage(url: HttpUrl, forceRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.Default) {
            imageLoadMutex.withLock {
                SchoolCalendarImage.getContentData(url).let {
                    if (it.isSuccess) {
                        val imageUrl = it.contentData!!.toString()
                        val storeUrl = AppPref.CurrentSchoolCalendarImageUrl
                        if (imageUrl != storeUrl || forceRefresh) {
                            calendarImageUrl.postValue(imageUrl)
                            // 保存当前获取到的校历所在页面的地址以及解析的图片地址
                            AppPref.CurrentSchoolCalendarUrl = url.toString()
                            AppPref.CurrentSchoolCalendarImageUrl = imageUrl
                        }
                    } else if (forceRefresh) {
                        calendarLoadStatus.postEventValue(CalendarLoadStatus.IMAGE_LOAD_FAILED)
                    }
                }
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
                    calendarLoadStatus.postEventValue(CalendarLoadStatus.IMAGE_LIST_LOAD_FAILED)
                }
            }
        }
    }

    fun saveImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            // 直接通过Bitmap存储，防止复制图片的时候出现其他特殊问题
            imageOperationMutex.withLock {
                if (ImageUtils.saveImageToAlbum(Constants.Image.SCHOOL_CALENDAR_IMAGE_NAME, Constants.Image.DIR_APP_IMAGE, bitmap, false)) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_SUCCESS)
                } else {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SAVE_FAILED)
                }
            }
        }
    }

    fun shareImage(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            // 通过存放bitmap的分享方式，而不是直接分享，防止更改分享uri时候使用了旧的图片或者其他情况
            imageOperationMutex.withLock {
                val uri = ImageUtils.createImageShareTemp(Constants.Image.SCHOOL_CALENDAR_IMAGE_NAME, bitmap, false)
                if (uri == null) {
                    imageOperation.postEventValue(ImageOperationType.IMAGE_SHARE_FAILED)
                } else {
                    imageShareUri.postEventValue(uri)
                }
            }
        }
    }
}