package tool.xfy9326.naucourse.beans.info;

import androidx.annotation.Nullable;

import tool.xfy9326.naucourse.beans.BaseData;

/**
 * Created by xfy9326 on 18-2-21.
 * 教务系统信息
 */

public class JwcTopic extends BaseData {
    @Nullable
    private String[] topic_type = null;
    @Nullable
    private String[] topic_title = null;
    @Nullable
    private String[] topic_click = null;
    @Nullable
    private String[] topic_post = null;
    @Nullable
    private String[] topic_date = null;
    @Nullable
    private String[] topic_url = null;
    private int topic_length = 0;

    public JwcTopic() {
    }

    public int getTopic_length() {
        return topic_length;
    }

    @SuppressWarnings("SameParameterValue")
    public void setTopic_length(int topic_length) {
        this.topic_length = topic_length;
    }

    @Nullable
    public String[] getTopic_type() {
        return topic_type;
    }

    public void setTopic_type(@Nullable String[] topic_type) {
        this.topic_type = topic_type;
    }

    @Nullable
    public String[] getTopic_title() {
        return topic_title;
    }

    public void setTopic_title(@Nullable String[] topic_title) {
        this.topic_title = topic_title;
    }

    @Nullable
    public String[] getTopic_click() {
        return topic_click;
    }

    public void setTopic_click(@Nullable String[] topic_click) {
        this.topic_click = topic_click;
    }

    @Nullable
    public String[] getTopic_post() {
        return topic_post;
    }

    public void setTopic_post(@Nullable String[] topic_post) {
        this.topic_post = topic_post;
    }

    @Nullable
    public String[] getTopic_date() {
        return topic_date;
    }

    public void setTopic_date(@Nullable String[] topic_date) {
        this.topic_date = topic_date;
    }

    @Nullable
    public String[] getTopic_url() {
        return topic_url;
    }

    public void setTopic_url(@Nullable String[] topic_url) {
        this.topic_url = topic_url;
    }

}
