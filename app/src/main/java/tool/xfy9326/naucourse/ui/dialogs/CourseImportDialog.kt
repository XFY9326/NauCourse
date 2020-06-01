package tool.xfy9326.naucourse.ui.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.providers.beans.jwc.Course
import tool.xfy9326.naucourse.providers.beans.jwc.CourseSet
import tool.xfy9326.naucourse.providers.beans.jwc.Term
import tool.xfy9326.naucourse.ui.models.activity.CourseManageViewModel

class CourseImportDialog : DialogFragment(), DialogInterface.OnMultiChoiceClickListener {
    private lateinit var courseSet: CourseSet
    private lateinit var courseType: CourseManageViewModel.ImportCourseType

    private lateinit var courseList: ArrayList<Course>
    private lateinit var checkedArray: BooleanArray

    companion object {
        const val COURSE_SET = "COURSE_SET"
        const val COURSE_TYPE = "COURSE_TYPE"

        private const val COURSE_LIST = "COURSE_LIST"
        private const val CHECKED_ARRAY = "CHECKED_ARRAY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        courseSet = arguments?.getSerializable(COURSE_SET) as CourseSet
        courseType = arguments?.getSerializable(COURSE_TYPE) as CourseManageViewModel.ImportCourseType

        @Suppress("UNCHECKED_CAST")
        courseList = (savedInstanceState?.getSerializable(COURSE_LIST) ?: courseSet.courses.sortedBy {
            it.id
        }.toMutableList()) as ArrayList<Course>
        checkedArray = savedInstanceState?.getBooleanArray(CHECKED_ARRAY) ?: BooleanArray(courseList.size) {
            true
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBooleanArray(CHECKED_ARRAY, checkedArray)
        outState.putSerializable(COURSE_LIST, courseList)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog = MaterialAlertDialogBuilder(requireContext()).apply {
        setCancelable(false)
        setTitle(R.string.course_import)
        setMultiChoiceItems(getCourseNameArray(), checkedArray, this@CourseImportDialog)
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(android.R.string.yes) { _, _ ->
            val activity = requireActivity()
            if (activity is CourseImportCallback) {
                activity.onCourseImport(generateNewCourseList(), courseSet.term, courseType)
            }
        }
    }.create()

    override fun onClick(dialog: DialogInterface?, which: Int, isChecked: Boolean) {
        checkedArray[which] = isChecked
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setBackgroundDrawable(requireContext().getDrawable(R.drawable.bg_dialog))
    }

    private fun getCourseNameArray() =
        Array(courseList.size) {
            courseList[it].name
        }

    private fun generateNewCourseList(): ArrayList<Course> {
        val list = ArrayList<Course>(courseList.size)
        for ((i, checked) in checkedArray.withIndex()) {
            if (checked) list.add(courseList[i])
        }
        return list
    }

    interface CourseImportCallback {
        fun onCourseImport(courses: ArrayList<Course>, term: Term, type: CourseManageViewModel.ImportCourseType)
    }
}