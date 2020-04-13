package tool.xfy9326.naucourse.ui.activities

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_school_calendar.*
import kotlinx.android.synthetic.main.view_general_toolbar.*
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CalendarItem
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.SchoolCalendarViewModel
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils
import tool.xfy9326.naucourse.utils.views.ActivityUtils.enableHomeButton
import tool.xfy9326.naucourse.utils.views.ActivityUtils.showToast
import tool.xfy9326.naucourse.utils.views.DialogUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils

class SchoolCalendarActivity : ViewModelActivity<SchoolCalendarViewModel>() {
    private var isCalendarSet = false

    override fun onCreateContentView(): Int = R.layout.activity_school_calendar

    override fun onCreateViewModel(): SchoolCalendarViewModel = ViewModelProvider(this)[SchoolCalendarViewModel::class.java]

    override fun bindViewModel(viewModel: SchoolCalendarViewModel) {
        viewModel.imageShareUri.observeEvent(this, Observer {
            startActivity(ShareUtils.getShareImageIntent(this, it))
        })
        viewModel.imageOperation.observeEvent(this, Observer {
            ActivityUtils.showSnackBar(layout_schoolCalendar, I18NUtils.getImageOperationTypeResId(it))
        })
        viewModel.calendarImage.observeEvent(this, Observer {
            if (it != null) setImageView(it)
        })
        viewModel.calendarList.observeEvent(this, Observer {
            showCalendarList(it)
        })
        viewModel.calendarLoadStatus.observeEvent(this, Observer {
            if (it == SchoolCalendarViewModel.CalendarLoadStatus.LOADING_IMAGE_LIST) {
                showToast(this, I18NUtils.getCalendarLoadStatusResId(it))
            } else {
                ActivityUtils.showSnackBar(layout_schoolCalendar, I18NUtils.getCalendarLoadStatusResId(it))
            }
        })
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: SchoolCalendarViewModel) {
        setSupportActionBar(tb_general)
        enableHomeButton()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_school_calendar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_calendarSave -> getBitmapFromImageView()?.let {
                getViewModel().saveImage(it)
            }
            R.id.menu_calendarShare -> getBitmapFromImageView()?.let {
                getViewModel().shareImage(it)
            }
            R.id.menu_calendarList -> getViewModel().getCalendarList()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCalendarList(list: Array<CalendarItem>) {
        val itemList = Array(list.size) {
            list[it].name
        }
        MaterialAlertDialogBuilder(this).apply {
            setTitle(R.string.calendar_list)

            setItems(itemList) { _, which ->
                resetImageView()
                getViewModel().loadCalendarImage(list[which].url)
            }
            setNeutralButton(R.string.restore_to_default_calendar) { _, _ ->
                resetImageView()
                getViewModel().restoreToDefaultImage()
            }
            setNegativeButton(android.R.string.cancel, null)
        }.create().apply {
            DialogUtils.addAutoCloseListener(lifecycle, this)
            show()
        }
    }

    @Synchronized
    private fun setImageView(bitmap: Bitmap) {
        pv_calendarImage.setImageBitmap(bitmap)
        isCalendarSet = true

        pb_calendarLoading.hide()
        pv_calendarImage.visibility = View.VISIBLE
    }

    @Synchronized
    private fun getBitmapFromImageView(): Bitmap? {
        return if (isCalendarSet) {
            (pv_calendarImage.drawable.current as BitmapDrawable?)?.bitmap
        } else {
            ActivityUtils.showSnackBar(layout_schoolCalendar, R.string.image_operation_when_calendar_loading)
            null
        }
    }

    @Synchronized
    private fun resetImageView() {
        isCalendarSet = false

        pv_calendarImage.visibility = View.GONE
        pb_calendarLoading.show()
    }

    override fun onDestroy() {
        getViewModel().calendarImage.setEventValue(null)
        super.onDestroy()
    }
}