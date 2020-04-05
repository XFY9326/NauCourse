package tool.xfy9326.naucourse.beans

import android.graphics.Bitmap
import java.io.Serializable
import java.nio.ByteBuffer

data class BitmapSerializable internal constructor(
    private val bitmapByteArray: ByteArray,
    private val bitmapWidth: Int,
    private val bitmapHeight: Int,
    private val bitmapConfig: Bitmap.Config
) : Serializable {
    companion object {
        fun create(bitmap: Bitmap): BitmapSerializable {
            val buffer = ByteBuffer.allocate(bitmap.byteCount)
            bitmap.copyPixelsToBuffer(buffer)
            val byteArray = buffer.array()
            return BitmapSerializable(byteArray, bitmap.width, bitmap.height, bitmap.config)
        }
    }

    fun getBitmap(): Bitmap {
        val newBitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, bitmapConfig)
        newBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bitmapByteArray))
        return newBitmap
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BitmapSerializable

        if (!bitmapByteArray.contentEquals(other.bitmapByteArray)) return false
        if (bitmapWidth != other.bitmapWidth) return false
        if (bitmapHeight != other.bitmapHeight) return false
        if (bitmapConfig != other.bitmapConfig) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bitmapByteArray.contentHashCode()
        result = 31 * result + bitmapWidth
        result = 31 * result + bitmapHeight
        result = 31 * result + bitmapConfig.hashCode()
        return result
    }
}