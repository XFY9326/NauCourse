package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.Objects;

import tool.xfy9326.naucourse.Methods.InfoMethods.RSSInfoMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Tools.RSSReader;
import tool.xfy9326.naucourse.Utils.AlstuTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;
import tool.xfy9326.naucourse.Utils.TopicInfo;

public class InfoMethod {
    public static final String TOPIC_SOURCE_JWC = "JWC";
    public static final String TOPIC_SOURCE_RSS = "RSS";
    public static final String TOPIC_SOURCE_ALSTU = "ALSTU";
    private static Comparator<TopicInfo> date_comparator;

    //设置数据（多来源数据整合）
    public static void combineData(Context context, ArrayList<TopicInfo> topic_data, JwcTopic jwcTopic, AlstuTopic alstuTopic, SparseArray<RSSReader.RSSObject> rssObjects) {
        if (jwcTopic != null) {
            for (int i = 0; i < jwcTopic.getTopic_length(); i++) {
                TopicInfo infoDetail = new TopicInfo();
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
                TopicInfo infoDetail = new TopicInfo();
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
                            TopicInfo infoDetail = new TopicInfo();
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

    //删除超过三个月的消息
    private static void deleteOutOfDateMsg(ArrayList<TopicInfo> topic_data) {
        long now = System.currentTimeMillis();
        Iterator<TopicInfo> iterator = topic_data.iterator();
        while (iterator.hasNext()) {
            TopicInfo detail = iterator.next();
            if (detail != null) {
                try {
                    long topic = TimeMethod.simpleDateFormat.parse(detail.getDate()).getTime();
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

    private static void sort(ArrayList<TopicInfo> topic_data) {
        if (date_comparator == null) {
            date_comparator = new Comparator<TopicInfo>() {
                @Override
                public int compare(TopicInfo o1, TopicInfo o2) {
                    if (o1.getDate() != null && o2.getDate() != null) {
                        String time1 = o1.getDate().trim();
                        String time2 = o2.getDate().trim();
                        try {
                            //格式化日期显示
                            Date date1 = TimeMethod.simpleDateFormat.parse(time1);
                            Date date2 = TimeMethod.simpleDateFormat.parse(time2);
                            o1.setDate(TimeMethod.simpleDateFormat.format(date1));
                            o2.setDate(TimeMethod.simpleDateFormat.format(date2));

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
}
