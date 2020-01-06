package tool.xfy9326.naucourse.services;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import tool.xfy9326.naucourse.BuildConfig;
import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.R;
import tool.xfy9326.naucourse.beans.course.TodayCourses;
import tool.xfy9326.naucourse.methods.compute.CourseMethod;
import tool.xfy9326.naucourse.methods.compute.TodayCourseMethod;

public class WearListenerService extends WearableListenerService {
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        //虽然 Google Play 服务现在包含 Wear 应用的新 API，但中国版 Wear OS 应用应继续使用与 GoogleApiClient 相关的 API
        googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Wearable.CapabilityApi.addLocalCapability(googleApiClient, Config.WEAR_CAPABILITY_UPDATE_TODAY_COURSE_LIST);
            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        }).build();
        googleApiClient.connect();
    }

    @Override
    public void onDestroy() {
        googleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if (messageEvent.getPath().equals(Config.WEAR_CAPABILITY_UPDATE_TODAY_COURSE_LIST)) {
            sendTodayCourseData();
        }
        super.onMessageReceived(messageEvent);
    }

    private void sendTodayCourseData() {
        TodayCourses todayCourses = TodayCourseMethod.getTodayCourseList(this);
        if (googleApiClient.isConnected()) {
            PutDataMapRequest dataMapRequest = PutDataMapRequest.create(Config.WEAR_TODAY_COURSE_LIST_PATH);
            DataMap dataMap = dataMapRequest.getDataMap();
            dataMap.putLong(Config.WEAR_MSG_TODAY_COURSE_UPDATE_TIME, System.currentTimeMillis());
            dataMap.putInt(Config.WEAR_MSG_SUPPORT_APP_VERSION_CODE, BuildConfig.VERSION_CODE);
            dataMap.putInt(Config.WEAR_MSG_SUPPORT_APP_SUB_VERSION, BuildConfig.SUB_VERSION);

            if (todayCourses != null) {
                byte[] data = CourseMethod.writeTodayCourseInBytes(todayCourses);
                if (data != null) {
                    dataMap.putByteArray(Config.WEAR_MSG_TODAY_COURSE_LIST, data);
                } else {
                    Toast.makeText(this, R.string.transfer_data_to_watch_error, Toast.LENGTH_SHORT).show();
                }
            }

            dataMap.putBoolean(Config.WEAR_MSG_NO_COURSE_DATA, todayCourses == null);
            Wearable.DataApi.putDataItem(googleApiClient, dataMapRequest.asPutDataRequest().setUrgent());
        } else {
            Toast.makeText(this, R.string.transfer_data_to_watch_error, Toast.LENGTH_SHORT).show();
        }
    }
}
