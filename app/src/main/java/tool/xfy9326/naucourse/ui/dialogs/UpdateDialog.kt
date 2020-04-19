package tool.xfy9326.naucourse.ui.dialogs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.Menu
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.dialog_update.view.*
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.update.beans.DownloadSource
import tool.xfy9326.naucourse.update.beans.UpdateInfo
import tool.xfy9326.naucourse.utils.BaseUtils.getPackageUri
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast

class UpdateDialog : DialogFragment() {
    companion object {
        const val UPDATE_INFO = "UPDATE_INFO"

        private const val UPDATE_DIALOG_TAG = "UPDATE_DIALOG_TAG"
        private const val REQUEST_INSTALL_PACKAGE_PERMISSION = 1
        private const val CONTENT_WIDTH_PERCENT = 0.85

        fun showDialog(fragmentManager: FragmentManager, updateInfo: UpdateInfo) {
            if (fragmentManager.findFragmentByTag(UPDATE_DIALOG_TAG) == null) {
                UpdateDialog().apply {
                    arguments = Bundle().apply {
                        putSerializable(UPDATE_INFO, updateInfo)
                    }
                }.show(fragmentManager, UPDATE_DIALOG_TAG)
            }
        }
    }

    private lateinit var updateInfo: UpdateInfo
    private var selectedSource: DownloadSource? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateInfo = arguments?.getSerializable(UPDATE_INFO) as UpdateInfo
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?) =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.found_new_version)

            val view = layoutInflater.inflate(R.layout.dialog_update, null).apply {
                tv_updateVersion.text = getString(
                    R.string.update_version_info, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE,
                    updateInfo.versionName, updateInfo.versionCode
                )
                tv_updateAttention.isVisible = updateInfo.forceUpdate
                tv_updateChangeLog.text = updateInfo.changeLog

                if (updateInfo.downloadSource.size > 1) {
                    val menu = PopupMenu(context, btn_updateNow).apply {
                        for ((i, source) in updateInfo.downloadSource.withIndex()) {
                            menu.add(Menu.NONE, i, Menu.NONE, source.sourceName)
                        }
                        gravity = Gravity.BOTTOM or Gravity.END
                    }
                    menu.setOnMenuItemClickListener {
                        val source = updateInfo.downloadSource[it.itemId]
                        if (source.isDirectLink) {
                            selectedSource = source
                            if (checkPackageInstallPermission(true)) {
                                downloadUpdateFile(source.url)
                            }
                        } else {
                            IntentUtils.launchUrlInBrowser(requireContext(), source.url)
                        }
                        return@setOnMenuItemClickListener true
                    }

                    btn_updateNow.setOnClickListener {
                        menu.show()
                    }
                } else if (updateInfo.downloadSource.isNotEmpty()) {
                    btn_updateNow.setOnClickListener {
                        updateInfo.downloadSource.first().let {
                            selectedSource = it
                            if (checkPackageInstallPermission(true)) {
                                downloadUpdateFile(it.url)
                            }
                        }
                    }
                } else {
                    btn_updateNow.setOnClickListener {
                        showToast(R.string.no_update_source)
                    }
                }

                btn_updateCancel.isVisible = !updateInfo.forceUpdate
                btn_updateIgnore.isVisible = !updateInfo.forceUpdate
                if (!updateInfo.forceUpdate) {
                    btn_updateCancel.setOnClickListener {
                        dismiss()
                    }
                    btn_updateIgnore.setOnClickListener {
                        AppPref.IgnoreUpdateVersionCode = updateInfo.versionCode
                        dismiss()
                    }
                }
            }
            setView(view)
        }.create()

    private fun checkPackageInstallPermission(requestPermission: Boolean): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!requireContext().packageManager.canRequestPackageInstalls()) {
                if (requestPermission) {
                    startActivityForResult(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, requireContext().getPackageUri()),
                        REQUEST_INSTALL_PACKAGE_PERMISSION
                    )
                }
                return false
            }
            return true
        } else {
            return true
        }
    }

    private fun downloadUpdateFile(url: String) {
        IntentUtils.requestDownloadUpdate(requireContext(), url, updateInfo.versionCode, updateInfo.versionName)
        showToast(R.string.start_download_update)
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            setCancelable(!updateInfo.forceUpdate)
            setCanceledOnTouchOutside(!updateInfo.forceUpdate)

            val displayMetrics = activity?.resources?.displayMetrics!!
            window?.apply {
                setLayout((displayMetrics.widthPixels * CONTENT_WIDTH_PERCENT).toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
                setBackgroundDrawable(activity?.getDrawable(R.drawable.bg_dialog))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_INSTALL_PACKAGE_PERMISSION) {
            if (checkPackageInstallPermission(false)) {
                selectedSource?.let {
                    downloadUpdateFile(it.url)
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}