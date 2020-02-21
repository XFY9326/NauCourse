package tool.xfy9326.naucourses.ui.fragments.base

import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import tool.xfy9326.naucourses.R
import tool.xfy9326.naucourses.ui.models.base.BaseViewModel

abstract class DrawerToolbarFragment<T : BaseViewModel>
    (private val activity: AppCompatActivity, @IdRes private val idRes: Int) : ViewModelFragment<T>() {

    override fun onStart() {
        super.onStart()
        val toolbar = onBindToolbar()
        bindToolbarWithDrawer(toolbar)
    }

    private fun bindToolbarWithDrawer(toolbar: Toolbar) {
        val drawer = activity.findViewById<DrawerLayout>(idRes)
        ActionBarDrawerToggle(activity, drawer, toolbar, R.string.drawer_layout_open, R.string.drawer_layout_close).apply {
            isDrawerIndicatorEnabled = true
            syncState()
            drawer.addDrawerListener(this)
        }
    }

    protected abstract fun onBindToolbar(): Toolbar
}