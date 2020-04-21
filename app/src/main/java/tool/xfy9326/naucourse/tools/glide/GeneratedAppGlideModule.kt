package tool.xfy9326.naucourse.tools.glide

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.module.AppGlideModule
import java.io.File
import java.io.InputStream

@GlideModule
class GeneratedAppGlideModule : AppGlideModule() {
    companion object {
        private const val DISK_CACHE_SIZE = 1024 * 1024 * 50L
        private const val DISK_CACHE_FOLDER_NAME = "GlideImageCache"
    }

    override fun isManifestParsingEnabled(): Boolean = false

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(DiskLruCacheFactory(File(context.cacheDir, DISK_CACHE_FOLDER_NAME).absolutePath, DISK_CACHE_SIZE))
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.prepend(ClientRequest::class.java, InputStream::class.java, LoginClientModelLoaderFactory())
    }
}