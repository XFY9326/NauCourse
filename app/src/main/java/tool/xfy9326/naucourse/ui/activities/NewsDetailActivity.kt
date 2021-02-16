package tool.xfy9326.naucourse.ui.activities

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.SerializableNews
import tool.xfy9326.naucourse.constants.TimeConst
import tool.xfy9326.naucourse.databinding.ActivityNewsDetailBinding
import tool.xfy9326.naucourse.kt.createWithLifecycle
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.network.LoginNetworkManager
import tool.xfy9326.naucourse.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.NewsDetailViewModel
import tool.xfy9326.naucourse.ui.views.html.AdvancedLinkMovementMethod
import tool.xfy9326.naucourse.ui.views.html.AdvancedTagHandler
import tool.xfy9326.naucourse.ui.views.html.HtmlImageGetter
import tool.xfy9326.naucourse.utils.debug.ExceptionUtils
import tool.xfy9326.naucourse.utils.utility.BitmapUtils
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.utility.PermissionUtils
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import java.text.SimpleDateFormat
import java.util.*


class NewsDetailActivity : ViewModelActivity<NewsDetailViewModel>(), AdvancedTagHandler.OnImageLongPressListener,
    AdvancedTagHandler.OnImageClickListener {
    private var isNewsDetailSet = false
    private lateinit var newsData: SerializableNews

    companion object {
        const val NEWS_DATA = "NEWS_DATA"

        private val DATE_FORMAT_YMD = SimpleDateFormat(TimeConst.FORMAT_YMD, Locale.CHINA)
    }

    private val binding by lazy {
        ActivityNewsDetailBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        newsData = intent?.getSerializableExtra(NEWS_DATA) as SerializableNews
        super.onCreate(savedInstanceState)
    }

    override fun onCreateContentView() = binding.root

    override fun onCreateViewModel(): NewsDetailViewModel = ViewModelProvider(this)[NewsDetailViewModel::class.java]

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_news_detail, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_newsDetailOpenInBrowser -> IntentUtils.launchUrlInBrowser(this, newsData.detailUrl.toString())
            R.id.menu_newsDetailShare -> showNewsShareDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showNewsShareDialog() {
        MaterialAlertDialogBuilder(this).apply {
            setItems(R.array.news_share_type) { _, which ->
                if (which == 0) {
                    startActivity(ShareUtils.getShareNewsIntent(this@NewsDetailActivity, newsData))
                } else if (which == 1) {
                    if (isNewsDetailSet) {
                        binding.layoutNewsDetail.showSnackBar(R.string.generating_image)
                        lifecycleScope.launch {
                            try {
                                binding.layoutNewsDetail.drawToBitmap().let {
                                    BitmapUtils.drawDefaultWaterPrint(this@NewsDetailActivity, it)
                                    getViewModel().shareNewsImage(it)
                                }
                            } catch (e: Exception) {
                                binding.layoutNewsDetail.showSnackBar(R.string.share_when_news_loading)
                            }
                        }
                    } else {
                        binding.layoutNewsDetail.showSnackBar(R.string.share_when_news_loading)
                    }
                }
            }
            background = ContextCompat.getDrawable(this@NewsDetailActivity, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle).show()
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: NewsDetailViewModel) {
        val postSourceTextResId = I18NUtils.getNewsPostSourceResId(newsData.postSource)!!

        binding.toolbar.tbGeneral.setTitle(postSourceTextResId)
        setSupportActionBar(binding.toolbar.tbGeneral)
        enableHomeButton()

        binding.tvNewsDetailTitle.text = newsData.title
        if (newsData.clickAmount == null) {
            binding.tvNewsDetailInfo.text = getString(R.string.news_detail_info, getString(postSourceTextResId))
        } else {
            binding.tvNewsDetailInfo.text = getString(
                R.string.news_detail_info_with_click_amount, getString(postSourceTextResId), newsData
                    .clickAmount
            )
        }
        binding.tvNewsDetailDate.text = DATE_FORMAT_YMD.format(newsData.postDate)

        binding.aslNewsDetail.setOnRefreshListener {
            requestNewsDetail(viewModel)
        }

        requestNewsDetail(viewModel)
    }

    private fun requestNewsDetail(viewModel: NewsDetailViewModel) = viewModel.requestNewsDetail(
        newsData.detailUrl.toHttpUrlOrNull()!!, newsData.postSource
    )

    override fun bindViewModel(viewModel: NewsDetailViewModel) {
        viewModel.isRefreshing.observeEvent(this) {
            binding.aslNewsDetail.postStopRefreshing()
        }
        viewModel.newsDetail.observe(this, {
            showNewsDetail(it)
            isNewsDetailSet = true
        })
        viewModel.errorNotifyType.observeEvent(this) {
            binding.layoutNewsDetail.showSnackBar(I18NUtils.getContentErrorResId(it)!!)
        }
        viewModel.imageShareUri.observeEvent(this) {
            startActivity(ShareUtils.getShareImageIntent(this, it))
        }
        viewModel.imageOperation.observeEvent(this) {
            binding.layoutNewsDetail.showSnackBar(I18NUtils.getImageOperationTypeResId(it))
        }
    }

    private fun showNewsDetail(detail: GeneralNewsDetail) {
        val postSourceTextResId = I18NUtils.getNewsPostSourceResId(newsData.postSource)!!

        binding.tvNewsDetailInfo.text = getString(R.string.news_detail_info_with_click_amount, getString(postSourceTextResId), detail.clickAmount)

        binding.tvNewsDetailContent.text = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val imageGetter = HtmlImageGetter(this, binding.tvNewsDetailContent, newsData.postSource)
                val tagHandler = AdvancedTagHandler().apply {
                    setOnImageClickListener(this@NewsDetailActivity)
                    setOnImageLongPressListener(this@NewsDetailActivity)
                }
                Html.fromHtml(detail.htmlContent, Html.FROM_HTML_MODE_LEGACY, imageGetter, tagHandler)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(detail.htmlContent)
            }
        } catch (e: Exception) {
            ExceptionUtils.printStackTrace<NewsDetailActivity>(e)
            detail.htmlContent
        }

        binding.tvNewsDetailContent.movementMethod = AdvancedLinkMovementMethod
    }

    override fun onHtmlTextImageClick(source: String, clientType: LoginNetworkManager.ClientType?) {
        ImageShowActivity.showImageActivity(this, source, clientType)
    }

    override fun onHtmlTextImageLongPress(source: String, bitmap: Bitmap) =
        DialogUtils.createImageOperationDialog(this, lifecycle,
            { getViewModel().shareImage(source, bitmap) },
            {
                if (PermissionUtils.prepareStoragePermission(this)) {
                    getViewModel().saveImage(source, bitmap)
                }
            }).show()

    override fun onDestroy() {
        AdvancedLinkMovementMethod.cancelLongClick()
        super.onDestroy()
    }
}