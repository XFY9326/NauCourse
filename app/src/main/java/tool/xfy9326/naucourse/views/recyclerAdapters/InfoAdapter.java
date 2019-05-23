package tool.xfy9326.naucourse.views.recyclerAdapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.activities.InfoDetailActivity;
import tool.xfy9326.naucourse.utils.TopicInfo;

/**
 * Created by xfy9326 on 18-2-20.
 * 主页信息显示
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InfoViewHolder> {
    @NonNull
    private final Context context;
    @NonNull
    private ArrayList<TopicInfo> topic_data;

    public InfoAdapter(@NonNull Context context, @NonNull ArrayList<TopicInfo> topic_data) {
        this.context = context;
        this.topic_data = topic_data;
    }

    @NonNull
    @Override
    public InfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_info_card, parent, false);
        return new InfoViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return topic_data.size();
    }

    @Override
    public void onBindViewHolder(@NonNull final InfoViewHolder holder, int position) {
        final TopicInfo detail = topic_data.get(holder.getAdapterPosition());
        String title = detail.getTitle();
        if (title != null) {
            holder.textView_type.setText(context.getString(R.string.info_type, detail.getType()));
            holder.textView_title.setText(title);
            String click = detail.getClick();
            if (click != null) {
                holder.textView_click.setVisibility(View.VISIBLE);
                holder.textView_click.setText(context.getString(R.string.info_click, click));
            } else {
                holder.textView_click.setVisibility(View.INVISIBLE);
            }
            holder.textView_post.setText(context.getString(R.string.info_post, detail.getPost()));
            holder.textView_date.setText(context.getString(R.string.info_date, detail.getDate()));
            holder.cardView_info.setOnClickListener(v -> {
                Intent intent = new Intent(context, InfoDetailActivity.class);
                intent.putExtra(Config.INTENT_INFO_DETAIL, detail);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
        }
    }

    //更新列表
    synchronized public void updateTopic(@NonNull ArrayList<TopicInfo> topicInfo) {
        this.topic_data = topicInfo;
        notifyDataSetChanged();
    }

    /**
     * Created by xfy9326 on 18-2-20.
     */

    static class InfoViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView_info;
        final TextView textView_type;
        final TextView textView_title;
        final TextView textView_click;
        final TextView textView_post;
        final TextView textView_date;

        InfoViewHolder(@NonNull View view) {
            super(view);
            cardView_info = view.findViewById(R.id.cardView_info);
            textView_type = view.findViewById(R.id.textView_info_type);
            textView_title = view.findViewById(R.id.textView_info_title);
            textView_click = view.findViewById(R.id.textView_info_click);
            textView_post = view.findViewById(R.id.textView_info_post);
            textView_date = view.findViewById(R.id.textView_info_date);
        }
    }
}
