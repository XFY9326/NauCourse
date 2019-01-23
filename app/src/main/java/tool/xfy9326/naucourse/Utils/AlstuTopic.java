package tool.xfy9326.naucourse.Utils;

public class AlstuTopic extends BaseData {
    private String[] topicTitle;
    private String[] topicDate;
    private String[] topicUrl;
    private int topic_length = 0;

    public AlstuTopic() {
    }

    public String[] getTopicTitle() {
        return topicTitle;
    }

    public void setTopicTitle(String[] topicTitle) {
        this.topicTitle = topicTitle;
    }

    public String[] getTopicDate() {
        return topicDate;
    }

    public void setTopicDate(String[] topicDate) {
        this.topicDate = topicDate;
    }

    public String[] getTopicUrl() {
        return topicUrl;
    }

    public void setTopicUrl(String[] topicUrl) {
        this.topicUrl = topicUrl;
    }

    public int getTopic_length() {
        return topic_length;
    }

    public void setTopic_length(int topic_length) {
        this.topic_length = topic_length;
    }
}
