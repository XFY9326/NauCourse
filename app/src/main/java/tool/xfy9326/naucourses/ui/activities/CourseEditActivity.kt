package tool.xfy9326.naucourses.ui.activities

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import kotlinx.android.synthetic.main.activity_course_edit.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.beans.CourseCellStyle
import tool.xfy9326.naucourses.providers.beans.jwc.Course
import tool.xfy9326.naucourses.utils.BaseUtils
import tool.xfy9326.naucourses.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourses.utils.views.AnimUtils

class CourseEditActivity : AppCompatActivity() {
    companion object {
        const val COURSE_DATA = "COURSE_DATA"
        const val COURSE_CELL_STYLE = "COURSE_CELL_STYLE"

        private const val VIEW_EXPANDED = "VIEW_EXPANDED"

        private const val EXPAND_ANIMATION_DURATION = 250L
    }

    private lateinit var courseData: Course
    private lateinit var courseStyle: CourseCellStyle

    private var isShowMoreInfoExpanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let {
            isShowMoreInfoExpanded = it.getBoolean(VIEW_EXPANDED)
        }
        readIntentData()
        setContentView(R.layout.activity_course_edit)
        setSupportActionBar(tb_general)
        enableHomeButton()
        viewInit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(VIEW_EXPANDED, isShowMoreInfoExpanded)
        super.onSaveInstanceState(outState)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                prepareBack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        prepareBack()
    }

    private fun readIntentData() {
        courseData = intent?.getSerializableExtra(COURSE_DATA) as Course
        courseStyle = intent?.getSerializableExtra(COURSE_CELL_STYLE) as CourseCellStyle
    }

    private fun viewInit() {
        btn_showMoreCourseEditInfo.setOnClickListener {
            currentFocus?.clearFocus()
            BaseUtils.hideKeyboard(this, layout_courseEdit.windowToken)

            (it as AppCompatImageButton).apply {
                if (isShowMoreInfoExpanded) {
                    isShowMoreInfoExpanded = false
                    setImageResource(R.drawable.ic_load_more)
                    loadLess()
                } else {
                    isShowMoreInfoExpanded = true
                    setImageResource(R.drawable.ic_load_less)
                    loadMore()
                }
            }
        }

        if (isShowMoreInfoExpanded) {
            btn_showMoreCourseEditInfo.setImageResource(R.drawable.ic_load_less)
            layout_courseEditInfo.visibility = View.VISIBLE
        }

        applyCourseInfoData()
    }

    private fun loadMore() {
        layout_courseEditInfo.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        animateExpand(layout_courseEditInfo.measuredHeight)
        layout_courseEditInfo.animation = AnimUtils.getAnimationFadeVisible(this)
        layout_courseEditInfo.visibility = View.VISIBLE
    }

    private fun loadLess() {
        animateExpand(-layout_courseEditInfo.measuredHeight)
        layout_courseEditInfo.animation = AnimUtils.getAnimationFadeGone(this)
        layout_courseEditInfo.visibility = View.GONE
    }

    private fun animateExpand(changeHeight: Int) =
        ValueAnimator.ofInt(cv_courseBaseInfo.measuredHeight, cv_courseBaseInfo.measuredHeight + changeHeight).apply {
            duration = EXPAND_ANIMATION_DURATION
            interpolator = FastOutSlowInInterpolator()
            addUpdateListener {
                val value = animatedValue as Int
                cv_courseBaseInfo.layoutParams.height = value
                layout_courseDetail.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                // cv_courseBaseInfo.requestLayout() 计算一个控件布局时，相邻的也会计算
                layout_courseDetail.requestLayout()
            }
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                    btn_showMoreCourseEditInfo.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animator?) {}

                override fun onAnimationEnd(animation: Animator?) = resetAfterExpandLayout()
                override fun onAnimationCancel(animation: Animator?) = resetAfterExpandLayout()
            })
            start()
        }

    private fun resetAfterExpandLayout() {
        cv_courseBaseInfo.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        cv_courseBaseInfo.requestLayout()

        btn_showMoreCourseEditInfo.animation = AnimUtils.getAnimationFadeVisible(this@CourseEditActivity)
        btn_showMoreCourseEditInfo.visibility = View.VISIBLE
    }

    private fun applyCourseInfoData() {
        et_courseName.setText(courseData.name)
        et_teacherName.setText(courseData.teacher)
        et_courseCredit.setText(courseData.credit.toString())
        et_teachClass.setText(courseData.teachClass)
        et_courseClass.setText(courseData.courseClass)
        et_courseType.setText(courseData.type)
        et_courseProperty.setText(courseData.property)
    }

    private fun prepareBack() {
        super.onBackPressed()
    }
}