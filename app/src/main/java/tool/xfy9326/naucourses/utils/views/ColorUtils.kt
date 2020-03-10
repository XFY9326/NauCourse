package tool.xfy9326.naucourses.utils.views

import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import tool.xfy9326.naucourses.App
import tool.xfy9326.naucourses.R

object ColorUtils {
    @FloatRange(from = 0.0, to = 1.0)
    private const val LUMINANCE_IS_LIGHT_COLOR = 0.7f

    private val MATERIAL_COLOR = App.instance.resources.getIntArray(R.array.material_colors_600)

    fun isLightColor(color: Int) = ColorUtils.calculateLuminance(color) >= LUMINANCE_IS_LIGHT_COLOR

    fun getRandomMaterialColor() = MATERIAL_COLOR.random().toInt()
}