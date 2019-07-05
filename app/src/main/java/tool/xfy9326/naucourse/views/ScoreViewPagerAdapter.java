package tool.xfy9326.naucourse.views;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import tool.xfy9326.naucourse.fragments.CurrentScoreFragment;
import tool.xfy9326.naucourse.fragments.HistoryScoreFragment;

public class ScoreViewPagerAdapter extends FragmentPagerAdapter {
    public static final int ITEM_COUNT = 2;
    private final CurrentScoreFragment currentScoreFragment;
    private final HistoryScoreFragment historyScoreFragment;

    public ScoreViewPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        currentScoreFragment = new CurrentScoreFragment();
        historyScoreFragment = new HistoryScoreFragment();
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
            case 0:
                fragment = currentScoreFragment;
                break;
            case 1:
                fragment = historyScoreFragment;
                break;
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
