package tool.xfy9326.naucourse.Activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import lib.xfy9326.naujwc.NauJwcClient;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.Methods.BaseMethod;
import tool.xfy9326.naucourse.Methods.LoginMethod;
import tool.xfy9326.naucourse.Methods.NetMethod;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.Tools.AES;

/**
 * Created by xfy9326 on 18-2-20.
 */

public class LoginActivity extends AppCompatActivity {
    private boolean loginSuccess = false;
    private Dialog loadingDialog;
    private String loginURL;
    private int loginErrorCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginURL = null;
        ToolBarSet();
        ViewSet();
    }

    private void ToolBarSet() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    private void ViewSet() {
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        final EditText editText_userId = findViewById(R.id.editText_userId);
        String userId = sharedPreferences.getString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID);
        if (!userId.equals(Config.DEFAULT_PREFERENCE_USER_ID)) {
            editText_userId.setText(userId);
        }
        final EditText editText_userPw = findViewById(R.id.editText_userPw);
        String en_userPw = sharedPreferences.getString(Config.PREFERENCE_USER_PW, Config.DEFAULT_PREFERENCE_USER_PW);
        if (!en_userPw.equals(Config.DEFAULT_PREFERENCE_USER_PW)) {
            editText_userPw.setText(AES.decrypt(en_userPw, userId));
        }
        final CheckBox checkBox_rememberPw = findViewById(R.id.checkBox_rememberPw);
        checkBox_rememberPw.setChecked(sharedPreferences.getBoolean(Config.PREFERENCE_REMEMBER_PW, Config.DEFAULT_PREFERENCE_REMEMBER_PW));
        Button button_login = findViewById(R.id.button_login);
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (loadingDialog != null) {
                                            loadingDialog.cancel();
                                            loadingDialog = null;
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                                loginSuccess = false;
                                loginErrorCode = NauJwcClient.LOGIN_ERROR;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (loadingDialog != null) {
                                            loadingDialog.cancel();
                                            loadingDialog = null;
                                        }
                                    }
                                });
                            }
                        }
                    }).start();
                    if (checkBox_rememberPw.isChecked()) {
                        String en_pw = AES.encrypt(String.valueOf(pw), String.valueOf(id));
                        sharedPreferences.edit().putString(Config.PREFERENCE_USER_ID, id).putString(Config.PREFERENCE_USER_PW, en_pw).putBoolean(Config.PREFERENCE_REMEMBER_PW, true).apply();
                    } else {
                        sharedPreferences.edit().putString(Config.PREFERENCE_USER_ID, Config.DEFAULT_PREFERENCE_USER_ID).putString(Config.PREFERENCE_USER_PW, Config.DEFAULT_PREFERENCE_USER_PW).putBoolean(Config.PREFERENCE_REMEMBER_PW, false).apply();
                    }
                } else {
                    Snackbar.make(findViewById(R.id.layout_login_content), R.string.network_error, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showLoadingDialog(final Context context) {
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
                    if (loginErrorCode == NauJwcClient.LOGIN_ERROR) {
                        Snackbar.make(findViewById(R.id.layout_login_content), R.string.login_error, Snackbar.LENGTH_SHORT).show();
                    } else if (loginErrorCode == NauJwcClient.LOGIN_ALREADY_LOGIN) {
                        Snackbar.make(findViewById(R.id.layout_login_content), R.string.already_login_error, Snackbar.LENGTH_SHORT).show();
                    } else if (loginErrorCode == NauJwcClient.LOGIN_CHECKCODE_WRONG) {
                        Snackbar.make(findViewById(R.id.layout_login_content), R.string.checkcode_error, Snackbar.LENGTH_SHORT).show();
                    } else if (loginErrorCode == NauJwcClient.LOGIN_USER_INFO_WRONG) {
                        Snackbar.make(findViewById(R.id.layout_login_content), R.string.user_info_error, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });
        loadingDialog = builder.show();
    }

}
