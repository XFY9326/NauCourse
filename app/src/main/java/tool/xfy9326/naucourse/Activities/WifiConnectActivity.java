package tool.xfy9326.naucourse.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.WifiLoginMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Services.WifiConnectService;
import tool.xfy9326.naucourse.Tools.AES;

/**
 * Created by 10696 on 2018/4/17.
 */

public class WifiConnectActivity extends AppCompatActivity {
    private static final int ACCESS_FINE_LOCATION_COMMANDS_REQUEST_CODE = 0;
    private SharedPreferences sharedPreferences;
    private boolean hasLogin;
    private boolean activityDestroyed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_connect);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        activityDestroyed = false;
        ToolBarSet();
        requestPermission();
        testNet();
    }

    @Override
    protected void onDestroy() {
        activityDestroyed = true;
        super.onDestroy();
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_COMMANDS_REQUEST_CODE);
        }
    }

    private void testNet() {
        ViewSet();
        showLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                hasLogin = false;
                if (checkNet(WifiConnectActivity.this)) {
                    hasLogin = NetMethod.pingIpAddress("114.114.114.114");
                }
                if (!activityDestroyed) {
                    WifiConnectActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (hasLogin) {
                                Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.edit_after_login_out, Snackbar.LENGTH_SHORT).show();
                            }
                            viewReset(hasLogin);
                            closeLoading();
                        }
                    });
                }
            }
        }).start();
    }

    private void viewReset(boolean hasLogin) {
        TextInputEditText editText_account = findViewById(R.id.editText_network_account);
        TextInputEditText editText_password = findViewById(R.id.editText_network_pw);
        Button button_connect = findViewById(R.id.button_network_connect);
        Spinner spinner_netProvider = findViewById(R.id.spinner_network_provider);
        if (hasLogin) {
            button_connect.setText(R.string.disconnect_network);
        } else {
            editText_account.setEnabled(true);
            editText_password.setEnabled(true);
            spinner_netProvider.setEnabled(true);
        }
    }

    private void ViewSet() {
        final TextInputEditText editText_account = findViewById(R.id.editText_network_account);
        String set_id = sharedPreferences.getString(Config.PREFERENCE_NETWORK_ACCOUNT, Config.DEFAULT_PREFERENCE_USER_ID);
        if (set_id.equals(Config.DEFAULT_PREFERENCE_USER_ID)) {
            String default_id = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
            if (!default_id.equals(Config.DEFAULT_PREFERENCE_USER_ID)) {
                editText_account.setText(default_id);
                set_id = default_id;
            }
        } else {
            editText_account.setText(set_id);
        }

        final TextInputEditText editText_password = findViewById(R.id.editText_network_pw);
        String set_pw = sharedPreferences.getString(Config.PREFERENCE_NETWORK_PASSWORD, Config.DEFAULT_PREFERENCE_USER_PW);
        if (set_pw.equals(Config.DEFAULT_PREFERENCE_USER_PW)) {
            String default_pw = sharedPreferences.getString(Config.PREFERENCE_USER_PW, Config.DEFAULT_PREFERENCE_USER_PW);
            if (!default_pw.equals(Config.DEFAULT_PREFERENCE_USER_PW) && !set_id.equals(Config.DEFAULT_PREFERENCE_USER_ID)) {
                editText_password.setText(AES.decrypt(default_pw, set_id));
            }
        } else if (!set_id.equals(Config.DEFAULT_PREFERENCE_USER_ID)) {
            editText_password.setText(AES.decrypt(set_pw, set_id));
        }

        final CheckBox checkBox_rememberPw = findViewById(R.id.checkBox_network_rememberPw);
        checkBox_rememberPw.setChecked(sharedPreferences.getBoolean(Config.PREFERENCE_NETWORK_REMEMBER_PASSWORD, Config.DEFAULT_PREFERENCE_NETWORK_REMEMBER_PASSWORD));
        checkBox_rememberPw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (hasLogin) {
                    checkBox_rememberPw.setChecked(!isChecked);
                    Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.edit_after_login_out, Snackbar.LENGTH_SHORT).show();
                } else {
                    if (!isChecked) {
                        sharedPreferences.edit().remove(Config.PREFERENCE_NETWORK_PASSWORD).apply();
                    }
                    sharedPreferences.edit().putBoolean(Config.PREFERENCE_NETWORK_REMEMBER_PASSWORD, isChecked).apply();
                }
            }
        });

        final CheckBox checkBox_autoLogin = findViewById(R.id.checkBox_network_autoLogin);
        checkBox_autoLogin.setChecked(sharedPreferences.getBoolean(Config.PREFERENCE_NETWORK_AUTO_LOGIN, Config.DEFAULT_PREFERENCE_NETWORK_AUTO_LOGIN));
        checkBox_autoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (hasLogin) {
                    checkBox_autoLogin.setChecked(!isChecked);
                    Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.edit_after_login_out, Snackbar.LENGTH_SHORT).show();
                } else {
                    if (isChecked) {
                        String id = editText_account.getText().toString();
                        String pw = editText_password.getText().toString();
                        if (!id.isEmpty() && !pw.isEmpty()) {
                            checkBox_rememberPw.setChecked(true);
                            sharedPreferences.edit().putString(Config.PREFERENCE_NETWORK_ACCOUNT, id).apply();
                            sharedPreferences.edit().putString(Config.PREFERENCE_NETWORK_PASSWORD, AES.encrypt(pw, id)).apply();
                            Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.ask_lock_background, Snackbar.LENGTH_SHORT).show();
                            getApplicationContext().startService(new Intent(WifiConnectActivity.this, WifiConnectService.class).putExtra(Config.INTENT_AUTO_LOGIN, true));
                        } else {
                            checkBox_autoLogin.setChecked(false);
                            Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.i_nau_home_settings_error, Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        getApplicationContext().startService(new Intent(WifiConnectActivity.this, WifiConnectService.class).putExtra(Config.INTENT_AUTO_LOGIN, false));
                    }
                    sharedPreferences.edit().putBoolean(Config.PREFERENCE_NETWORK_AUTO_LOGIN, isChecked).apply();
                }
            }
        });

        final Spinner spinner_netProvider = findViewById(R.id.spinner_network_provider);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.net_provider));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_netProvider.setAdapter(adapter);
        spinner_netProvider.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sharedPreferences.edit().putInt(Config.PREFERENCE_NETWORK_PROVIDER, position).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        spinner_netProvider.setSelection(sharedPreferences.getInt(Config.PREFERENCE_NETWORK_PROVIDER, Config.DEFAULT_PREFERENCE_NETWORK_PROVIDER));

        editText_account.setEnabled(false);
        editText_password.setEnabled(false);
        spinner_netProvider.setEnabled(false);

        final Button button_connect = findViewById(R.id.button_network_connect);
        button_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();

                final String id = editText_account.getText().toString();
                final String pw = editText_password.getText().toString();
                final int provider = spinner_netProvider.getSelectedItemPosition();

                if (id.isEmpty() || pw.isEmpty() || provider < 0) {
                    closeLoading();
                    Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.i_nau_home_settings_error, Snackbar.LENGTH_SHORT).show();
                } else {
                    if (checkNet(WifiConnectActivity.this)) {
                        WifiLoginMethod wifiLoginMethod = new WifiLoginMethod(NetMethod.getLocalIP(WifiConnectActivity.this));
                        wifiLoginMethod.setOnRequestListener(new WifiLoginMethod.OnRequestListener() {
                            @Override
                            public void OnRequest(boolean isSuccess, int errorType) {
                                WifiConnectActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        closeLoading();
                                    }
                                });
                                if (isSuccess && errorType == WifiLoginMethod.ERROR_TYPE_SUCCESS) {
                                    if (hasLogin) {
                                        Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.login_out_success, Snackbar.LENGTH_SHORT).show();
                                        sharedPreferences.edit().putString(Config.PREFERENCE_NETWORK_ACCOUNT, id).apply();
                                        if (checkBox_rememberPw.isChecked()) {
                                            sharedPreferences.edit().putString(Config.PREFERENCE_NETWORK_PASSWORD, AES.encrypt(pw, id)).apply();
                                        }
                                        hasLogin = false;
                                        WifiConnectActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                editText_account.setEnabled(true);
                                                editText_password.setEnabled(true);
                                                spinner_netProvider.setEnabled(true);
                                                button_connect.setText(R.string.connect_network);
                                            }
                                        });
                                    } else {
                                        Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.login_success, Snackbar.LENGTH_SHORT).show();
                                        sharedPreferences.edit().putString(Config.PREFERENCE_NETWORK_ACCOUNT, id).apply();
                                        if (checkBox_rememberPw.isChecked()) {
                                            sharedPreferences.edit().putString(Config.PREFERENCE_NETWORK_PASSWORD, AES.encrypt(pw, id)).apply();
                                        }
                                        hasLogin = true;
                                        WifiConnectActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                editText_account.setEnabled(false);
                                                editText_password.setEnabled(false);
                                                spinner_netProvider.setEnabled(false);
                                                button_connect.setText(R.string.disconnect_network);
                                            }
                                        });
                                    }
                                } else {
                                    if (errorType == WifiLoginMethod.ERROR_TYPE_LOGIN_ERROR) {
                                        Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.network_connect_error_login, Snackbar.LENGTH_SHORT).show();
                                    } else if (errorType == WifiLoginMethod.ERROR_TYPE_REQUEST_ERROR) {
                                        Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.network_connect_error_server, Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                        if (hasLogin) {
                            wifiLoginMethod.loginOut(id, pw);
                        } else {
                            String type = null;
                            switch (provider) {
                                case 0:
                                    type = WifiLoginMethod.TYPE_TELECOM;
                                    break;
                                case 1:
                                    type = WifiLoginMethod.TYPE_CMCC;
                                    break;
                                case 2:
                                    type = WifiLoginMethod.TYPE_UNICOM;
                                    break;
                                case 3:
                                    type = WifiLoginMethod.TYPE_SCHOOL;
                                    break;
                            }
                            if (type != null) {
                                wifiLoginMethod.login(id, pw, type);
                            } else {
                                Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.i_nau_home_settings_error, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        closeLoading();
                    }
                }
            }
        });
    }

    private void showLoading() {
        Button button = findViewById(R.id.button_network_connect);
        LinearLayout linearLayout = findViewById(R.id.layout_network_loading);
        button.setVisibility(View.GONE);
        linearLayout.setVisibility(View.VISIBLE);
    }

    private void closeLoading() {
        LinearLayout linearLayout = findViewById(R.id.layout_network_loading);
        Button button = findViewById(R.id.button_network_connect);
        linearLayout.setVisibility(View.GONE);
        button.setVisibility(View.VISIBLE);
    }

    private boolean checkNet(@NonNull Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (NetMethod.isNetworkConnected(context) && NetMethod.isWifiNetWork(context)) {
                if (NetMethod.checkNauWifiSSID(context)) {
                    if (NetMethod.pingIpAddress("172.26.3.20")) {
                        return true;
                    } else {
                        Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.network_connect_check_error_server, Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.network_connect_check_error_ssid, Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.network_connect_check_error_ssid, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(WifiConnectActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_COMMANDS_REQUEST_CODE);
            Snackbar.make(findViewById(R.id.layout_wifi_connect), R.string.permission_error, Snackbar.LENGTH_SHORT).show();
        }
        return false;
    }

}
