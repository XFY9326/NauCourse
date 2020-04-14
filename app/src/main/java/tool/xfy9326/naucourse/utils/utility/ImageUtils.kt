package tool.xfy9326.naucourse.utils.utility

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Rect
import androidx.core.content.ContextCompat
import androidx.core.graphics.applyCanvas
import tool.xfy9326.naucourse.R

object ImageUtils {
    fun drawDefaultWaterPrint(context: Context, bitmap: Bitmap) = drawWaterPrint(context, bitmap, context.getString(R.string.water_print_text))

    private fun drawWaterPrint(context: Context, bitmap: Bitmap, text: String) {
        val textPadding = context.resources.getDimensionPixelSize(R.dimen.water_print_text_padding)
        val waterPrintSize = context.resources.getDimensionPixelSize(R.dimen.water_print_text_size).toFloat()
        val paint = Paint().apply {
            color = ContextCompat.getColor(context, R.color.colorSecondaryText)
            alpha = context.resources.getInteger(R.integer.water_print_alpha)
            textSize = waterPrintSize
            isAntiAlias = true
            isFakeBoldText = true
        }
        bitmap.applyCanvas {
            val rect = Rect()
            paint.getTextBounds(text, 0, text.length, rect)
            val font = paint.fontMetrics
            val baseLineHeight = (font.descent - font.ascent) / 2
            drawText(text, width - rect.width().toFloat() - textPadding, height - rect.height().toFloat() + baseLineHeight - textPadding, paint)
        }
    }
}