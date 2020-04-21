package tool.xfy9326.naucourse.tools.glide

import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Response
import okhttp3.internal.closeQuietly
import tool.xfy9326.naucourse.network.LoginNetworkManager
import java.io.InputStream

class ClientDataFetcher(private val model: ClientRequest) : DataFetcher<InputStream> {
    private val client = LoginNetworkManager.getClient(model.clientType)
    private var response: Response? = null
    private var inputStream: InputStream? = null

    override fun getDataClass(): Class<InputStream> = InputStream::class.java

    override fun getDataSource(): DataSource = DataSource.REMOTE

    override fun cleanup() {
        response?.closeQuietly()
        inputStream?.closeQuietly()
    }

    override fun cancel() {}

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        try {
            response = client.newAutoLoginCall(model.url.toHttpUrl()).apply {
                inputStream = body?.byteStream()
                callback.onDataReady(inputStream)
            }
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }
}