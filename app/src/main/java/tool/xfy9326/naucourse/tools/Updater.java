package tool.xfy9326.naucourse.tools;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class Updater {
    private static final String checkUrl = "https://www.coolapk.com/apk/178329";
    private static Updater INSTANCE = null;
    private final OkHttpClient client;

    private Updater() {
        client = new OkHttpClient();
    }

    public static Updater getInstance() {
        synchronized (Updater.class) {
            if (INSTANCE == null) {
                INSTANCE = new Updater();
            }
        }
        return INSTANCE;
    }

    public void checkUpdate(@NonNull String versionName, @NonNull final OnUpdateListener updateListener) {
        client.newCall(new Request.Builder().url(checkUrl).build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                updateListener.onError();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                ResponseBody body = response.body();
                if (body != null) {
                    String content = body.string();

                    Document document = Jsoup.parse(content);
                    Element newVersion = document.selectFirst("body > div > div:nth-child(2) > div.app_left > div.apk_left_one > div > div > div.apk_topbar_mss > p.detail_app_title > span");
                    Element updateInfo = document.selectFirst("body > div > div:nth-child(2) > div.app_left > div.apk_left_two > div > div:nth-child(2) > p.apk_left_title_info");
                    if (newVersion != null && updateInfo != null) {
                        String name = newVersion.text().trim();
                        if (name.contains(".") && versionName.contains(".")) {
                            String[] newTemp = name.split("\\.");
                            String[] nowTemp = versionName.split("\\.");
                            boolean hasUpdate = false;
                            try {
                                for (int i = 0; i < Math.max(newTemp.length, nowTemp.length) && !hasUpdate; i++) {
                                    if (i < newTemp.length && i < nowTemp.length) {
                                        if (Integer.valueOf(newTemp[i]) > Integer.valueOf(nowTemp[i])) {
                                            hasUpdate = true;
                                        }
                                    } else {
                                        hasUpdate = newTemp.length > nowTemp.length;
                                    }
                                }
                                if (hasUpdate) {
                                    updateListener.findUpdate(newVersion.text().trim(), updateInfo.html().replace("<br>", "\n"), checkUrl);
                                } else {
                                    updateListener.noUpdate();
                                }
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                                updateListener.onError();
                            }
                        } else {
                            updateListener.onError();
                        }
                    } else {
                        updateListener.onError();
                    }
                } else {
                    updateListener.onError();
                }
                response.close();
            }
        });
    }

    public interface OnUpdateListener {
        void noUpdate();

        void onError();

        void findUpdate(String versionName, String updateInfo, String updateUrl);
    }
}
