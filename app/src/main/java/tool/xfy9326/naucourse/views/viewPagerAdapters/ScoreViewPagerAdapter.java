package tool.xfy9326.naucourse.views.viewPagerAdapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;

import tool.xfy9326.naucourse.fragments.score.CurrentScoreFragment;
import tool.xfy9326.naucourse.fragments.score.HistoryScoreFragment;

public class ScoreViewPagerAdapter extends FragmentPagerAdapter {
    public static final int ITEM_COUNT = 2;
    private CurrentScoreFragment currentScoreFragment;
    private HistoryScoreFragment historyScoreFragment;

    public ScoreViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        setOldFragments(fragmentManager);
        createNewFragments();
    }

    private void createNewFragments() {
        if (currentScoreFragment == null) {
            currentScoreFragment = new CurrentScoreFragment();
        }
        if (historyScoreFragment == null) {
            historyScoreFragment = new HistoryScoreFragment();
        }
    }

    private void setOldFragments(FragmentManager fragmentManager) {
        List<Fragment> fragmentList = fragmentManager.getFragments();
        for (Fragment fragment : fragmentList) {
            if (fragment instanceof CurrentScoreFragment) {
                currentScoreFragment = (CurrentScoreFragment) fragment;
            } else if (fragment instanceof HistoryScoreFragment) {
                historyScoreFragment = (HistoryScoreFragment) fragment;
            }
        }
    }

    public CurrentScoreFragment getCurrentScoreFragment() {
        return currentScoreFragment;
    }

    public HistoryScoreFragment getHistoryScoreFragment() {
        return historyScoreFragment;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        Fragment fragment;
        switch (position) {
            case 1:
                fragment = historyScoreFragment;
                break;
            case 0:
            default:
                fragment = currentScoreFragment;
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return ITEM_COUNT;
    }
}
