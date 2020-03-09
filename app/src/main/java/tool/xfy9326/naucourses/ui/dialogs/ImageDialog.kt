package tool.xfy9326.naucourses.ui.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import tool.xfy9326.naucourses.R

class ImageDialog : DialogFragment() {
    private lateinit var imageType: ImageType
    private lateinit var imageSource: String

    companion object {
        const val IMAGE_TYPE = "IMAGE_TYPE"
        const val IMAGE_SOURCE = "IMAGE_SOURCE"

        private const val CONTENT_WIDTH_PERCENT = 0.75f
    }

    enum class ImageType {
        LOCAL_IMAGE,
        ONLINE_IMAGE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            imageType = getSerializable(IMAGE_TYPE) as ImageType
            imageSource = getString(IMAGE_SOURCE) as String
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            val displayMetrics = activity?.resources?.displayMetrics!!
            window?.apply {
                setLayout((displayMetrics.widthPixels * CONTENT_WIDTH_PERCENT).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(resources.getDrawable(R.drawable.bg_dialog, null))
            }
        }
    }
}