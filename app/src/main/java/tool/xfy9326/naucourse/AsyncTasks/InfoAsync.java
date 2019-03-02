package tool.xfy9326.naucourse.AsyncTasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.SparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Fragments.HomeFragment;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.DataMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.AlstuMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.JwcInfoMethod;
import tool.xfy9326.naucourse.Methods.InfoMethods.RSSInfoMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Tools.RSSReader;
import tool.xfy9326.naucourse.Utils.AlstuTopic;
import tool.xfy9326.naucourse.Utils.JwcTopic;
import tool.xfy9326.naucourse.Views.ViewPagerAdapter;

/**
 * Created by 10696 on 2018/3/2.
 */

public class InfoAsync extends AsyncTask<Context, Void, Context> {
    private int JwcLoadSuccess = -1;
    private int rssLoadSuccess = -1;
    private int alstuLoadSuccess = -1;
    private int loadCode = Config.NET_WORK_GET_SUCCESS;
    @Nullable
    private JwcTopic jwcTopic;
    @Nullable
    private AlstuTopic alstuTopic;
    @Nullable
    private SparseArray<RSSReader.RSSObject> rssObjects;

    public InfoAsync() {
        this.jwcTopic = null;
        this.rssObjects = null;
        this.alstuTopic = null;
    }

    @Override
    protected Context doInBackground(final Context... context) {
        int loadTime = 0;
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context[0]).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            HomeFragment homeFragment = viewPagerAdapter.getHomeFragment();
            if (homeFragment != null) {
                loadTime = homeFragment.getLoadTime();
            }
            final boolean[] infoChannel = DataMethod.InfoData.getInfoChannel(context[0]);
            final boolean showJwcTopic = infoChannel[JwcInfoMethod.TYPE_JWC];
            final boolean showAlstuTopic = infoChannel[AlstuMethod.TYPE_ALSTU];

            if (loadTime == 0) {
                //首次只加载离线数据
                Thread[] threads = new Thread[3];
                threads[0] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (showJwcTopic) {
                            jwcTopic = (JwcTopic) DataMethod.getOfflineData(context[0], JwcTopic.class, JwcInfoMethod.FILE_NAME);
                        } else {
                            jwcTopic = new JwcTopic();
                        }
                        JwcLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    }
                });
                threads[1] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (showAlstuTopic) {
                            alstuTopic = (AlstuTopic) DataMethod.getOfflineData(context[0], AlstuTopic.class, AlstuMethod.FILE_NAME);
                        } else {
                            alstuTopic = new AlstuTopic();
                        }
                        alstuLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    }
                });
                threads[2] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        rssObjects = RSSInfoMethod.getOfflineRSSObject(context[0]);
                        rssLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                    }
                });

                for (Thread thread : threads) {
                    thread.start();
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                Thread[] threads = new Thread[3];
                threads[0] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (showJwcTopic) {
                                JwcInfoMethod jwcInfoMethod = new JwcInfoMethod(context[0]);
                                JwcLoadSuccess = jwcInfoMethod.load();
                                if (JwcLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                                    jwcTopic = jwcInfoMethod.getJwcTopic();
                                }
                            } else {
                                jwcTopic = new JwcTopic();
                                JwcLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                            }
                        } catch (Exception e) {
                            JwcLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                            e.printStackTrace();
                        }
                    }
                });
                threads[1] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (showAlstuTopic) {
                                AlstuMethod alstuMethod = new AlstuMethod(context[0]);
                                alstuLoadSuccess = alstuMethod.load();
                                if (alstuLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                                    alstuTopic = alstuMethod.getAlstuTopic();
                                }
                            } else {
                                alstuTopic = new AlstuTopic();
                                alstuLoadSuccess = Config.NET_WORK_GET_SUCCESS;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            alstuLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                        }
                    }
                });
                threads[2] = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            RSSInfoMethod rssInfoMethod = new RSSInfoMethod(context[0]);
                            rssLoadSuccess = rssInfoMethod.load();
                            if (rssLoadSuccess == Config.NET_WORK_GET_SUCCESS) {
                                rssObjects = rssInfoMethod.getRSSObject();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            rssLoadSuccess = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
                        }
                    }
                });

                for (Thread thread : threads) {
                    thread.start();
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (homeFragment != null) {
                homeFragment.setLoadTime(++loadTime);
            }
            if ((rssLoadSuccess == -1 || rssLoadSuccess == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) &&
                    (alstuLoadSuccess == -1 || alstuLoadSuccess == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) &&
                    (JwcLoadSuccess == -1 || JwcLoadSuccess == Config.NET_WORK_ERROR_CODE_CONNECT_ERROR) &&
                    (rssLoadSuccess != -1 && alstuLoadSuccess != -1 && JwcLoadSuccess != -1)) {
                loadCode = Config.NET_WORK_ERROR_CODE_CONNECT_ERROR;
            }
            if (loadTime > 2) {
                BaseMethod.getApp(context[0]).setShowConnectErrorOnce(false);
            }
        }
        return context[0];
    }

    @Override
    protected void onPostExecute(@NonNull Context context) {
        ViewPagerAdapter viewPagerAdapter = BaseMethod.getApp(context).getViewPagerAdapter();
        if (viewPagerAdapter != null) {
            HomeFragment homeFragment = viewPagerAdapter.getHomeFragment();
            if (homeFragment != null) {
                if (NetMethod.checkNetWorkCode(context, new int[]{rssLoadSuccess, alstuLoadSuccess, JwcLoadSuccess}, loadCode, true)) {
                    homeFragment.InfoSet(jwcTopic, alstuTopic, rssObjects);
                }
                homeFragment.lastViewSet(context);
            }
        }
        System.gc();
        super.onPostExecute(context);
    }
}
