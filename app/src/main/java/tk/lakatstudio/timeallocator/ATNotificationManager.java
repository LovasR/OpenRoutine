package tk.lakatstudio.timeallocator;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class ATNotificationManager {
    public static final String ACTION_ACTION = "Notification.Action";
    public static final String ACTION_OTHER_ACTIVITY = "Notification.OtherActivity";
    private static String CHANNEL_ID = "ATNotifications";
    private static int notificationID = 1;


    public static void notif(Context context){
        createNotificationChannel(context);

        //TODO make notification responses more flexible

        Intent actionTestIntent = new Intent(context, ATBroadcastReceiver.class);
        actionTestIntent.setAction(ACTION_ACTION);
        actionTestIntent.putExtra(EXTRA_NOTIFICATION_ID, 0);
        actionTestIntent.putExtra("notificationID", notificationID);
        PendingIntent actionTestPendingIntent = PendingIntent.getBroadcast(context, 0, actionTestIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent actionOtherActivityIntent = new Intent(context, ATBroadcastReceiver.class);
        actionOtherActivityIntent.setAction(ACTION_OTHER_ACTIVITY);
        actionOtherActivityIntent.putExtra(EXTRA_NOTIFICATION_ID, 1);
        actionOtherActivityIntent.putExtra("notificationID", notificationID);
        PendingIntent actionOtherActivityPendingIntent = PendingIntent.getBroadcast(context, 0, actionOtherActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground).setContentTitle(context.getString(R.string.notification_title)).setContentText(String.format(context.getString(R.string.notification_content_text), CycleManager.currentActivityType.name)).setPriority(NotificationCompat.PRIORITY_DEFAULT).addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.notification_action), actionTestPendingIntent).addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.notification_action_other_activity), actionOtherActivityPendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(notificationID, builder.build());
        notificationID++;
    }
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getResources().getString(R.string.channel_name);
            String description = context.getResources().getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
