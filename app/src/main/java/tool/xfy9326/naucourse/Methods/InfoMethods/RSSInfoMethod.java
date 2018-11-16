package tool.xfy9326.naucourse.Methods.InfoMethods;

import android.content.Context;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Tools.RSSReader;

public class RSSInfoMethod {
    private static final int RSS_TYPE_JW = 1;
    private static final int RSS_TYPE_XW = 2;
    private static final int RSS_TYPE_TW = 3;
    private static final int RSS_TYPE_XXB = 4;

    //private static final int[] RSS_TYPE_ALL = new int[]{RSS_TYPE_JW, RSS_TYPE_XW, RSS_TYPE_TW, RSS_TYPE_XXB};

    private static final String RSS_JW_URL = "http://plus.nau.edu.cn/_wp3services/rssoffer?siteId=126&templateId=221&columnId=4353";
    private static final String RSS_XW_URL = "http://plus.nau.edu.cn/_wp3services/rssoffer?siteId=110&templateId=181&columnId=3439";
    private static final String RSS_TW_URL = "http://plus.nau.edu.cn/_wp3services/rssoffer?siteId=105&templateId=177&columnId=3364";
    private static final String RSS_XXB_URL = "http://plus.nau.edu.cn/_wp3services/rssoffer?siteId=116&templateId=188&columnId=4048";

    private static final String RSS_JW_FILE_NAME = "RssJwTopic";
    private static final String RSS_XW_FILE_NAME = "RssXwTopic";
    private static final String RSS_TW_FILE_NAME = "RssTwTopic";
    private static final String RSS_XXB_FILE_NAME = "RssXxbTopic";
    private static Document document_detail;
    private final SparseArray<RSSReader.RSSObject> rssObjectSparseArray;
    private final Integer[] typeList;
    private final Context context;

    public RSSInfoMethod(@NonNull Context context) {
        this.context = context;
        this.rssObjectSparseArray = new SparseArray<>();
        this.typeList = getShowType(context);
    }

    @Nullable
    public static SparseArray<RSSReader.RSSObject> getOfflineRSSObject(Context context) {
        SparseArray<RSSReader.RSSObject> rssObjectSparseArray = new SparseArray<>();
        for (int typeCode : getShowType(context)) {
            String fileName = getRSSFileName(typeCode);
            if (fileName != null) {
                String jsonData = DataMethod.getOfflineData(context, fileName);
                if (jsonData != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(jsonData);
                        RSSReader.RSSObject rssObject = RSSReader.JSONObjectToRSSObject(jsonObject);
                        if (rssObject != null) {
                            rssObjectSparseArray.put(typeCode, rssObject);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (rssObjectSparseArray.size() != 0) {
            return rssObjectSparseArray;
        }
        return null;
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

    @Nullable
    private static String getRSSFileName(int rssType) {
        switch (rssType) {
            case RSS_TYPE_JW:
                return RSS_JW_FILE_NAME;
            case RSS_TYPE_XW:
                return RSS_XW_FILE_NAME;
            case RSS_TYPE_TW:
                return RSS_TW_FILE_NAME;
            case RSS_TYPE_XXB:
                return RSS_XXB_FILE_NAME;
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

    public static int loadDetail(String url) throws Exception {
        String data = NetMethod.loadUrl(url);
        if (data != null) {
            data = data.replace("&nbsp;", "\\b");
            document_detail = Jsoup.parse(data);
            return Config.NET_WORK_GET_SUCCESS;
        }
        return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
    }

    @NonNull
    public static String getDetail() {
        StringBuilder result = new StringBuilder();
        Elements tags = Objects.requireNonNull(document_detail).body().getElementsByClass("Article_Content");
        tags = tags.first().getElementsByTag("p");
        List<String> data = tags.eachText();
        for (String str : data) {
            if (str.equals("") || str.equals(" ")) {
                result.append("\n");
            } else {
                result.append(str.replace("\\b", " ")).append("\n");
            }
        }
        return result.toString();
    }

    public int load() throws Exception {
        boolean failedLoad = false;
        rssObjectSparseArray.clear();
        for (int typeCode : typeList) {
            String rssUrl = getRSSUrl(typeCode);
            if (rssUrl != null) {
                String data = NetMethod.loadUrl(rssUrl);
                System.gc();
                if (data != null) {
                    RSSReader.RSSObject rssObject = RSSReader.getRSSObject(data);
                    if (rssObject != null) {
                        rssObjectSparseArray.put(typeCode, rssObject);
                        continue;
                    }
                }
            }
            failedLoad = true;
        }
        if (typeList.length != 0 && failedLoad) {
            return Config.NET_WORK_ERROR_CODE_GET_DATA_ERROR;
        }
        return Config.NET_WORK_GET_SUCCESS;
    }

    @Nullable
    public SparseArray<RSSReader.RSSObject> getRSSObject() {
        for (int i = 0; i < rssObjectSparseArray.size(); i++) {
            String fileName = getRSSFileName(rssObjectSparseArray.keyAt(i));
            if (fileName != null) {
                JSONObject jsonObject = RSSReader.RSSObjectToJSONObject(rssObjectSparseArray.valueAt(i));
                if (jsonObject != null) {
                    String data = jsonObject.toString();
                    DataMethod.saveOfflineData(context, data, fileName, false);
                }
            }
        }
        return rssObjectSparseArray;
    }
}
