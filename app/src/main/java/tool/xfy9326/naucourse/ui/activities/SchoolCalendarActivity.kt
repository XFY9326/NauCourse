package tool.xfy9326.naucourse.ui.activities

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tool.xfy9326.naucourse.R
import tool.xfy9326.naucourse.beans.CalendarItem
import tool.xfy9326.naucourse.databinding.ActivitySchoolCalendarBinding
import tool.xfy9326.naucourse.kt.createWithLifecycle
import tool.xfy9326.naucourse.kt.enableHomeButton
import tool.xfy9326.naucourse.kt.showShortToast
import tool.xfy9326.naucourse.kt.showSnackBar
import tool.xfy9326.naucourse.ui.activities.base.ViewModelActivity
import tool.xfy9326.naucourse.ui.models.activity.SchoolCalendarViewModel
import tool.xfy9326.naucourse.utils.utility.BitmapUtils
import tool.xfy9326.naucourse.utils.utility.PermissionUtils
import tool.xfy9326.naucourse.utils.utility.ShareUtils
import tool.xfy9326.naucourse.utils.views.I18NUtils

class SchoolCalendarActivity : ViewModelActivity<SchoolCalendarViewModel>() {
    private var isCalendarSet = false

    private val binding by lazy {
        ActivitySchoolCalendarBinding.inflate(layoutInflater)
    }

    override fun onCreateContentView() = binding.root

    override fun onCreateViewModel(): SchoolCalendarViewModel = ViewModelProvider(this)[SchoolCalendarViewModel::class.java]

    override fun bindViewModel(viewModel: SchoolCalendarViewModel) {
        viewModel.imageShareUri.observeEvent(this) {
            startActivity(ShareUtils.getShareImageIntent(this, it))
        }
        viewModel.imageOperation.observeEvent(this) {
            binding.layoutSchoolCalendar.showSnackBar(I18NUtils.getImageOperationTypeResId(it))
        }
        viewModel.calendarImageUrl.observe(this) {
            setImageView(it)
        }
        viewModel.calendarList.observeEvent(this) {
            showCalendarList(it)
        }
        viewModel.calendarLoadStatus.observeEvent(this) {
            if (it == SchoolCalendarViewModel.CalendarLoadStatus.LOADING_IMAGE_LIST) {
                showShortToast(I18NUtils.getCalendarLoadStatusResId(it))
            } else {
                binding.layoutSchoolCalendar.showSnackBar(I18NUtils.getCalendarLoadStatusResId(it))
            }
        }
    }

    override fun initView(savedInstanceState: Bundle?, viewModel: SchoolCalendarViewModel) {
        setSupportActionBar(binding.toolbar.tbGeneral)
        enableHomeButton()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_school_calendar, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_calendarSave -> getBitmapFromImageView()?.let {
                if (PermissionUtils.prepareStoragePermission(this)) {
                    getViewModel().saveImage(it)
                }
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
                getViewModel().loadCalendarImage(list[which].url, true)
            }
            setNeutralButton(R.string.restore_to_default_calendar) { _, _ ->
                resetImageView()
                getViewModel().restoreToDefaultImage()
            }
            setNegativeButton(android.R.string.cancel, null)
            background = ContextCompat.getDrawable(context, R.drawable.bg_dialog)
        }.createWithLifecycle(lifecycle).show()
    }

    @Synchronized
    private fun setImageView(url: String) {
        Glide.with(this).load(url).override(resources.displayMetrics.widthPixels, resources.displayMetrics.heightPixels)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                    binding.layoutSchoolCalendar.showSnackBar(
                        I18NUtils.getCalendarLoadStatusResId(SchoolCalendarViewModel.CalendarLoadStatus.IMAGE_LOAD_FAILED)
                    )
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?, model: Any?, target: Target<Drawable>?,
                    dataSource: DataSource?, isFirstResource: Boolean
                ): Boolean {
                    isCalendarSet = true

                    binding.pbCalendarLoading.hide()
                    binding.pvCalendarImage.visibility = View.VISIBLE

                    return false
                }
            }).fitCenter().into(binding.pvCalendarImage)
    }

    @Synchronized
    private fun getBitmapFromImageView(): Bitmap? {
        return if (isCalendarSet) {
            BitmapUtils.getBitmapFromDrawable(binding.pvCalendarImage.drawable).also {
                if (it == null) binding.layoutSchoolCalendar.showSnackBar(R.string.image_operation_when_calendar_loading)
            }
        } else {
            binding.layoutSchoolCalendar.showSnackBar(R.string.image_operation_when_calendar_loading)
            null
        }
    }

    @Synchronized
    private fun resetImageView() {
        isCalendarSet = false

        binding.pvCalendarImage.visibility = View.GONE
        binding.pbCalendarLoading.show()
    }
}