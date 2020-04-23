package tool.xfy9326.naucourse.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.android.synthetic.main.activity_image_show.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.tools.glide.ClientRequest
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.ImageShowViewModel
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.BitmapUtils
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils

class ImageShowActivity : ViewModelActivity<ImageShowViewModel>(), View.OnLongClickListener, View.OnClickListener {
    private lateinit var imageUrl: String
    private var loginClientType: LoginNetworkManager.ClientType? = null

    companion object {
        const val EXTRA_IMAGE_URL = "EXTRA_IMAGE_URL"
        const val EXTRA_LOGIN_CLIENT_TYPE = "EXTRA_LOGIN_CLIENT_TYPE"

        fun showImageActivity(activity: Activity, url: String, loginClientType: LoginNetworkManager.ClientType? = null) {
            activity.startActivity(Intent(activity, ImageShowActivity::class.java).apply {
                putExtra(EXTRA_IMAGE_URL, url)
                if (loginClientType != null) putExtra(EXTRA_LOGIN_CLIENT_TYPE, loginClientType)
            })
            activity.overridePendingTransition(R.anim.fade_enter, R.anim.fade_exit)
        }
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
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: ImageShowViewModel) {
        imageUrl = intent?.extras?.getString(EXTRA_IMAGE_URL)!!
        loginClientType = intent?.extras?.getSerializable(EXTRA_LOGIN_CLIENT_TYPE) as LoginNetworkManager.ClientType?

        pv_imageView.setOnLongClickListener(this)
        pv_imageView.setOnClickListener(this)
        layout_imageView.setOnClickListener(this)

        Glide.with(this).let {
            val type = loginClientType
            if (type == null) {
                it.load(imageUrl)
            } else {
                it.load(ClientRequest(type, imageUrl))
            }
        }.override(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    e?.let {
                        ExceptionUtils.printStackTrace(this@ImageShowActivity, it)
                    }
                    ActivityUtils.showSnackBar(layout_imageView, R.string.image_load_failed)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?, target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    pb_imageLoading.hide()
                    pv_imageView.visibility = View.VISIBLE
                    return false
                }
            }).fitCenter().into(pv_imageView)
    }

    override fun onClick(v: View?) {
        onBackPressed()
    }

    override fun onLongClick(v: View?): Boolean {
        val bitmap = BitmapUtils.getBitmapFromDrawable((v as PhotoView).drawable.current)!!
        DialogUtils.createImageOperationDialog(this, lifecycle,
            { getViewModel().shareImage(imageUrl, bitmap) },
            { getViewModel().saveImage(imageUrl, bitmap) }).show()
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        return true
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.fade_enter, R.anim.fade_exit)
    }
}