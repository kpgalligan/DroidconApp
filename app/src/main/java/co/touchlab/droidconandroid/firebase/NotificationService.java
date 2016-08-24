package co.touchlab.droidconandroid.firebase;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import co.touchlab.droidconandroid.EventDetailActivity;
import co.touchlab.droidconandroid.R;
import co.touchlab.droidconandroid.ScheduleActivity;
import co.touchlab.droidconandroid.data.DatabaseHelper;
import co.touchlab.droidconandroid.data.Event;
import co.touchlab.droidconandroid.presenter.AppManager;
import co.touchlab.droidconandroid.tasks.persisted.RefreshScheduleData;

/**
 * Created by kgalligan on 5/22/16.
 */
public class NotificationService extends FirebaseMessagingService
{
    private static final String TAG = "NotificationService";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        try
        {
            Map<String, String> data = remoteMessage.getData();
            String type = data.get("type");
            Log.d(TAG, "Received firebase message: " + type);

            switch(type)
            {
                case "updateSchedule":
                    RefreshScheduleData.callMe(getApplicationContext());
                    break;
                case "event":

                    String message = data.get("message");
                    long eventId = Long.parseLong(data.get("eventId"));
                    Event event = DatabaseHelper.getInstance(this)
                            .getEventDao()
                            .queryForId(eventId);
                    if(event != null)
                    {
                        sendEventNotification(data.get("title"), message, eventId, event.category);
                    }
                    break;
                case "version":
                    PackageManager manager = getPackageManager();
                    String name = getPackageName();
                    PackageInfo pInfo = manager.getPackageInfo(name, 0);

                    int versionCode = pInfo.versionCode;
                    int checkCode = Integer.parseInt(data.get("versionCode"));
                    if(versionCode < checkCode)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + name));

                        if(intent.resolveActivity(manager) == null)
                        {
                            intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=" +
                                            name));
                        }

                        sendIntentNotification(getString(R.string.app_name),
                                "Please update your app",
                                intent);
                    }
                    break;
            }

            // Check if message contains a notification payload.
            final RemoteMessage.Notification notification = remoteMessage.getNotification();
            if(notification != null)
            {
                sendNotification(notification);
            }
        }
        catch(Exception e)
        {
            Log.e(TAG, "onMessageReceived: ",e );
            AppManager.getPlatformClient().logException(e);
        }
    }

    private void sendNotification(RemoteMessage.Notification notification) {
        Intent intent = new Intent(this, ScheduleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String title = TextUtils.isEmpty(notification.getTitle()) ? "Droidcon" : notification.getTitle();

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendEventNotification(String title, String message, long eventId, String category)
    {
        Intent intent = EventDetailActivity.Companion.createIntent(this, category, eventId);
        sendIntentNotification(title, message, intent);
    }

    private void sendIntentNotification(String title, String message, Intent intent)
    {
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}
