package tool.xfy9326.naucourse.methods;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tool.xfy9326.naucourse.Config;
import tool.xfy9326.naucourse.beans.course.TodayCourses;

public class CourseListUpdate {

    public static TodayCourses readTodayCourseFromBytes(byte[] bytes) {
        TodayCourses todayCourses = null;
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream sIn;
        try {
            sIn = new ObjectInputStream(in);
            Object object = sIn.readObject();
            todayCourses = (TodayCourses) object;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return todayCourses;
    }

    static byte[] writeTodayCourseInBytes(TodayCourses todayCourses) {
        byte[] bytes = null;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream sOut;
        try {
            sOut = new ObjectOutputStream(out);
            sOut.writeObject(todayCourses);
            sOut.flush();
            bytes = out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    private static String getBestNode(@NonNull GoogleApiClient googleApiClient) {
        CapabilityApi.GetCapabilityResult result = Wearable.CapabilityApi.getCapability(googleApiClient, Config.WEAR_MSG_UPDATE_TODAY_COURSE_LIST, CapabilityApi.FILTER_REACHABLE).await();
        String bestNodeId = null;
        for (Node node : result.getCapability().getNodes()) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }

    public static void requestNewCourseData(final GoogleApiClient googleApiClient, @NonNull final onRequestCourseListUpdateListener listener) {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String nodeId = getBestNode(googleApiClient);
                    if (nodeId != null) {
                        MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(googleApiClient, nodeId, Config.WEAR_MSG_UPDATE_TODAY_COURSE_LIST, new byte[0]).await();
                        listener.onResult(nodeId, result.getRequestId() != MessageApi.UNKNOWN_REQUEST_ID);
                    } else {
                        listener.onResult(null, false);
                    }
                }
            }).start();
        } else {
            listener.onResult(null, false);
        }
    }

    public interface onRequestCourseListUpdateListener {
        void onResult(@Nullable String nodeId, boolean isSuccess);
    }
}
