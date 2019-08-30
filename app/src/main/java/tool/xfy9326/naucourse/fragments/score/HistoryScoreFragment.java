package tool.xfy9326.naucourse.fragments.score;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.utils.HistoryScore;
import tool.xfy9326.naucourse.views.AdvancedRecyclerView;
import tool.xfy9326.naucourse.views.recyclerAdapters.HistoryScoreAdapter;

public class HistoryScoreFragment extends Fragment {
    private View view;
    private AdvancedRecyclerView recyclerView;
    @Nullable
    private HistoryScoreAdapter scoreAdapter;

    public HistoryScoreFragment() {
        this.view = null;
        this.recyclerView = null;
        this.scoreAdapter = null;
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, container, false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        viewSet();
    }

    private void viewSet() {
        if (isAdded() && getActivity() != null && view != null) {
            recyclerView = view.findViewById(R.id.recyclerView_list);
            recyclerView.setFocusableInTouchMode(false);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setEmptyView(view.findViewById(R.id.textView_empty_data));
            if (scoreAdapter == null) {
                scoreAdapter = new HistoryScoreAdapter(getActivity());
            }
            recyclerView.setAdapter(scoreAdapter);
        }
    }

    public void setScore(HistoryScore historyScore) {
        if (isAdded() && getActivity() != null) {
            if (scoreAdapter == null) {
                scoreAdapter = new HistoryScoreAdapter(getActivity(), historyScore);
                recyclerView.setAdapter(scoreAdapter);
            } else {
                scoreAdapter.updateData(historyScore);
            }
        }
    }
}
