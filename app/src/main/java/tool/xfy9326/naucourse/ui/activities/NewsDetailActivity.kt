package tool.xfy9326.naucourse.ui.activities

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.drawToBitmap
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_news_detail.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.SerializableNews
import tool.xfy9326.naucourse.providers.beans.GeneralNewsDetail
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.NewsDetailViewModel
import tool.xfy9326.naucourse.ui.views.html.AdvancedLinkMovementMethod
import tool.xfy9326.naucourse.ui.views.html.AdvancedTagHandler
import tool.xfy9326.naucourse.ui.views.html.HtmlImageGetter
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showSnackBar
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils
import java.text.SimpleDateFormat
import java.util.*


class NewsDetailActivity : ViewModelActivity<NewsDetailViewModel>(), AdvancedTagHandler.OnImageLongPressListener,
    AdvancedTagHandler.OnImageClickListener {
    private var imageGetter: HtmlImageGetter? = null
    private var isNewsDetailSet = false
    private lateinit var newsData: SerializableNews

    companion object {
        const val NEWS_DATA = "NEWS_DATA"

        private val DATE_FORMAT_YMD = SimpleDateFormat(Constants.Time.FORMAT_YMD, Locale.CHINA)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        newsData = intent?.getSerializableExtra(NEWS_DATA) as SerializableNews
        super.onCreate(savedInstanceState)
    }

    override fun onCreateContentView(): Int = R.layout.activity_news_detail

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
                        showSnackBar(layout_newsDetail, R.string.generating_image)
                        getViewModel().shareNewsImage(layout_newsContent.drawToBitmap())
                    } else {
                        showSnackBar(layout_newsDetail, R.string.share_when_news_loading)
                    }
                }
            }
        }.create().apply {
            DialogUtils.addAutoCloseListener(lifecycle, this)
            show()
        }
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: NewsDetailViewModel) {
        val postSourceTextResId = I18NUtils.getNewsPostSourceResId(newsData.postSource)!!

        tb_general.setTitle(postSourceTextResId)
        setSupportActionBar(tb_general)
        enableHomeButton()

        tv_newsDetailTitle.text = newsData.title
        if (newsData.clickAmount == null) {
            tv_newsDetailInfo.text = getString(R.string.news_detail_info, getString(postSourceTextResId))
        } else {
            tv_newsDetailInfo.text = getString(R.string.news_detail_info_with_click_amount, getString(postSourceTextResId), newsData.clickAmount)
        }
        tv_newsDetailDate.text = DATE_FORMAT_YMD.format(newsData.postDate)

        asl_newsDetailRefreshLayout.setOnRefreshListener {
            requestNewsDetail(viewModel)
        }

        requestNewsDetail(viewModel)
    }

    private fun requestNewsDetail(viewModel: NewsDetailViewModel) = viewModel.requestNewsDetail(
        newsData.detailUrl.toHttpUrlOrNull()!!, newsData.postSource
    )

    override fun bindViewModel(viewModel: NewsDetailViewModel) {
        viewModel.isRefreshing.observeEvent(this, Observer {
            asl_newsDetailRefreshLayout.post {
                asl_newsDetailRefreshLayout.isRefreshing = it
            }
        })
        viewModel.newsDetail.observe(this, Observer {
            showNewsDetail(it)
            isNewsDetailSet = true
        })
        viewModel.errorNotifyType.observeEvent(this, Observer {
            showSnackBar(layout_newsDetail, I18NUtils.getContentErrorResId(it)!!)
        })
        viewModel.imageShareUri.observeEvent(this, Observer {
            startActivity(ShareUtils.getShareImageIntent(this, it))
        })
        viewModel.imageOperation.observeEvent(this, Observer {
            showSnackBar(layout_newsDetail, I18NUtils.getImageOperationTypeResId(it))
        })
    }

    private fun showNewsDetail(detail: GeneralNewsDetail) {
        val postSourceTextResId = I18NUtils.getNewsPostSourceResId(newsData.postSource)!!

        tv_newsDetailInfo.text = getString(R.string.news_detail_info_with_click_amount, getString(postSourceTextResId), detail.clickAmount)

        tv_newsDetailContent.text = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                imageGetter = HtmlImageGetter(
                    lifecycleScope, tv_newsDetailContent, this,
                    newsData.postSource
                )
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
            detail.htmlContent
        }

        tv_newsDetailContent.movementMethod = AdvancedLinkMovementMethod
    }

    override fun onHtmlTextImageClick(source: String, bitmap: Bitmap) {
        startActivity(Intent(this, ImageShowActivity::class.java).apply {
            putExtra(ImageShowActivity.EXTRA_IMAGE_URL, source)
        })
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onHtmlTextImageLongPress(source: String, bitmap: Bitmap) =
        DialogUtils.createImageOperationDialog(this, lifecycle,
            { getViewModel().shareImage(source, bitmap) },
            { getViewModel().saveNewsImage(source, bitmap) }).show()

    override fun onDestroy() {
        AdvancedLinkMovementMethod.clearHandler()
        imageGetter?.recycleDrawable()
        System.gc()
        super.onDestroy()
    }
}