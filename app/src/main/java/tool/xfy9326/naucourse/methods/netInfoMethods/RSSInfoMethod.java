package tool.xfy9326.naucourse.methods.netInfoMethods;

import android.content.Context;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.DataMethod;
import tool.xfy9326.naucourse.methods.NetMethod;
import tool.xfy9326.naucourse.methods.VPNMethods;
import tool.xfy9326.naucourse.tools.RSSReader;

public class RSSInfoMethod {
    private static final int RSS_TYPE_JW = 1;
    private static final int RSS_TYPE_XW = 2;
    private static final int RSS_TYPE_TW = 3;
    private static final int RSS_TYPE_XXB = 4;

    private static final String RSS_JW_URL = "http://plus.nau.edu.cn/_wp3services/rssoffer?siteId=126&templateId=221&columnId=4353";
    private static final String RSS_XW_URL = "http://plus.nau.edu.cn/_wp3services/rssoffer?siteId=110&templateId=181&columnId=3439";
    private static final String RSS_TW_URL = "http://plus.nau.edu.cn/_wp3services/rssoffer?siteId=105&templateId=177&columnId=3364";
    private static final String RSS_XXB_URL = "http://plus.nau.edu.cn/_wp3services/rssoffer?siteId=116&templateId=188&columnId=4048";

    private static Document document_detail;
    private static String lastLoadInfoDetailHost;
    private final SparseArray<RSSReader.RSSObject> rssObjectSparseArray;
    private final Integer[] typeList;
    private final ExecutorService executorService;
    private boolean hasFailedLoad = false;
    private final Context context;


    public RSSInfoMethod(@NonNull Context context, ExecutorService executorService) {
        this.context = context.getApplicationContext();
        this.executorService = executorService;
        this.rssObjectSparseArray = new SparseArray<>();
        this.typeList = getShowType(context);
    }

    private static Integer[] getShowType(Context context) {
        ArrayList<Integer> result = new ArrayList<>();
        boolean[] showType = DataMethod.InfoData.getInfoChannel(context);
        if (showType[RSS_TYPE_JW]) {
            result.add(RSS_TYPE_JW);
        }
        if (showType[RSS_TYPE_XW]) {
            result.add(RSS_TYPE_XW);
        }
        if (showType[RSS_TYPE_TW]) {
            result.add(RSS_TYPE_TW);
        }
        if (showType[RSS_TYPE_XXB]) {
            result.add(RSS_TYPE_XXB);
        }
        return result.toArray(new Integer[]{});
    }

    @Nullable
    private static String getRSSUrl(int rssType) {
        switch (rssType) {
            case RSS_TYPE_JW:
                return RSS_JW_URL;
            case RSS_TYPE_XW:
                return RSS_XW_URL;
            case RSS_TYPE_TW:
                return RSS_TW_URL;
            case RSS_TYPE_XXB:
                return RSS_XXB_URL;
            default:
                return null;
        }
    }

    @NonNull
    public static String getTypePostName(Context context, int rssType) {
        switch (rssType) {
            case RSS_TYPE_JW:
                return context.getString(R.string.jwc);
            case RSS_TYPE_XW:
                return context.getString(R.string.xw);
            case RSS_TYPE_TW:
                return context.getString(R.string.tw);
            case RSS_TYPE_XXB:
                return context.getString(R.string.xxb);
            default:
                return context.getString(R.string.unknown_post);
        }
    }

    @NonNull
    public static String getTypeName(Context context, int rssType) {
        switch (rssType) {
            case RSS_TYPE_JW:
                return context.getString(R.string.jwc_type);
            case RSS_TYPE_XW:
                return context.getString(R.string.xw_type);
            case RSS_TYPE_TW:
                return context.getString(R.string.tw_type);
            case RSS_TYPE_XXB:
                return context.getString(R.string.xxb_type);
            default:
                return context.getString(R.string.unknown_post_type);
        }
    }

    public static int loadDetail(Context context, String url) throws Exception {
        String data = NetMethod.loadUrlFromLoginClient(context, url, false);
        lastLoadInfoDetailHost = url.substring(0, url.indexOf("/", 9));
        if (data != null) {
            document_detail = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    @NonNull
    public static String getDetail(Context context) {
        Elements tags = Objects.requireNonNull(document_detail).body().getElementsByClass("Article_Content");
        String html = tags.html();
        if (lastLoadInfoDetailHost != null) {
            html = html.replace("href=\"/", "href=\"" + VPNMethods.vpnLinkUrlFix(context, lastLoadInfoDetailHost, "/"))
                    .replaceAll("<img src=\".*?/_ueditor/themes/default/images/icon_.*?.gif.*?\">", "")
                    .replaceAll("<img.*?/?>", "<div><p>" + context.getResources().getString(R.string.image_replace)) + "</p></div>";
        }
        return html;
    }

    public int load() throws Exception {
        hasFailedLoad = false;
        rssObjectSparseArray.clear();
        Future[] futures = new Future[typeList.length];
        for (int i = 0; i < typeList.length; i++) {
            final int type = typeList[i];
            futures[i] = executorService.submit(() -> {
                String rssUrl = getRSSUrl(type);
                if (rssUrl != null) {
                    try {
                        String data = NetMethod.loadUrlFromLoginClient(context, rssUrl, false);
                        if (data != null) {
                            RSSReader.RSSObject rssObject = RSSReader.getRSSObject(data);
                            if (rssObject != null) {
                                rssObjectSparseArray.put(type, rssObject);
                            } else {
                                hasFailedLoad = true;
                            }
                        } else {
                            hasFailedLoad = true;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        hasFailedLoad = true;
                    }
                } else {
                    hasFailedLoad = true;
                }
            });
        }
        for (Future future : futures) {
            future.get(Config.TASK_RUN_MAX_SECOND, TimeUnit.SECONDS);
        }
        if (typeList.length != 0 && hasFailedLoad) {
            return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
        }
        return Config.NET_WORK_GET_SUCCESS;
    }

    @Nullable
    public SparseArray<RSSReader.RSSObject> getRSSObject() {
        return rssObjectSparseArray;
    }
}
