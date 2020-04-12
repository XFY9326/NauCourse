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
            val bitmap = tryLoadCalendarTemp()
            if (bitmap != null) {
                calendarImage.postEventValue(bitmap)
            } else {
                LogUtils.d<SchoolCalendarViewModel>("School Calendar Image Temp Load Failed!")
            }
            // 默认先从指定位置读取校历，如果有其他设置就从其他地方读取
            val currentUseUrl = AppPref.CurrentSchoolCalendarUrl?.toHttpUrlOrNull() ?: getDefaultCalendarUrl()
            loadCalendarImage(currentUseUrl, bitmap != null)
        }
    }

    fun restoreToDefaultImage() {
        viewModelScope.launch(Dispatchers.Default) {
            imageLoadMutex.withLock {
                AppPref.remove(AppPref.CURRENT_SCHOOL_CALENDAR_URL)
                AppPref.remove(AppPref.CURRENT_SCHOOL_CALENDAR_IMAGE_URL)
                loadCalendarImage(getDefaultCalendarUrl(), false)
            }
        }
    }

    private fun getDefaultCalendarUrl(): HttpUrl {
        // 可能以后地址会改变或者有其他动态获取地址的情况，另外考虑，目前不做修改
        return SchoolCalendarImage.CURRENT_TERM_CALENDAR_PAGE_URL
    }

    fun loadCalendarImage(url: HttpUrl, tempLoadSuccess: Boolean = true) {
        viewModelScope.launch(Dispatchers.Default) {
            imageLoadMutex.withLock {
                // 此处不使用ContentInfo的缓存功能，因为校历实时性要求较高
                // 如果以后要改动，应该使用MultiContentInfo实现默认校历以及校历列表以及图片的下载与缓存
                SchoolCalendarImage.getContentData(url).let {
                    if (it.isSuccess) {
                        val imageUrl = it.contentData!!.toString()
                        val storeUrl = AppPref.CurrentSchoolCalendarImageUrl
                        // 缓存加载不成功或者图片更新时才会下载新的图片
                        if (!tempLoadSuccess || imageUrl != storeUrl) {
                            val bitmap = SimpleNetworkManager.getClient().getBitmapFromUrl(it.contentData)
                            if (bitmap != null) {
                                // 图片保存到外置存储
                                // 如果要改为缓存应该放在内置存储
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
                                // 保存当前获取到的校历所在页面的地址以及解析的图片地址
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

    private suspend fun tryLoadCalendarTemp(): Bitmap? = withContext(Dispatchers.IO) {
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