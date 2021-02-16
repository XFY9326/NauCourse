package tool.xfy9326.naucourse.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.PhotoView
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.databinding.ActivityImageShowBinding
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.tools.glide.ClientRequest
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.ImageShowViewModel
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.BitmapUtils
import tool.xfy9326.naucourse.utils.utility.PermissionUtils
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils

class ImageShowActivity : ViewModelActivity<ImageShowViewModel>(), View.OnLongClickListener, View.OnClickListener {
    private lateinit var imageUrl: String
    private var loginClientType: LoginNetworkManager.ClientType? = null

    private val binding by lazy {
        ActivityImageShowBinding.inflate(layoutInflater)
    }

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

    override fun onCreateContentView() = binding.root

    override fun onCreateViewModel(): ImageShowViewModel = ViewModelProvider(this)[ImageShowViewModel::class.java]

    override fun bindViewModel(viewModel: ImageShowViewModel) {
        viewModel.imageShareUri.observeEvent(this) {
            startActivity(ShareUtils.getShareImageIntent(this, it))
        }
        viewModel.imageOperation.observeEvent(this) {
            binding.layoutImageView.showSnackBar(I18NUtils.getImageOperationTypeResId(it))
        }
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: ImageShowViewModel) {
        imageUrl = intent?.extras?.getString(EXTRA_IMAGE_URL)!!
        loginClientType = intent?.extras?.getSerializable(EXTRA_LOGIN_CLIENT_TYPE) as LoginNetworkManager.ClientType?

        binding.pvImageView.setOnLongClickListener(this)
        binding.pvImageView.setOnClickListener(this)
        binding.layoutImageView.setOnClickListener(this)

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
                    binding.layoutImageView.showSnackBar(R.string.image_load_failed)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?, target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    binding.pbImageLoading.hide()
                    binding.pvImageView.visibility = View.VISIBLE
                    return false
                }
            }).fitCenter().into(binding.pvImageView)
    }

    override fun onClick(v: View?) {
        onBackPressed()
    }

    override fun onLongClick(v: View?): Boolean {
        val bitmap = BitmapUtils.getBitmapFromDrawable((v as PhotoView).drawable)
        if (bitmap != null) {
            DialogUtils.createImageOperationDialog(this, lifecycle,
                { getViewModel().shareImage(imageUrl, bitmap) },
                {
                    if (PermissionUtils.prepareStoragePermission(this)) {
                        getViewModel().saveImage(imageUrl, bitmap)
                    }
                }).show()
        } else {
            binding.layoutImageView.showSnackBar(R.string.image_load_failed)
        }
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        return true
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.fade_enter, R.anim.fade_exit)
    }
}