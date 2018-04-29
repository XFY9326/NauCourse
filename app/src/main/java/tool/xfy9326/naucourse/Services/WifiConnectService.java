package tool.xfy9326.naucourse.Services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Handlers.MainHandler;

public class WifiConnectService extends Service {
    private int connectTime = 0;
    @Nullable
    private BroadcastReceiver broadcastReceiver;
    private boolean setReceiver = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@NonNull Intent intent, int flags, int startId) {
        if (intent.hasExtra(Config.INTENT_AUTO_LOGIN)) {
            if (intent.getBooleanExtra(Config.INTENT_AUTO_LOGIN, false)) {
                if (!setReceiver) {
                    startWifiListen();
                }
            } else {
                if (broadcastReceiver != null && setReceiver) {
                    unregisterReceiver(broadcastReceiver);
                    connectTime = 0;
                    broadcastReceiver = null;
                    setReceiver = false;
                }
            }
        }
        return START_REDELIVER_INTENT;
    }

    private void startWifiListen() {
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Config.PREFERENCE_NETWORK_AUTO_LOGIN, Config.DEFAULT_PREFERENCE_NETWORK_AUTO_LOGIN)) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, @NonNull Intent intent) {
                    String action = intent.getAction();
                    if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        if (info.isConnected()) {
                            connectTime++;
                            if (connectTime >= 2) {
                                MainHandler mainHandler = new MainHandler(WifiConnectService.this);
                                mainHandler.sendEmptyMessageDelayed(Config.HANDLER_AUTO_LOGIN_WIFI, 500);
                                connectTime = 0;
                            }
                        }
                    }
                }
            };
            registerReceiver(broadcastReceiver, intentFilter);
            connectTime = 0;
            setReceiver = true;
        }
    }

    @Override
    public void onDestroy() {
        if (broadcastReceiver != null && setReceiver) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
            connectTime = 0;
            setReceiver = false;
        }
        super.onDestroy();
    }
}
