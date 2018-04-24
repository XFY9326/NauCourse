package tool.xfy9326.naucourse.Views;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import java.util.Objects;

import tool.xfy9326.naucourse.Activities.InfoDetailActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Utils.InfoDetail;
import tool.xfy9326.naucourse.Utils.JwTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;

/**
 * Created by xfy9326 on 18-2-20.
 * 主页信息显示
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoViewHolder> {
    public static final String TOPIC_SOURCE_JWC = "JWC";
    public static final String TOPIC_SOURCE_JW = "JW";

    private final Context context;
    @NonNull
    private final ArrayList<InfoDetail> topic_data;
    private JwcTopic jwcTopic;
    private JwTopic jwTopic;

    public InfoAdapter(Context context, JwcTopic jwcTopic, JwTopic jwTopic) {
        this.context = context;
        this.jwcTopic = jwcTopic;
        this.jwTopic = jwTopic;
        this.topic_data = new ArrayList<>();
        setData();
    }

    //更新列表
    public void updateJwcTopic(JwcTopic jwcTopic, JwTopic jwTopic) {
        this.jwcTopic = jwcTopic;
        this.jwTopic = jwTopic;
        topic_data.clear();
        setData();
        notifyDataSetChanged();
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
        holder.textView_type.setText(context.getString(R.string.info_type, topic_data.get(holder.getAdapterPosition()).getType()));
        holder.textView_title.setText(topic_data.get(holder.getAdapterPosition()).getTitle());
        String click = topic_data.get(holder.getAdapterPosition()).getClick();
        if (click != null) {
            holder.textView_click.setVisibility(View.VISIBLE);
            holder.textView_click.setText(context.getString(R.string.info_click, click));
        } else {
            holder.textView_click.setVisibility(View.INVISIBLE);
        }
        holder.textView_post.setText(context.getString(R.string.info_post, topic_data.get(holder.getAdapterPosition()).getPost()));
        holder.textView_date.setText(context.getString(R.string.info_date, topic_data.get(holder.getAdapterPosition()).getDate()));
        holder.cardView_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, InfoDetailActivity.class);
                intent.putExtra(Config.INTENT_INFO_DETAIL, topic_data.get(holder.getAdapterPosition()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    //设置数据（多来源数据整合）
    private void setData() {
        if (jwcTopic != null) {
            for (int i = 0; i < jwcTopic.getTopic_length(); i++) {
                InfoDetail infoDetail = new InfoDetail();
                infoDetail.setTitle(Objects.requireNonNull(jwcTopic.getTopic_title())[i]);
                infoDetail.setClick(Objects.requireNonNull(jwcTopic.getTopic_click())[i]);
                infoDetail.setDate(Objects.requireNonNull(jwcTopic.getTopic_date())[i]);
                infoDetail.setPost(Objects.requireNonNull(jwcTopic.getTopic_post())[i]);
                infoDetail.setSource(TOPIC_SOURCE_JWC);
                infoDetail.setUrl(Objects.requireNonNull(jwcTopic.getTopic_url())[i]);
                infoDetail.setType(Objects.requireNonNull(jwcTopic.getTopic_type())[i]);
                topic_data.add(infoDetail);
            }
        }
        if (jwTopic != null) {
            for (int i = 0; i < jwTopic.getPostLength(); i++) {
                InfoDetail infoDetail = new InfoDetail();
                infoDetail.setTitle(Objects.requireNonNull(jwTopic.getPostTitle())[i]);
                infoDetail.setClick(null);
                infoDetail.setDate(Objects.requireNonNull(jwTopic.getPostTime())[i]);
                infoDetail.setPost(context.getString(R.string.jwc));
                infoDetail.setSource(TOPIC_SOURCE_JW);
                infoDetail.setUrl(Objects.requireNonNull(jwTopic.getPostUrl())[i]);
                infoDetail.setType(Objects.requireNonNull(jwTopic.getPostType())[i]);
                topic_data.add(infoDetail);
            }
        }
        sort();
    }

    //数据按照日期排序
    synchronized private void sort() {
        Comparator<InfoDetail> date_comparator = new Comparator<InfoDetail>() {
            @Override
            public int compare(InfoDetail o1, InfoDetail o2) {
                try {
                    String time1 = Objects.requireNonNull(o1.getDate()).trim();
                    String time2 = Objects.requireNonNull(o2.getDate()).trim();
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
