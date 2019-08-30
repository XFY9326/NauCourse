package tool.xfy9326.naucourse.views.viewPagerAdapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

import tool.xfy9326.naucourse.fragments.base.HomeFragment;
import tool.xfy9326.naucourse.fragments.base.PersonFragment;
import tool.xfy9326.naucourse.fragments.base.TableFragment;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class MainViewPagerAdapter extends FragmentPagerAdapter {
    public static final int ITEM_COUNT = 3;
    private HomeFragment homeFragment = null;
    private TableFragment tableFragment = null;
    private PersonFragment personFragment = null;

    public MainViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        setOldFragments(fragmentManager);
        createNewFragments();
    }

    private void createNewFragments() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        if (tableFragment == null) {
            tableFragment = new TableFragment();
        }
        if (personFragment == null) {
            personFragment = new PersonFragment();
        }
    }

    private void setOldFragments(FragmentManager fragmentManager) {
        List<Fragment> fragmentList = fragmentManager.getFragments();
        for (Fragment fragment : fragmentList) {
            if (fragment instanceof HomeFragment) {
                homeFragment = (HomeFragment) fragment;
            } else if (fragment instanceof TableFragment) {
                tableFragment = (TableFragment) fragment;
            } else if (fragment instanceof PersonFragment) {
                personFragment = (PersonFragment) fragment;
            }
        }
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public TableFragment getTableFragment() {
        return tableFragment;
    }

    public PersonFragment getPersonFragment() {
        return personFragment;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 1:
                fragment = tableFragment;
                break;
            case 2:
                fragment = personFragment;
                break;
            case 0:
            default:
                fragment = homeFragment;
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return ITEM_COUNT;
    }
}
