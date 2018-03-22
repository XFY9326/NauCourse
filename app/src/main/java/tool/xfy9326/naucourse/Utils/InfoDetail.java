package tool.xfy9326.naucourse.Utils;

import java.io.Serializable;

/**
 * Created by 10696 on 2018/3/22.
 */

public class InfoDetail implements Serializable {
    private String title;
    private String date;
    private String click;
    private String post;
    private String source;
    private String url;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getClick() {
        return click;
    }

    public void setClick(String click) {
        this.click = click;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
