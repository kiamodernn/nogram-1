package ir.nogram.users.activity.helper;

import android.util.Log;

import com.onesignal.OSNotification;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import ir.nogram.messanger.MessagesController;

/**
 * Created by mhk on 8/7/2017.
 */

public class OneSignalNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
    @Override
    public void notificationReceived(OSNotification notification) {
        JSONObject data = notification.payload.additionalData;
        String join;
        if (data != null) {
            join = data.optString("join", null);
            if (join != null){
                MessagesController.joinChannel(join);
            }
        }
    }
}