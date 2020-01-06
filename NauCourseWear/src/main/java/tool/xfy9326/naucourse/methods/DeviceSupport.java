package tool.xfy9326.naucourse.methods;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.wearable.phone.PhoneDeviceType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.wearable.intent.RemoteIntent;

import java.util.concurrent.TimeUnit;

import tool.xfy9326.naucourse.Config;

public class DeviceSupport {
    public static void checkDeviceSupport(@NonNull final Context context, @NonNull final GoogleApiClient googleApiClient, @NonNull final onCheckAppSupportListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodesResult = Wearable.NodeApi.getConnectedNodes(googleApiClient).await(3, TimeUnit.SECONDS);
                CapabilityApi.GetCapabilityResult capabilityResult = Wearable.CapabilityApi.getCapability(googleApiClient, Config.WEAR_CAPABILITY_UPDATE_TODAY_COURSE_LIST, CapabilityApi.FILTER_REACHABLE).await(3, TimeUnit.SECONDS);

                if (nodesResult.getNodes().size() > 0) {
                    String bestNodeId = null;
                    for (Node node : nodesResult.getNodes()) {
                        if (node.isNearby()) {
                            bestNodeId = node.getId();
                            break;
                        }
                        bestNodeId = node.getId();
                    }

                    if (capabilityResult.getCapability().getNodes().size() == 0) {
                        if (PhoneDeviceType.DEVICE_TYPE_ANDROID == PhoneDeviceType.getPhoneDeviceType(context)) {
                            listener.onChecked(true, true, false, bestNodeId);
                        } else {
                            listener.onChecked(true, false, false, bestNodeId);
                        }
                    } else {
                        listener.onChecked(true, true, true, bestNodeId);

                    }
                } else {
                    listener.onChecked(false, false, false, null);
                }
            }
        }).start();
    }

    static void runInstallWebsite(Context context, @Nullable String nodeId) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(Config.APP_DOWNLOAD_URL));

        RemoteIntent.startRemoteActivity(context, intent, null, nodeId);
    }

    public interface onCheckAppSupportListener {
        void onChecked(boolean hasConnectedDevice, boolean isSupportSystem, boolean hasSupportApp, String nodeId);
    }
}
