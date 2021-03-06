package tool.xfy9326.naucourse.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.io.prefs.AppPref
import tool.xfy9326.naucourse.providers.beans.PostSource
import tool.xfy9326.naucourse.utils.views.I18NUtils

class NewsTypeChoiceDialog : DialogFragment() {
    private lateinit var newsTypeArray: List<PostSource>
    private lateinit var newsTypeTextArray: Array<String>
    private lateinit var choiceStatus: BooleanArray
    private var statusChanged = false

    companion object {
        private const val CHOICE_STATUS = "CHOICE_STATUS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBooleanArray(CHOICE_STATUS, choiceStatus)
        super.onSaveInstanceState(outState)
    }

    private fun initData(savedInstanceState: Bundle?) {
        // 排除 GeneralNews.PostSource.UNKNOWN
        newsTypeArray = PostSource.values().filterNot {
            it == PostSource.UNKNOWN
        }

        newsTypeTextArray = Array(newsTypeArray.size) {
            getString(I18NUtils.getNewsPostSourceResId(newsTypeArray[it])!!)
        }

        val restoredChoiceStatus = savedInstanceState?.getBooleanArray(CHOICE_STATUS)
        if (restoredChoiceStatus == null) {
            readSavedResult()
        } else {
            choiceStatus = restoredChoiceStatus
        }
    }

    private fun readSavedResult() {
        choiceStatus = BooleanArray(newsTypeArray.size) { false }
        for (postSource in AppPref.readShowNewsType()) {
            choiceStatus[newsTypeArray.indexOf(postSource)] = true
        }
    }

    private fun saveResult() {
        val typesStringSet = HashSet<String>(newsTypeArray.size)
        for ((i, choiceStatus) in choiceStatus.withIndex()) {
            if (choiceStatus) {
                typesStringSet.add(newsTypeArray[i].name)
            }
        }
        AppPref.saveShowNewsType(typesStringSet)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.bg_dialog))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext()).apply {
        setTitle(R.string.news_show_type)
        setMultiChoiceItems(newsTypeTextArray, choiceStatus) { _, which, isChecked ->
            choiceStatus[which] = isChecked
            statusChanged = true
        }
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(android.R.string.ok) { _, _ ->
            if (statusChanged) {
                saveResult()
                val fragment = requireParentFragment()
                if (fragment is OnNewsTypeChangedListener) {
                    fragment.onNewsTypeChanged()
                }
            }
        }
    }.create()

    interface OnNewsTypeChangedListener {
        fun onNewsTypeChanged()
    }
}