package tool.xfy9326.naucourse.Views;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import tool.xfy9326.naucourse.Activities.InfoDetailActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.JwcTopic;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoViewHolder> {
    private final Context context;
    private JwcTopic jwcTopic;

    public InfoAdapter(Context context, JwcTopic jwcTopic) {
        this.context = context;
        this.jwcTopic = jwcTopic;
    }

    public void updateJwcTopic(JwcTopic jwcTopic) {
        this.jwcTopic = jwcTopic;
        notifyDataSetChanged();
    }

    @Override
    public InfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.item_card, parent, false);
        return new InfoViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return jwcTopic.getTopic_length();
    }

    @Override
    public void onBindViewHolder(final InfoViewHolder holder, int position) {
        final String[] topic_url = jwcTopic.getTopic_url();
        holder.textView_type.setText(context.getString(R.string.info_type, jwcTopic.getTopic_type()[holder.getAdapterPosition()]));
        holder.textView_title.setText(jwcTopic.getTopic_title()[holder.getAdapterPosition()]);
        holder.textView_click.setText(context.getString(R.string.info_click, jwcTopic.getTopic_click()[holder.getAdapterPosition()]));
        holder.textView_post.setText(context.getString(R.string.info_post, jwcTopic.getTopic_post()[holder.getAdapterPosition()]));
        holder.textView_date.setText(context.getString(R.string.info_date, jwcTopic.getTopic_date()[holder.getAdapterPosition()]));
        holder.cardView_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InfoDetailActivity.class);
                intent.putExtra(Config.INTENT_INFO_DETAIL_TITLE, jwcTopic.getTopic_title()[holder.getAdapterPosition()]);
                intent.putExtra(Config.INTENT_INFO_DETAIL_CLICK, jwcTopic.getTopic_click()[holder.getAdapterPosition()]);
                intent.putExtra(Config.INTENT_INFO_DETAIL_DATE, jwcTopic.getTopic_date()[holder.getAdapterPosition()]);
                intent.putExtra(Config.INTENT_INFO_DETAIL_POST, jwcTopic.getTopic_post()[holder.getAdapterPosition()]);
                intent.putExtra(Config.INTENT_INFO_DETAIL_URL, topic_url[holder.getAdapterPosition()]);
                context.startActivity(intent);
            }
        });
    }
}
