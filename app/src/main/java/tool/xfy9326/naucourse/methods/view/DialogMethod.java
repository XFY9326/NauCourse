package tool.xfy9326.naucourse.methods.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.IOException;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.methods.BaseMethod;
import tool.xfy9326.naucourse.methods.ImageMethod;
import tool.xfy9326.naucourse.methods.PermissionMethod;
import tool.xfy9326.naucourse.methods.io.DataMethod;
import tool.xfy9326.naucourse.methods.net.NetMethod;

public class DialogMethod {
    /**
     * 显示加载中的提示
     *
     * @param activity       Activity
     * @param cancelable     是否可以取消显示
     * @param cancelListener 对取消显示的监听
     * @return show方法返回的Dialog
     */
    public static Dialog showLoadingDialog(@NonNull Activity activity, boolean cancelable, @Nullable DialogInterface.OnCancelListener cancelListener) {
        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.dialog_loading, activity.findViewById(R.id.dialog_layout_loading));
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(cancelable);
        builder.setOnCancelListener(cancelListener);
        builder.setView(view);
        return builder.show();
    }

    public static void showEULADialog(@NonNull Context context, boolean checkAccept, final BaseMethod.OnEULAListener eulaListener) {
        if (checkAccept) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (sharedPreferences.getBoolean(Config.PREFERENCE_EULA_ACCEPT, Config.DEFAULT_PREFERENCE_EULA_ACCEPT)) {
                return;
            }
        }
        String eulaText = DataMethod.readAssetsText(context, Config.ASSETS_EULA_PATH);
        if (eulaText != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.eula);
            builder.setMessage(eulaText);
            if (checkAccept) {
                builder.setCancelable(false);
                builder.setPositiveButton(R.string.accept, (dialog, which) -> eulaListener.onAccept());
                builder.setNegativeButton(R.string.reject, (dialog, which) -> eulaListener.onReject());
            } else {
                builder.setPositiveButton(android.R.string.yes, null);
            }
            builder.show();
        } else {
            eulaListener.onReject();
        }
    }

    public static void showLicenseDialog(@NonNull Context context) {
        String eulaText = DataMethod.readAssetsText(context, Config.ASSETS_LICENSE_PATH);
        if (eulaText != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.open_source_license);
            builder.setMessage(eulaText);
            builder.setPositiveButton(android.R.string.yes, null);
            builder.show();
        }
    }

    public static void showDonateDialog(@NonNull Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.donate);
        builder.setMessage(R.string.donate_content);
        builder.setPositiveButton(R.string.alipay, (dialog, which) -> NetMethod.viewUrlInBrowser(context, Config.DONATE_URL_ALIPAY));
        builder.setNegativeButton(R.string.wechat, (dialog, which) -> NetMethod.viewUrlInBrowser(context, Config.DONATE_URL_WECHAT));
        builder.setNeutralButton(R.string.qq_wallet, (dialog, which) -> NetMethod.viewUrlInBrowser(context, Config.DONATE_URL_QQ_WALLET));
        builder.show();
    }

    public static void showImageShareDialog(@NonNull Activity activity, Bitmap bitmap, final String saveImgName, final int shareTitleId, final int shareFailedId, final int shareTextId) {
        if (PermissionMethod.checkStoragePermission(activity, 0)) {
            if (bitmap != null) {
                LayoutInflater layoutInflater = activity.getLayoutInflater();
                View view = layoutInflater.inflate(R.layout.dialog_share_image, activity.findViewById(R.id.layout_dialog_share_image));
                final PhotoView photoView = view.findViewById(R.id.photoView_share_image);
                photoView.setImageDrawable(new BitmapDrawable(activity.getResources(), bitmap));

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setView(view);
                builder.setTitle(shareTitleId);
                builder.setPositiveButton(R.string.share, (dialog, which) -> {
                    String tempPath = Config.PICTURE_TEMP_DICTIONARY_PATH + saveImgName;
                    try {
                        if (ImageMethod.saveBitmap(bitmap, tempPath, false)) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            Uri photoURI = FileProvider.getUriForFile(activity, Config.FILE_PROVIDER_AUTH, new File(tempPath));
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_STREAM, photoURI);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            BaseMethod.runIntent(activity, Intent.createChooser(intent, activity.getString(shareTextId)));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(activity, shareFailedId, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNeutralButton(R.string.save, (dialog, which) -> {
                    String savePath = Config.PICTURE_DICTIONARY_PATH + saveImgName;
                    try {
                        if (ImageMethod.saveBitmap(bitmap, savePath, false)) {
                            Toast.makeText(activity, activity.getString(R.string.save_file_success, savePath), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(activity, R.string.save_failed, Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.setOnCancelListener(dialog -> {
                    photoView.refreshDrawableState();
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(activity, R.string.data_is_loading, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, R.string.permission_error, Toast.LENGTH_SHORT).show();
        }
    }
}
