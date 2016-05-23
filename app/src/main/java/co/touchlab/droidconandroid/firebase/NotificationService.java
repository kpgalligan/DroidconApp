package co.touchlab.droidconandroid.firebase;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by kgalligan on 5/22/16.
 */
public class NotificationService extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        Log.e("asdf", "qwert");
        final RemoteMessage.Notification notification = remoteMessage.getNotification();

    }
}
