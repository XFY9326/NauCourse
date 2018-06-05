package tool.xfy9326.naucourse.Views;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tool.xfy9326.naucourse.Activities.MoaActivity;
import tool.xfy9326.naucourse.Methods.MoaMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.Moa;

public class MoaAdapter extends RecyclerView.Adapter<MoaViewHolder> {
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

    @Override
    public void onBindViewHolder(@NonNull MoaViewHolder holder, int position) {
        holder.textViewMoaTitle.setText(moa.getTitle()[holder.getAdapterPosition()]);
        holder.textViewMoaType.setText(activity.getString(R.string.moa_type, (moa.getType()[holder.getAdapterPosition()]).equalsIgnoreCase(MoaMethod.Academic_Report) ? activity.getString(R.string.academic_report) : activity.getString(R.string.large_academic_conference)));
        holder.textViewMoaReporter.setText(activity.getString(R.string.moa_reporter, moa.getReporter()[holder.getAdapterPosition()]));
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
}
