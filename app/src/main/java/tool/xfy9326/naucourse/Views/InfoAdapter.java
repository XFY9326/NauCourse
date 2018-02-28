package tool.xfy9326.naucourse.Views;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import tool.xfy9326.naucourse.Activities.InfoDetailActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.JwTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoViewHolder> {
    public static final String TOPIC_SOURCE_JWC = "JWC";
    public static final String TOPIC_SOURCE_JW = "JW";

    private static final int topic_type = 0;
    private static final int topic_title = 1;
    private static final int topic_click = 2;
    private static final int topic_post = 3;
    private static final int topic_date = 4;
    private static final int topic_url = 5;
    private static final int topic_source = 6;

    private final Context context;
    private final ArrayList<ArrayList<String>> topic_data;
    private JwcTopic jwcTopic;
    private JwTopic jwTopic;

    public InfoAdapter(Context context, JwcTopic jwcTopic, JwTopic jwTopic) {
        this.context = context;
        this.jwcTopic = jwcTopic;
        this.jwTopic = jwTopic;
        this.topic_data = new ArrayList<>();
        setData();
    }

    public void updateJwcTopic(JwcTopic jwcTopic, JwTopic jwTopic) {
        this.jwcTopic = jwcTopic;
        this.jwTopic = jwTopic;
        topic_data.clear();
        setData();
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
        return topic_data.size();
    }

    @Override
    public void onBindViewHolder(final InfoViewHolder holder, int position) {
        holder.textView_type.setText(context.getString(R.string.info_type, topic_data.get(holder.getAdapterPosition()).get(topic_type)));
        holder.textView_title.setText(topic_data.get(holder.getAdapterPosition()).get(topic_title));
        String click = topic_data.get(holder.getAdapterPosition()).get(topic_click);
        if (click != null) {
            holder.textView_click.setVisibility(View.VISIBLE);
            holder.textView_click.setText(context.getString(R.string.info_click, click));
        } else {
            holder.textView_click.setVisibility(View.INVISIBLE);
        }
        holder.textView_post.setText(context.getString(R.string.info_post, topic_data.get(holder.getAdapterPosition()).get(topic_post)));
        holder.textView_date.setText(context.getString(R.string.info_date, topic_data.get(holder.getAdapterPosition()).get(topic_date)));
        holder.cardView_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InfoDetailActivity.class);
                intent.putExtra(Config.INTENT_INFO_DETAIL_TITLE, topic_data.get(holder.getAdapterPosition()).get(topic_title));
                intent.putExtra(Config.INTENT_INFO_DETAIL_CLICK, topic_data.get(holder.getAdapterPosition()).get(topic_click));
                intent.putExtra(Config.INTENT_INFO_DETAIL_DATE, topic_data.get(holder.getAdapterPosition()).get(topic_date));
                intent.putExtra(Config.INTENT_INFO_DETAIL_POST, topic_data.get(holder.getAdapterPosition()).get(topic_post));
                intent.putExtra(Config.INTENT_INFO_DETAIL_SOURCE, topic_data.get(holder.getAdapterPosition()).get(topic_source));
                intent.putExtra(Config.INTENT_INFO_DETAIL_URL, topic_data.get(holder.getAdapterPosition()).get(topic_url));
                context.startActivity(intent);
            }
        });
    }

    private void setData() {
        if (jwcTopic != null) {
            for (int i = 0; i < jwcTopic.getTopic_length(); i++) {
                ArrayList<String> data = new ArrayList<>(7);
                data.add(topic_type, jwcTopic.getTopic_type()[i]);
                data.add(topic_title, jwcTopic.getTopic_title()[i]);
                data.add(topic_click, jwcTopic.getTopic_click()[i]);
                data.add(topic_post, jwcTopic.getTopic_post()[i]);
                data.add(topic_date, jwcTopic.getTopic_date()[i]);
                data.add(topic_url, jwcTopic.getTopic_url()[i]);
                data.add(topic_source, TOPIC_SOURCE_JWC);
                topic_data.add(data);
            }
        }
        if (jwTopic != null) {
            for (int i = 0; i < jwTopic.getPostLength(); i++) {
                ArrayList<String> data = new ArrayList<>(7);
                data.add(topic_type, jwTopic.getPostType()[i]);
                data.add(topic_title, jwTopic.getPostTitle()[i]);
                data.add(topic_click, null);
                data.add(topic_post, context.getString(R.string.jwc));
                data.add(topic_date, jwTopic.getPostTime()[i]);
                data.add(topic_url, jwTopic.getPostUrl()[i]);
                data.add(topic_source, TOPIC_SOURCE_JW);
                topic_data.add(data);
            }
        }
        sort();
    }

    synchronized private void sort() {
        Comparator<ArrayList<String>> date_comparator = new Comparator<ArrayList<String>>() {
            @Override
            public int compare(ArrayList<String> o1, ArrayList<String> o2) {
                try {
                    String time1 = o1.get(topic_date).trim();
                    String time2 = o2.get(topic_date).trim();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                    long day1 = simpleDateFormat.parse(time1).getTime();
                    long day2 = simpleDateFormat.parse(time2).getTime();
                    if (day1 > day2) {
                        return -1;
                    } else {
                        return 1;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        };
        Collections.sort(topic_data, date_comparator);
    }

}
