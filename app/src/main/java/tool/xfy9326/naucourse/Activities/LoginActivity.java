package tool.xfy9326.naucourse.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import lib.xfy9326.naujwc.NauJwcClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.Methods.SecurityMethod;
import tool.xfy9326.naucourse.Methods.UpdateMethod;
import tool.xfy9326.naucourse.R;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class LoginActivity extends AppCompatActivity {
    private boolean loginSuccess = false;
    @Nullable
    private Dialog loadingDialog;
    @Nullable
    private String loginURL;
    private int loginErrorCode;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginURL = null;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        ToolBarSet();
        ViewSet();
        netCheck();
        updateCheck();
    }

    @Override
    protected void onDestroy() {
        loadingDialog = null;
        super.onDestroy();
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    private void ViewSet() {
        final EditText editText_userId = findViewById(R.id.editText_userId);
        String userId = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        if (!userId.equals(Config.DEFAULT_PREFERENCE_USER_ID)) {
            editText_userId.setText(userId);
        }
        final EditText editText_userPw = findViewById(R.id.editText_userPw);
        String en_userPw = sharedPreferences.getString(Config.PREFERENCE_USER_PW, Config.DEFAULT_PREFERENCE_USER_PW);
        if (!en_userPw.equals(Config.DEFAULT_PREFERENCE_USER_PW)) {
            editText_userPw.setText(SecurityMethod.getUserPassWord(this));
        }
        final CheckBox checkBox_rememberPw = findViewById(R.id.checkBox_rememberPw);
        checkBox_rememberPw.setChecked(sharedPreferences.getBoolean(Config.PREFERENCE_REMEMBER_PW, Config.DEFAULT_PREFERENCE_REMEMBER_PW));
        Button button_login = findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText_userId.clearFocus();
                editText_userPw.clearFocus();
                if (NetMethod.isNetworkConnected(LoginActivity.this)) {
                    final String id = editText_userId.getText().toString().trim();
                    final String pw = editText_userPw.getText().toString().trim();
                    final NauJwcClient nauJwcClient = BaseMethod.getApp(LoginActivity.this).getClient();
                    showLoadingDialog(LoginActivity.this);
                    LoginMethod.cleanUserTemp(LoginActivity.this);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (nauJwcClient.login(id, pw)) {
                                    loginURL = nauJwcClient.getLoginUrl();
                                    if (loginURL != null) {
                                        loginSuccess = true;
                                    }
                                }
                                loginErrorCode = nauJwcClient.getLoginErrorCode();
                                if (loginErrorCode == NauJwcClient.LOGIN_ALREADY_LOGIN) {
                                    LoginMethod.loginOut(LoginActivity.this);
                                    Thread.sleep(1000);
                                    if (nauJwcClient.login(id, pw)) {
                                        loginURL = nauJwcClient.getLoginUrl();
                                        if (loginURL != null) {
                                            loginSuccess = true;
                                        }
                                    }
                                    loginErrorCode = nauJwcClient.getLoginErrorCode();
                                }
                                if (!LoginActivity.this.isDestroyed() && !LoginActivity.this.isFinishing()) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (loadingDialog != null && loadingDialog.isShowing()) {
                                                loadingDialog.cancel();
                                                loadingDialog = null;
                                            }
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                loginSuccess = false;
                                loginErrorCode = NauJwcClient.LOGIN_ERROR;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (loadingDialog != null && loadingDialog.isShowing()) {
                                            loadingDialog.cancel();
                                            loadingDialog = null;
                                        }
                                    }
                                });
                            }
                        }
                    }).start();
                    if (checkBox_rememberPw.isChecked()) {
                        SecurityMethod.saveUserInfo(LoginActivity.this, String.valueOf(id), String.valueOf(pw));
                        sharedPreferences.edit().putBoolean(Config.PREFERENCE_REMEMBER_PW, true).apply();
                    } else {
                        sharedPreferences.edit().putString(Config.PREFERENCE_USER_ID, String.valueOf(id)).putString(Config.PREFERENCE_USER_PW, Config.DEFAULT_PREFERENCE_USER_PW).putBoolean(Config.PREFERENCE_REMEMBER_PW, false).apply();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.layout_login_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLoadingDialog(@NonNull final Context context) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_loading, (ViewGroup) findViewById(R.id.dialog_layout_loading));
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setView(view);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                System.gc();
                if (loginSuccess) {
                    PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(Config.PREFERENCE_HAS_LOGIN, true).putString(Config.PREFERENCE_LOGIN_URL, loginURL).apply();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra(Config.INTENT_JUST_LOGIN, true));
                    finish();
                } else {
                    switch (loginErrorCode) {
                        case NauJwcClient.LOGIN_ERROR:
                            Snackbar.make(findViewById(R.id.layout_login_content), R.string.login_error, Snackbar.LENGTH_SHORT).show();
                            break;
                        case NauJwcClient.LOGIN_ALREADY_LOGIN:
                            Snackbar.make(findViewById(R.id.layout_login_content), R.string.already_login_error, Snackbar.LENGTH_SHORT).show();
                            break;
                        case NauJwcClient.LOGIN_USER_INFO_WRONG:
                            Snackbar.make(findViewById(R.id.layout_login_content), R.string.user_info_error, Snackbar.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        });
        loadingDialog = builder.show();
    }

    private void netCheck() {
        NetMethod.isJwcAvailable(new NetMethod.OnAvailableListener() {
            @Override
            public void OnError() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(LoginActivity.this, R.string.jwc_net_no_connection, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    synchronized private void updateCheck() {
        if (NetMethod.isNetworkConnected(this)) {
            UpdateMethod.checkUpdate(this, false);
        }
    }

}
