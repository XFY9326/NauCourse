package tool.xfy9326.naucourse.tools.glide

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.signature.ObjectKey
import java.io.InputStream

class LoginClientModelLoader : ModelLoader<ClientRequest, InputStream> {
    override fun buildLoadData(model: ClientRequest, width: Int, height: Int, options: Options): ModelLoader.LoadData<InputStream> {
        return ModelLoader.LoadData(ObjectKey(model), ClientDataFetcher(model))
    }

    override fun handles(model: ClientRequest): Boolean = true
}