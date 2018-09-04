package tool.xfy9326.naucourse.Views;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import tool.xfy9326.naucourse.Fragments.HomeFragment;
import tool.xfy9326.naucourse.Fragments.PersonFragment;
import tool.xfy9326.naucourse.Fragments.TableFragment;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    public static final int ITEM_COUNT = 3;
    private HomeFragment homeFragment = null;
    private TableFragment tableFragment = null;
    private PersonFragment personFragment = null;

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
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

    @Nullable
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = homeFragment;
                break;
            case 1:
                fragment = tableFragment;
                break;
            case 2:
                fragment = personFragment;
                break;
            default:
                fragment = null;
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return ITEM_COUNT;
    }
}
