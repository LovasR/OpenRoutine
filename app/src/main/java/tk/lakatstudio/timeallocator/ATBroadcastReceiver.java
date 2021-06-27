package tk.lakatstudio.timeallocator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.AlarmManagerCompat;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static tk.lakatstudio.timeallocator.DayInit.gson;

public class ATBroadcastReceiver extends android.content.BroadcastReceiver {
    int notificationID = 8;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.v("notification", "onReceive ATBroadcastReceiver " + intent.getAction());
        System.out.println("tk.lakatstudio onReceive ATBroadcastReceiver " + intent.getAction());
        Toast.makeText(context, "Boot TA", Toast.LENGTH_SHORT).show();
        final PendingResult result = goAsync();
        Thread thread = new Thread() {
            public void run() {
                int i = 0;

                switch(intent.getAction()){
                    case ATNotificationManager.ACTION_OTHER_ACTIVITY:
                        Intent intent1 = new Intent(context, SilenceDialog.class);
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent1.putExtra("notificationID", intent.getIntExtra("notificationID", 0));
                        context.startActivity(intent1);
                        break;
                    case ATNotificationManager.CREATE_NOTIFICATION:
                        //ATNotificationManager.dayItemNotification(context, intent.getStringExtra("dayItemID"));
                        break;
                    case Intent.ACTION_BOOT_COMPLETED:
                        Day.setAllNotifications(context);
                        break;
                }

                result.setResultCode(i);
                result.finish();
            }
        };
        thread.start();
    }

    void loadDay(Context context){
        ArrayList<String> days = DayInit.readFromFile(context, "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(Calendar.getInstance().getTime()));

        if(days == null){
            return;
        }

        DayInit.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        Day currentDay;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert am != null;
        long nowMili = System.currentTimeMillis();

        DayInit.initGson();
        for(String dayJson : days){
            currentDay = gson.fromJson(dayJson, Day.class);
            for(DayItem dayItem : currentDay.dayItems.values()){
                dayItem.nullCheck();
                Log.v("uuid_debug", dayItem.ID.toString());
                if(dayItem.start.getTime() > nowMili) {
                    Log.v("notificationSet_boot", dayItem.type.name + " " + new SimpleDateFormat("dd. HH:mm").format(dayItem.start));
                    AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, dayItem.start.getTime(), makeAlarm(context, dayItem));
                }
            }
        }
    }

    PendingIntent makeAlarm(Context context, DayItem dayItem){

        Intent notificationIntent = new Intent(context, NotificationReceiver.class);

        notificationIntent.setPackage(context.getPackageName());
        notificationIntent.setAction("Notification.Create");

        notificationIntent.putExtra("dayItemID", dayItem.ID.toString());
        notificationIntent.putExtra("dayItemTypeName", dayItem.type.name);
        notificationIntent.putExtra("dayItemStart", dayItem.start.getTime());
        notificationIntent.putExtra("requestID", DayInit.notificationRequestID);

        DayInit.increaseNotificationRequestID();
        return PendingIntent.getBroadcast(context, DayInit.notificationRequestID - 1, notificationIntent, 0);
    }
}
