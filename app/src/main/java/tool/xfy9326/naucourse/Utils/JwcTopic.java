package tool.xfy9326.naucourse.Utils;

/**
 * Created by xfy9326 on 18-2-21.
 */

public class JwcTopic {
    private String[] topic_type = null;
    private String[] topic_title = null;
    private String[] topic_click = null;
    private String[] topic_post = null;
    private String[] topic_date = null;
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

    public String[] getTopic_type() {
        return topic_type;
    }

    public void setTopic_type(String[] topic_type) {
        this.topic_type = topic_type;
    }

    public String[] getTopic_title() {
        return topic_title;
    }

    public void setTopic_title(String[] topic_title) {
        this.topic_title = topic_title;
    }

    public String[] getTopic_click() {
        return topic_click;
    }

    public void setTopic_click(String[] topic_click) {
        this.topic_click = topic_click;
    }

    public String[] getTopic_post() {
        return topic_post;
    }

    public void setTopic_post(String[] topic_post) {
        this.topic_post = topic_post;
    }

    public String[] getTopic_date() {
        return topic_date;
    }

    public void setTopic_date(String[] topic_date) {
        this.topic_date = topic_date;
    }

    public String[] getTopic_url() {
        return topic_url;
    }

    public void setTopic_url(String[] topic_url) {
        this.topic_url = topic_url;
    }
}
