package tool.xfy9326.naucourse.Views;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import tool.xfy9326.naucourse.Fragments.HomeFragment;
import tool.xfy9326.naucourse.Fragments.PersonFragment;
import tool.xfy9326.naucourse.Fragments.TableFragment;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private static final int ITEM_COUNT = 3;
    private HomeFragment homeFragment;
    private TableFragment tableFragment;
    private PersonFragment personFragment;

    public ViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        createFragments();
    }

    private void createFragments() {
        homeFragment = new HomeFragment();
        tableFragment = new TableFragment();
        personFragment = new PersonFragment();
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
