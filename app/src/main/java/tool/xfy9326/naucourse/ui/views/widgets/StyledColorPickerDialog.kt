package tool.xfy9326.naucourse.ui.views.widgets

import android.widget.Button
import androidx.core.content.ContextCompat
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import tool.xfy9326.naucourse.R

class StyledColorPickerDialog : ColorPickerDialog() {
    override fun onStart() {
        super.onStart()
        // 解决暗色模式下BUTTON_NEUTRAL颜色无法随主题更改的问题
        dialog?.findViewById<Button>(android.R.id.button3)?.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorDialogButtonText))
    }
}