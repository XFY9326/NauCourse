package tool.xfy9326.naucourses.ui.views.widgets

import android.widget.Button
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import tool.xfy9326.naucourses.R

class StyledColorPickerDialog : ColorPickerDialog() {
    override fun onStart() {
        super.onStart()
        // 解决暗色模式下BUTTON_NEUTRAL颜色无法随主题更改的问题
        dialog?.findViewById<Button>(android.R.id.button3)?.setTextColor(requireContext().getColor(R.color.colorDialogButtonText))
    }
}