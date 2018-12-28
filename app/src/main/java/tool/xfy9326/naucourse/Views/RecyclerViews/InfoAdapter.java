package tool.xfy9326.naucourse.Views.RecyclerViews;

import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import tool.xfy9326.naucourse.Activities.InfoDetailActivity;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.InfoMethods.RSSInfoMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Tools.RSSReader;
import tool.xfy9326.naucourse.Utils.AlstuTopic;
import tool.xfy9326.naucourse.Utils.InfoDetail;
import tool.xfy9326.naucourse.Utils.JwcTopic;

/**
 * Created by xfy9326 on 18-2-20.
 * 主页信息显示
 */

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.InfoViewHolder> {
    public static final String TOPIC_SOURCE_JWC = "JWC";
    public static final String TOPIC_SOURCE_RSS = "RSS";
    public static final String TOPIC_SOURCE_ALSTU = "ALSTU";

    private final Context context;
    @NonNull
    private final ArrayList<InfoDetail> topic_data;
    private final ArrayList<InfoDetail> topic_data_new;
    private JwcTopic jwcTopic;
    private AlstuTopic alstuTopic;
    private SparseArray<RSSReader.RSSObject> rssObjects;
    private Comparator<InfoDetail> date_comparator;

    public InfoAdapter(Context context, JwcTopic jwcTopic, AlstuTopic alstuTopic, SparseArray<RSSReader.RSSObject> rssObjects) {
        this.context = context;
        this.jwcTopic = jwcTopic;
        this.alstuTopic = alstuTopic;
        this.rssObjects = rssObjects;
        this.date_comparator = null;
        this.topic_data = new ArrayList<>();
        this.topic_data_new = new ArrayList<>();
        setData(topic_data);
    }

    //删除超过三个月的消息
    synchronized private static void deleteOutOfDateMsg(ArrayList<InfoDetail> topic_data) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        long now = System.currentTimeMillis();
        Iterator<InfoDetail> iterator = topic_data.iterator();
        while (iterator.hasNext()) {
            InfoDetail detail = iterator.next();
            if (detail != null) {
                try {
                    long topic = simpleDateFormat.parse(detail.getDate()).getTime();
                    if (now > topic) {
                        int day = (int) ((now - topic) / (1000 * 3600 * 24));
                        if (day > (31 * 3)) {
                            iterator.remove();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
        final InfoDetail detail = topic_data.get(holder.getAdapterPosition());
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
            holder.cardView_info.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, InfoDetailActivity.class);
                    intent.putExtra(Config.INTENT_INFO_DETAIL, detail);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }
    }

    //更新列表
    synchronized public void updateJwcTopic(JwcTopic jwcTopic, AlstuTopic alstuTopic, SparseArray<RSSReader.RSSObject> rssObjects) {
        this.jwcTopic = jwcTopic;
        this.alstuTopic = alstuTopic;
        this.rssObjects = rssObjects;
        if (this.topic_data.isEmpty()) {
            setData(this.topic_data);
        } else {
            this.topic_data.clear();
            setData(topic_data_new);
            this.topic_data.addAll(topic_data_new);
            this.topic_data_new.clear();
        }
        notifyDataSetChanged();
    }

    //设置数据（多来源数据整合）
    synchronized private void setData(ArrayList<InfoDetail> topic_data) {
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
        if (alstuTopic != null) {
            for (int i = 0; i < alstuTopic.getTopic_length(); i++) {
                InfoDetail infoDetail = new InfoDetail();
                infoDetail.setTitle(Objects.requireNonNull(alstuTopic.getTopicTitle())[i]);
                infoDetail.setClick(null);
                infoDetail.setDate(Objects.requireNonNull(alstuTopic.getTopicDate())[i]);
                infoDetail.setPost(context.getString(R.string.alstu_system));
                infoDetail.setSource(TOPIC_SOURCE_ALSTU);
                infoDetail.setUrl(Objects.requireNonNull(alstuTopic.getTopicUrl())[i]);
                infoDetail.setType(context.getString(R.string.alstu_msg));
                topic_data.add(infoDetail);
            }
        }
        if (rssObjects != null) {
            for (int i = 0; i < rssObjects.size(); i++) {
                String post = RSSInfoMethod.getTypePostName(context, rssObjects.keyAt(i));
                String defaultType = RSSInfoMethod.getTypeName(context, rssObjects.keyAt(i));

                RSSReader.RSSObject rssObject = rssObjects.valueAt(i);
                if (rssObject != null) {
                    for (RSSReader.RSSChannel rssChannel : rssObject.getChannels()) {
                        for (RSSReader.RSSItem rssItem : rssChannel.getItems()) {
                            InfoDetail infoDetail = new InfoDetail();
                            infoDetail.setTitle(rssItem.getTitle());
                            infoDetail.setClick(null);
                            infoDetail.setDate(rssItem.getDate());
                            infoDetail.setPost(post);
                            infoDetail.setSource(TOPIC_SOURCE_RSS);
                            infoDetail.setUrl(rssItem.getLink());
                            infoDetail.setType(rssItem.getType() == null ? defaultType : rssItem.getType());
                            topic_data.add(infoDetail);
                        }
                    }
                }
            }
        }
        deleteOutOfDateMsg(topic_data);
        sort(topic_data);
    }

    //数据按照日期排序
    synchronized private void sort(ArrayList<InfoDetail> topic_data) {
        if (date_comparator == null) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
            date_comparator = new Comparator<InfoDetail>() {
                @Override
                public int compare(InfoDetail o1, InfoDetail o2) {
                    if (o1.getDate() != null && o2.getDate() != null) {
                        String time1 = o1.getDate().trim();
                        String time2 = o2.getDate().trim();
                        try {
                            //格式化日期显示
                            Date date1 = simpleDateFormat.parse(time1);
                            Date date2 = simpleDateFormat.parse(time2);
                            o1.setDate(simpleDateFormat.format(date1));
                            o2.setDate(simpleDateFormat.format(date2));

                            long day1 = date1.getTime();
                            long day2 = date2.getTime();
                            return Long.compare(day2, day1);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return 0;
                }
            };
        }
        if (!topic_data.isEmpty()) {
            try {
                Collections.sort(topic_data, date_comparator);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
