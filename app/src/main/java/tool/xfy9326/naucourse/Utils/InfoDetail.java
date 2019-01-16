package tool.xfy9326.naucourse.Utils;

import androidx.annotation.Nullable;

/**
 * Created by 10696 on 2018/3/22.
 */

public class InfoDetail extends BaseUtils {
    @Nullable
    private String title;
    @Nullable
    private String date;
    @Nullable
    private String click;
    @Nullable
    private String post;
    @Nullable
    private String source;
    @Nullable
    private String url;
    @Nullable
    private String type;

    public InfoDetail() {
        this.title = null;
        this.date = null;
        this.click = null;
        this.post = null;
        this.source = null;
        this.url = null;
        this.type = null;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    public void setTitle(@Nullable String title) {
        this.title = title;
    }

    @Nullable
    public String getDate() {
        return date;
    }

    public void setDate(@Nullable String date) {
        this.date = date;
    }

    @Nullable
    public String getClick() {
        return click;
    }

    public void setClick(@Nullable String click) {
        this.click = click;
    }

    @Nullable
    public String getPost() {
        return post;
    }

    public void setPost(@Nullable String post) {
        this.post = post;
    }

    @Nullable
    public String getSource() {
        return source;
    }

    public void setSource(@Nullable String source) {
        this.source = source;
    }

    @Nullable
    public String getUrl() {
        return url;
    }

    public void setUrl(@Nullable String url) {
        this.url = url;
    }
}
