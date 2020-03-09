package tool.xfy9326.naucourses.utils.utility

import android.content.Context
import android.content.Intent
import android.net.Uri
import tool.xfy9326.naucourses.Constants
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.SerializableNews

object ShareUtils {
    fun getShareImageIntent(context: Context, uri: Uri): Intent =
        Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = Constants.MIME.IMAGE
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TITLE, context.getString(R.string.share_image))
            putExtra(Intent.EXTRA_MIME_TYPES, Constants.MIME.IMAGE)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }, null)

    fun getShareNewsIntent(context: Context, news: SerializableNews): Intent =
        Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = Constants.MIME.TEXT
            putExtra(Intent.EXTRA_TITLE, context.getString(R.string.share_news))
            putExtra(Intent.EXTRA_TEXT, news.getShareText(context))
            putExtra(Intent.EXTRA_MIME_TYPES, Constants.MIME.IMAGE)
        }, null)
}