package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import tool.xfy9326.naucourse.Activities.MoaActivity;
import tool.xfy9326.naucourse.Methods.InfoMethods.MoaMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Moa;

public class MoaAdapter extends RecyclerView.Adapter<MoaAdapter.MoaViewHolder> {
    private final MoaActivity activity;
    private Moa moa;

    public MoaAdapter(MoaActivity activity, Moa moa) {
        this.activity = activity;
        this.moa = moa;
    }

    public void updateMoa(Moa moa) {
        this.moa = moa;
        notifyDataSetChanged();
    }

    public void clearAdapter() {
        this.moa.setCount(0);
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull MoaViewHolder holder, int position) {
        holder.textViewMoaTitle.setText(moa.getTitle()[holder.getAdapterPosition()]);
        holder.textViewMoaType.setText(activity.getString(R.string.moa_type, (moa.getType()[holder.getAdapterPosition()]).equalsIgnoreCase(MoaMethod.Academic_Report) ? activity.getString(R.string.academic_report) : activity.getString(R.string.large_academic_conference)));
        String reporter = moa.getReporter()[holder.getAdapterPosition()];
        if (reporter != null && !reporter.equalsIgnoreCase("null")) {
            holder.textViewMoaReporter.setVisibility(View.VISIBLE);
            holder.textViewMoaReporter.setText(activity.getString(R.string.moa_reporter, reporter));
        } else {
            holder.textViewMoaReporter.setVisibility(View.GONE);
        }
        holder.textViewMoaTime.setText(activity.getString(R.string.moa_time, moa.getTime()[holder.getAdapterPosition()]));
        holder.textViewMoaLocation.setText(activity.getString(R.string.moa_location, moa.getLocation()[holder.getAdapterPosition()]));
        holder.textViewMoaApplyUnit.setText(activity.getString(R.string.moa_apply_unit, moa.getApplyUnit()[holder.getAdapterPosition()]));
    }

    @NonNull
    @Override
    public MoaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.item_moa, parent, false);
        return new MoaViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return moa.getCount();
    }

    static class MoaViewHolder extends RecyclerView.ViewHolder {
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
}
