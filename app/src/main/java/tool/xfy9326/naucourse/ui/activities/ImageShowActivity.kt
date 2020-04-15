package tool.xfy9326.naucourse.ui.activities

import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_image_show.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.ImageShowViewModel
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils

class ImageShowActivity : ViewModelActivity<ImageShowViewModel>(), View.OnLongClickListener, View.OnClickListener {
    private lateinit var imageUrl: String

    companion object {
        const val EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL"
    }

    override fun onCreateContentView(): Int = R.layout.activity_image_show

    override fun onCreateViewModel(): ImageShowViewModel = ViewModelProvider(this)[ImageShowViewModel::class.java]

    override fun bindViewModel(viewModel: ImageShowViewModel) {
        viewModel.imageShareUri.observeEvent(this, Observer {
            startActivity(ShareUtils.getShareImageIntent(this, it))
        })
        viewModel.imageOperation.observeEvent(this, Observer {
            ActivityUtils.showSnackBar(layout_imageView, I18NUtils.getImageOperationTypeResId(it))
        })
        viewModel.imageDownloadFailed.observeNotification(this, {
            ActivityUtils.showSnackBar(layout_imageView, R.string.image_load_failed)
        })
        viewModel.image.observeEventWithCheck(this, {
            pv_imageView.setImageBitmap(it)
            pb_imageLoading.hide()
            pv_imageView.visibility = View.VISIBLE
            true
        })
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: ImageShowViewModel) {
        imageUrl = intent?.extras?.getString(EXTRA_IMAGE_URL)!!

        pv_imageView.setOnLongClickListener(this)
        pv_imageView.setOnClickListener(this)
        layout_imageView.setOnClickListener(this)

        viewModel.loadBitmap(imageUrl)
    }

    override fun onClick(v: View?) {
        onBackPressed()
    }

    override fun onLongClick(v: View?): Boolean {
        val bitmap = ((v as PhotoView).drawable.current as BitmapDrawable).bitmap
        DialogUtils.createImageOperationDialog(this, lifecycle,
            { getViewModel().shareImage(imageUrl, bitmap) },
            { getViewModel().saveImage(imageUrl, bitmap) }).show()
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        return true
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}