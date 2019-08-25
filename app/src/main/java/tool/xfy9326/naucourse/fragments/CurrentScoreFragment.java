package tool.xfy9326.naucourse.fragments;

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
import tool.xfy9326.naucourse.utils.CourseScore;
import tool.xfy9326.naucourse.views.AdvancedRecyclerView;
import tool.xfy9326.naucourse.views.recyclerAdapters.CurrentScoreAdapter;

public class CurrentScoreFragment extends Fragment {
    private View view;
    private AdvancedRecyclerView recyclerView;
    @Nullable
    private CurrentScoreAdapter scoreAdapter;

    public CurrentScoreFragment() {
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
        ViewSet();
    }

    private void ViewSet() {
        if (isAdded() && getActivity() != null && view != null) {
            recyclerView = view.findViewById(R.id.recyclerView_list);
            recyclerView.setFocusableInTouchMode(false);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.setEmptyView(view.findViewById(R.id.textView_empty_data));
            if (scoreAdapter == null) {
                scoreAdapter = new CurrentScoreAdapter(getActivity());
            }
            recyclerView.setAdapter(scoreAdapter);
        }
    }

    public void setScore(CourseScore courseScore) {
        if (isAdded() && getActivity() != null) {
            if (scoreAdapter == null) {
                scoreAdapter = new CurrentScoreAdapter(getActivity(), courseScore);
                recyclerView.setAdapter(scoreAdapter);
            } else {
                scoreAdapter.updateData(courseScore);
            }
        }
    }
}
