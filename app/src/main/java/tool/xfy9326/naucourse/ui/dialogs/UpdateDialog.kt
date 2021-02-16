package tool.xfy9326.naucourse.ui.dialogs

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.naucourse.BuildConfig
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.databinding.DialogUpdateBinding
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.io.prefs.SettingsPref
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.update.beans.UpdateInfo
import tool.xfy9326.naucourse.utils.utility.IntentUtils
import tool.xfy9326.naucourse.utils.views.DialogUtils

class UpdateDialog : DialogFragment() {
    companion object {
        const val UPDATE_INFO = "UPDATE_INFO"

        private const val UPDATE_DIALOG_TAG = "UPDATE_DIALOG_TAG"
        private const val CONTENT_WIDTH_PERCENT = 0.85

        fun showDialog(fragmentManager: FragmentManager, updateInfo: UpdateInfo) {
            if (fragmentManager.findFragmentByTag(UPDATE_DIALOG_TAG) == null) {
                try {
                    UpdateDialog().apply {
                        arguments = Bundle().apply {
                            putSerializable(UPDATE_INFO, updateInfo)
                        }
                    }.showNow(fragmentManager, UPDATE_DIALOG_TAG)
                } catch (e: Exception) {
                }
            }
        }
    }

    private lateinit var updateInfo: UpdateInfo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateInfo = arguments?.getSerializable(UPDATE_INFO) as UpdateInfo
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?) =
        MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle(R.string.found_new_version)

            setView(
                DialogUpdateBinding.inflate(layoutInflater).apply {
                    tvUpdateVersion.text = getString(
                        R.string.update_version_info, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE,
                        updateInfo.versionName, updateInfo.versionCode
                    )
                    tvUpdateAttention.isVisible = updateInfo.forceUpdate
                    tvUpdateChangeLog.text = updateInfo.changeLog

                    when {
                        updateInfo.downloadSource.size > 1 -> {
                            val menu = PopupMenu(context, btnUpdateNow).apply {
                                for ((i, source) in updateInfo.downloadSource.withIndex()) {
                                    menu.add(Menu.NONE, i, Menu.NONE, source.sourceName)
                                }
                                gravity = Gravity.BOTTOM or Gravity.END
                            }
                            menu.setOnMenuItemClickListener {
                                val source = updateInfo.downloadSource[it.itemId]
                                if (source.isDirectLink && !SettingsPref.UseBrowserDownloadDirectLinkUpdate) {
                                    downloadUpdateFile(source.url)
                                } else {
                                    IntentUtils.launchUrlInBrowser(requireContext(), source.url)
                                }
                                return@setOnMenuItemClickListener true
                            }

                            btnUpdateNow.setOnClickListener {
                                menu.show()
                            }
                        }
                        updateInfo.downloadSource.isNotEmpty() -> btnUpdateNow.setOnClickListener {
                            downloadUpdateFile(updateInfo.downloadSource.first().url)
                        }
                        else -> btnUpdateNow.setOnClickListener {
                            showShortToast(R.string.no_update_source)
                        }
                    }

                    btnUpdateCancel.isVisible = !updateInfo.forceUpdate
                    btnUpdateIgnore.isVisible = !updateInfo.forceUpdate
                    if (!updateInfo.forceUpdate) {
                        btnUpdateCancel.setOnClickListener {
                            dismiss()
                        }
                        btnUpdateIgnore.setOnClickListener {
                            AppPref.IgnoreUpdateVersionCode = updateInfo.versionCode
                            dismiss()
                        }
                    }
                }.root
            )
        }.create()

    private fun downloadUpdateFile(url: String) {
        IntentUtils.requestDownloadUpdate(requireContext(), url, updateInfo.versionCode, updateInfo.versionName)
        showShortToast(R.string.start_download_update)
    }

    override fun onStart() {
        super.onStart()
        dialog?.apply {
            setCancelable(!updateInfo.forceUpdate)
            setCanceledOnTouchOutside(!updateInfo.forceUpdate)
            DialogUtils.applyBackgroundAndWidth(requireContext(), dialog, CONTENT_WIDTH_PERCENT)
        }
    }
}