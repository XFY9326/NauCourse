package tool.xfy9326.naucourse.Methods;

import android.content.Context;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

import tool.xfy9326.naucourse.Methods.NetInfoMethods.RSSInfoMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Tools.RSSReader;
import tool.xfy9326.naucourse.Utils.AlstuTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;
import tool.xfy9326.naucourse.Utils.TopicInfo;

public class InfoMethod {
    public static final String TOPIC_SOURCE_JWC = "JWC";
    public static final String TOPIC_SOURCE_RSS = "RSS";
    public static final String TOPIC_SOURCE_ALSTU = "ALSTU";
    public static final String FILE_NAME = "TopicInfo";
    public static final boolean IS_ENCRYPT = false;
    private static Comparator<TopicInfo> date_comparator;

    //设置数据（多来源数据整合）
    synchronized public static ArrayList<TopicInfo> combineData(final Context context, final JwcTopic jwcTopic, final AlstuTopic alstuTopic, final SparseArray<RSSReader.RSSObject> rssObjects) {
        ArrayList<TopicInfo> topic_data = new ArrayList<>();
        if (jwcTopic != null) {
            for (int i = 0; i < jwcTopic.getTopic_length(); i++) {
                long date = TimeMethod.getInfoDateLong(Objects.requireNonNull(jwcTopic.getTopic_date())[i]);
                if (isKeepMsgDate(date)) {
                    TopicInfo infoDetail = new TopicInfo();
                    infoDetail.setTitle(Objects.requireNonNull(jwcTopic.getTopic_title())[i]);
                    infoDetail.setClick(Objects.requireNonNull(jwcTopic.getTopic_click())[i]);
                    infoDetail.setDate(jwcTopic.getTopic_date()[i]);
                    infoDetail.setDateLong(date);
                    infoDetail.setPost(Objects.requireNonNull(jwcTopic.getTopic_post())[i]);
                    infoDetail.setSource(TOPIC_SOURCE_JWC);
                    infoDetail.setUrl(Objects.requireNonNull(jwcTopic.getTopic_url())[i]);
                    infoDetail.setType(Objects.requireNonNull(jwcTopic.getTopic_type())[i]);

                    topic_data.add(infoDetail);
                }
            }
        }

        if (alstuTopic != null) {
            for (int i = 0; i < alstuTopic.getTopic_length(); i++) {
                long date = TimeMethod.getInfoDateLong(Objects.requireNonNull(alstuTopic.getTopicDate())[i]);
                if (isKeepMsgDate(date)) {
                    TopicInfo infoDetail = new TopicInfo();
                    infoDetail.setTitle(Objects.requireNonNull(alstuTopic.getTopicTitle())[i]);
                    infoDetail.setClick(null);
                    infoDetail.setDate(alstuTopic.getTopicDate()[i]);
                    infoDetail.setDateLong(date);
                    infoDetail.setPost(context.getString(R.string.alstu_system));
                    infoDetail.setSource(TOPIC_SOURCE_ALSTU);
                    infoDetail.setUrl(Objects.requireNonNull(alstuTopic.getTopicUrl())[i]);
                    infoDetail.setType(context.getString(R.string.alstu_msg));

                    topic_data.add(infoDetail);
                }
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
                            long date = TimeMethod.getInfoDateLong(rssItem.getDate());
                            if (isKeepMsgDate(date)) {
                                TopicInfo infoDetail = new TopicInfo();
                                infoDetail.setTitle(rssItem.getTitle());
                                infoDetail.setClick(null);
                                infoDetail.setDate(rssItem.getDate());
                                infoDetail.setDateLong(date);
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
        }

        sort(topic_data);

        return topic_data;
    }

    //删除超过三个月的消息
    private static boolean isKeepMsgDate(long topic) {
        long now = System.currentTimeMillis();
        if (topic > 0) {
            try {
                if (now > topic) {
                    int day = (int) ((now - topic) / (1000 * 3600 * 24));
                    if (day > (31 * 3)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    synchronized private static void sort(ArrayList<TopicInfo> topic_data) {
        if (date_comparator == null) {
            date_comparator = (o1, o2) -> {
                if (o1.getDateLong() > 0 && o2.getDateLong() > 0) {
                    return Long.compare(o2.getDateLong(), o1.getDateLong());
                }
                return 0;
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
