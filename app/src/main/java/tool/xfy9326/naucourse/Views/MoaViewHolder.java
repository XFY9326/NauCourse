package tool.xfy9326.naucourse.Views;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import tool.xfy9326.naucourse.R;

class MoaViewHolder extends RecyclerView.ViewHolder {
    final TextView textViewMoaTitle;
    final TextView textViewMoaType;
    final TextView textViewMoaReporter;
    final TextView textViewMoaTime;
    final TextView textViewMoaLocation;
    final TextView textViewMoaApplyUnit;

    MoaViewHolder(View view) {
        super(view);
        textViewMoaTitle = view.findViewById(R.id.textView_moa_title);
        textViewMoaType = view.findViewById(R.id.textView_moa_type);
        textViewMoaReporter = view.findViewById(R.id.textView_moa_reporter);
        textViewMoaTime = view.findViewById(R.id.textView_moa_time);
        textViewMoaLocation = view.findViewById(R.id.textView_moa_location);
        textViewMoaApplyUnit = view.findViewById(R.id.textView_moa_apply_unit);

    }
}
