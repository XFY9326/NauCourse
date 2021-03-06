package tool.xfy9326.naucourse.tools.glide

import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import java.io.InputStream

// 指定客户端获取图片
class LoginClientModelLoaderFactory : ModelLoaderFactory<ClientRequest, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<ClientRequest, InputStream> = LoginClientModelLoader()

    override fun teardown() {}
}