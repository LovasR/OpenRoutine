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
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

public class ATNotificationManager {
    public static final String ACTION_ACTION = "Notification.Action";
    public static final String ACTION_OTHER_ACTIVITY = "Notification.OtherActivity";
    public static final String CREATE_NOTIFICATION = "Notification.Create";
    private static String CHANNEL_ID = "ATNotifications";
    private static int notificationID = 1;


    public static void notif(Context context){

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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.notification_title)).setContentText(String.format(context.getString(R.string.notification_content_text), "kakas"))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                /*.addAction(R.drawable.ic_launcher_foreground, context.getString(R.string.notification_action), actionOtherActivityPendingIntent)*/;

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(notificationID, builder.build());
        notificationID++;
    }

    public static  void initNotification(Context context, int dayIndex, DayItem dayItem){
        long currentTime = Calendar.getInstance().getTime().getTime();
        if(currentTime > dayItem.start.getTime()){
            //checks notification time
            Log.e("notification", "NOTIFICATION ERROR, NEGATIVE TIME");
            return;
        }

        Intent intent = new Intent(context, ATBroadcastReceiver.class);
        intent.setAction(CREATE_NOTIFICATION);
        intent.putExtra("dayIndex", dayIndex);
        intent.putExtra("dayItemID", dayItem.ID.toString());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        /*AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(Build.VERSION.SDK_INT > 22){
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, dayItem.start.getTime(), pendingIntent);
        }else {
            AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, dayItem.start.getTime(), pendingIntent);
        }*/
    }

    public static void dayItemNotification(Context context, String dayItemID, String typeName, long start, int requestID){

        Log.v("uuid_debug", UUID.fromString(dayItemID).toString());
        DayInit.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(typeName)
                .setContentText(String.format(context.getString(R.string.notification_show_before), typeName, new SimpleDateFormat(DayInit.getHourFormat(context), Locale.getDefault()).format(start)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        notificationManager.notify(requestID, builder.build());
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getResources().getString(R.string.channel_name);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
