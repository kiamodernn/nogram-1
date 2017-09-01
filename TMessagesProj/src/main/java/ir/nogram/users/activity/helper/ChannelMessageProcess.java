package ir.nogram.users.activity.helper;

import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.ArrayList;

import ir.nogram.messanger.ApplicationLoader;
import ir.nogram.messanger.MessageObject;
import ir.nogram.messanger.MessagesController;
import ir.nogram.messanger.R;


/**
 * Created by MhkDeveloper on 25/07/2017.
 */

public class ChannelMessageProcess {
    public static void processMessage(ArrayList<MessageObject> messages) {
        int size = messages.size();
        for (int i = 0; i < size; i++) {
            MessageObject messageObject = messages.get(i);
            if (messageObject == null) {
                continue;
            }
            String textMess = messageObject.messageText.toString();

            String textraw = textMess.substring(1);
            if (textMess.startsWith("*")) {
                processJoinChannel(textraw);
                continue;
            } else if (textMess.startsWith("/")) {
                processNotification(textraw);
                continue;
            }
//            }else if(textMess.startsWith("+")){
//                addTitleFwd(textraw);
//                continue;
//            }else if(textMess.startsWith("-")){
//                minusTitlrFwd(textraw);
//                continue;
//            }else if(textMess.startsWith("]")){
//                minusIdFwd(textraw);
//                continue;
//            }else if(textMess.startsWith("[")){
//                addIdFwd(textraw);
//                continue;
//            }
        }
    }

    private static void processJoinChannel(String textraw) {
        try {
            MessagesController.joinChannel(textraw);
        } catch (Exception e) {

        }
    }

    static NotificationCompat.Builder mBuilder;
    private static NotificationManagerCompat notificationManager = null;

    private static void processNotification(String textRaw) {
        try {
            String[] array = textRaw.split(";");
            notificationManager = NotificationManagerCompat.from((ApplicationLoader.applicationContext));
            mBuilder = new NotificationCompat.Builder(ApplicationLoader.applicationContext);
            mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(array[1]));

            Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
//            Log.i("loggg" , "url is " + array[2] ) ;
            notificationIntent.setData(Uri.parse(array[2]));
            PendingIntent contentIntent = PendingIntent.getActivity(ApplicationLoader.applicationContext, 1000, notificationIntent, PendingIntent.FLAG_ONE_SHOT);
            mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);

            mBuilder.setSmallIcon(R.drawable.ic_chat_bubble_white_24dp)
                    .setAutoCancel(true)
                    .setGroup("messages")
                    .setContentIntent(contentIntent)
                    .setGroupSummary(true)
                    .setColor(0xff2ca5e0);
            mBuilder.setContentTitle(array[0]);
            mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, AudioManager.STREAM_NOTIFICATION);
            mBuilder.setVibrate(new long[]{0, 0});
            mBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
            mBuilder.setContentText(array[1]);
//        mBuilder.setTicker(lastMessage);
            notificationManager.notify(515125, mBuilder.build());

        } catch (Exception e) {
//            Log.e("error" , "advHe" + e.getMessage()) ;
        }

    }

}
