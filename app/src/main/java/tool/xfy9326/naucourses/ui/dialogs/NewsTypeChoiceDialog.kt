package tool.xfy9326.naucourses.ui.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.io.prefs.AppPref
import tool.xfy9326.naucourses.providers.beans.GeneralNews
import tool.xfy9326.naucourses.utils.views.I18NUtils

class NewsTypeChoiceDialog : DialogFragment() {
    private lateinit var newsTypeArray: List<GeneralNews.PostSource>
    private lateinit var newsTypeTextArray: Array<String>
    private lateinit var choiceStatus: BooleanArray
    private var statusChanged = false
    private var typeChangedListener: OnNewsTypeChangedListener? = null

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

    fun setTypeChangedListener(typeChangedListener: OnNewsTypeChangedListener?) {
        this.typeChangedListener = typeChangedListener
    }

    private fun initData(savedInstanceState: Bundle?) {
        // 排除 GeneralNews.PostSource.UNKNOWN
        newsTypeArray = GeneralNews.PostSource.values().filterNot {
            it == GeneralNews.PostSource.UNKNOWN
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
        dialog?.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.bg_dialog, null))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = AlertDialog.Builder(requireActivity()).apply {
        setTitle(R.string.news_show_type)
        setMultiChoiceItems(newsTypeTextArray, choiceStatus) { _, which, isChecked ->
            choiceStatus[which] = isChecked
            statusChanged = true
        }
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(android.R.string.yes) { _, _ ->
            if (statusChanged) {
                saveResult()
                typeChangedListener?.onChanged()
            }
        }
    }.create()

    interface OnNewsTypeChangedListener {
        fun onChanged()
    }
}