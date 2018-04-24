package tool.xfy9326.naucourse.Utils;

import android.support.annotation.Nullable;

/**
 * Created by 10696 on 2018/2/28.
 * 教务处信息
 */

public class JwTopic {
    @Nullable
    private String[] postTime;
    @Nullable
    private String[] postTitle;
    @Nullable
    private String[] postType;
    @Nullable
    private String[] postUrl;
    private int dataVersionCode = 0;

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

    @Nullable
    public String[] getPostTime() {
        return postTime;
    }

    public void setPostTime(@Nullable String[] postTime) {
        this.postTime = postTime;
    }

    @Nullable
    public String[] getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(@Nullable String[] postTitle) {
        this.postTitle = postTitle;
    }

    @Nullable
    public String[] getPostType() {
        return postType;
    }

    public void setPostType(@Nullable String[] postType) {
        this.postType = postType;
    }

    @Nullable
    public String[] getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(@Nullable String[] postUrl) {
        this.postUrl = postUrl;
    }

    @SuppressWarnings("unused")
    public int getDataVersionCode() {
        return dataVersionCode;
    }

    @SuppressWarnings("SameParameterValue")
    public void setDataVersionCode(int dataVersionCode) {
        this.dataVersionCode = dataVersionCode;
    }
}
