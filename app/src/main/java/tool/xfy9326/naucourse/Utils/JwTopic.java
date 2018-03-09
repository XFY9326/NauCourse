package tool.xfy9326.naucourse.Utils;

/**
 * Created by 10696 on 2018/2/28.
 * 教务处信息
 */

public class JwTopic {
    private String[] postTime;
    private String[] postTitle;
    private String[] postType;
    private String[] postUrl;

    private int postLength = 0;

    public JwTopic() {
        this.postTime = null;
        this.postTitle = null;
        this.postType = null;
        this.postUrl = null;
    }

    public int getPostLength() {
        return postLength;
    }

    @SuppressWarnings("SameParameterValue")
    public void setPostLength(int postLength) {
        this.postLength = postLength;
    }

    public String[] getPostTime() {
        return postTime;
    }

    public void setPostTime(String[] postTime) {
        this.postTime = postTime;
    }

    public String[] getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String[] postTitle) {
        this.postTitle = postTitle;
    }

    public String[] getPostType() {
        return postType;
    }

    public void setPostType(String[] postType) {
        this.postType = postType;
    }

    public String[] getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String[] postUrl) {
        this.postUrl = postUrl;
    }
}
