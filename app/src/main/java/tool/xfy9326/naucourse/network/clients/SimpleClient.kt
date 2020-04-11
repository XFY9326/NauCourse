package tool.xfy9326.naucourse.network.clients

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import tool.xfy9326.naucourse.network.clients.base.BaseNetworkClient
import tool.xfy9326.naucourse.ui.models.activity.ImageShowViewModel
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils

class SimpleClient : BaseNetworkClient() {
    private val okHttpClient = OkHttpClient()

    override fun getNetworkClient(): OkHttpClient = okHttpClient

    override fun newClientCall(request: Request): Response = okHttpClient.newCall(request).execute()

    fun getBitmapFromUrl(url: HttpUrl): Bitmap? {
        try {
            newClientCall(url).use {
                if (it.isSuccessful) {
                    if (it.body?.byteStream() != null) {
                        it.body?.byteStream()?.let { input ->
                            return BitmapFactory.decodeStream(input)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<ImageShowViewModel>(e)
        }
        return null
    }
}