package tool.xfy9326.naucourse.utils.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import tool.xfy9326.naucourse.Constants
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.SerializableNews

object ShareUtils {
    // 获取分享图片的Intent
    fun getShareImageIntent(context: Context, uri: Uri): Intent =
        Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = Constants.MIME.IMAGE
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, context.getString(R.string.share_image))
            putExtra(Intent.EXTRA_MIME_TYPES, Constants.MIME.IMAGE)
        }, null)

    // 获取分享通知的Intent
    fun getShareNewsIntent(context: Context, news: SerializableNews): Intent =
        Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = Constants.MIME.TEXT
            putExtra(Intent.EXTRA_TITLE, context.getString(R.string.share_news))
            putExtra(Intent.EXTRA_TEXT, getShareNewsText(context, news))
            putExtra(Intent.EXTRA_MIME_TYPES, Constants.MIME.TEXT)
        }, null)

    // 获取SerializableNews分享的文字
    private fun getShareNewsText(context: Context, news: SerializableNews) =
        if (news.type == null) {
            context.getString(R.string.news_share_text, news.title, news.detailUrl.toString())
        } else {
            context.getString(R.string.news_share_text_with_type, news.type, news.title, news.detailUrl.toString())
        }
}