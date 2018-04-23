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
import android.support.annotation.Nullable;
import android.util.Log;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.NetMethod;

public class WifiConnectService extends Service {
    private boolean onLogin = false;
    private BroadcastReceiver broadcastReceiver;
    private boolean setReceiver = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("WIFI_CONNECT", "Service Set");
        if (intent.hasExtra(Config.INTENT_AUTO_LOGIN)) {
            if (intent.getBooleanExtra(Config.INTENT_AUTO_LOGIN, false)) {
                if (!setReceiver) {
                    startWifiListen();
                }
            } else {
                if (broadcastReceiver != null && setReceiver) {
                    unregisterReceiver(broadcastReceiver);
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
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        if (info.isConnected()) {
                            login();
                        }
                    }
                }
            };
            registerReceiver(broadcastReceiver, intentFilter);
            setReceiver = true;
        }
    }

    synchronized private void login() {
        if (!onLogin) {
            onLogin = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(500);
                        NetMethod.loginNAUWifi(WifiConnectService.this);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    onLogin = false;
                }
            }).start();
        }
    }

    @Override
    public void onDestroy() {
        Log.d("WIFI_CONNECT", "Service Destroy");
        if (broadcastReceiver != null && setReceiver) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
            setReceiver = false;
        }
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Config.PREFERENCE_NETWORK_AUTO_LOGIN, Config.DEFAULT_PREFERENCE_NETWORK_AUTO_LOGIN)) {
            startService(new Intent(this, WifiConnectService.class).putExtra(Config.INTENT_AUTO_LOGIN, true).setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES));
        }
        super.onDestroy();
    }
}
