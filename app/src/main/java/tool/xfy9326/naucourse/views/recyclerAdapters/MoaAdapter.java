package tool.xfy9326.naucourse.views.recyclerAdapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.MoaActivity;
import tool.xfy9326.naucourse.methods.netInfoMethods.MoaMethod;
import tool.xfy9326.naucourse.utils.Moa;

public class MoaAdapter extends RecyclerView.Adapter<MoaAdapter.MoaViewHolder> {
    private final MoaActivity activity;
    private Moa moa;

    public MoaAdapter(MoaActivity activity) {
        this(activity, new Moa());
    }

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
        String time = moa.getTime()[holder.getAdapterPosition()];
        String location = moa.getLocation()[holder.getAdapterPosition()];
        String applyUnit = moa.getApplyUnit()[holder.getAdapterPosition()];
        holder.textViewMoaTime.setText(activity.getString(R.string.moa_time, time != null && !time.equals("null") ? time : ""));
        holder.textViewMoaLocation.setText(activity.getString(R.string.moa_location, location != null && !location.equals("null") ? location : ""));
        holder.textViewMoaApplyUnit.setText(activity.getString(R.string.moa_apply_unit, applyUnit != null && !applyUnit.equals("null") ? applyUnit : ""));
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
