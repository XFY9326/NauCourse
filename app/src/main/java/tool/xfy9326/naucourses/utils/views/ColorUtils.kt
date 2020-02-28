package tool.xfy9326.naucourses.utils.views

import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils

object ColorUtils {
    @FloatRange(from = 0.0, to = 1.0)
    private const val LUMINANCE_IS_LIGHT_COLOR = 0.7f

    // Material Color 600
    private val MATERIAL_COLOR = arrayOf(
        0xffdd191d, // Red
        0xffd81b60, // Pink
        0xff8e24aa, // Purple
        0xff5e35b1, // Deep Purple
        0xff3949ab, // Indigo
        0xff4e6cef, // Blue
        0xff039be5, // Light Blue
        0xff00acc1, // Cyan
        0xff00897b, // Teal
        0xff0a8f08, // Green
        0xff7cb342, // Light Green
        0xffc0ca33, // Lime
        // 0xfffdd835, // Yellow 颜色过浅
        0xffffb300, // Amber
        0xfffb8c00, // Orange
        0xfff4511e, // Deep Orange
        0xff6d4c41, // Brown
        0xff757575, // Grey
        0xff546e7a // Blue Grey
    )

    fun isLightColor(color: Int) = ColorUtils.calculateLuminance(color) >= LUMINANCE_IS_LIGHT_COLOR

    fun getRandomMaterialColor() = MATERIAL_COLOR.random().toInt()
}