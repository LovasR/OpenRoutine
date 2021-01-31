package tk.lakatstudio.timeallocator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Date;

public class CycleManager {
    final static String ACTION_CHECK = "Notification.Check";

    static Cycle currentCycle;
    static DayItem currentItem;
    static Day currentDay;

    static int cycleTime;

    static int accurateCycles;
    static int maxAccurateCycles;       //Set when current day is initialized

    static ActivityType currentActivityType;

    public static ActivityType getCurrentActivity(){
        return currentActivityType;
    }
    public static Cycle getCurrentCycle(){
        Date now = Calendar.getInstance().getTime();
        //no use, could be used for error handling
        return currentCycle;
    }

    void setAlarm(Context context){
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent cycleCheckIntent = new Intent(context, ATBroadcastReceiver.class);
        cycleCheckIntent.setAction(ACTION_CHECK);
        PendingIntent cycleCheckPIntent = PendingIntent.getBroadcast(context, 0, cycleCheckIntent, 0);

        //TODO finalize numbers
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 1000, cycleTime * 1000 * 60, cycleCheckPIntent);
    }

    static void cycleCheck(Context context){
        Date now = Calendar.getInstance().getTime();
        for (int i = currentDay.cycleIndex; i < currentDay.cycles.size(); i++){
            Cycle cycle = currentDay.cycles.get(i);
            if(now.getTime() < currentDay.start.getTime() + (cycle.index * cycleTime + cycleTime)){
                currentCycle = cycle;           //refresh current cycle
                currentItem = cycle.dayItem;    //set planned activity
                ATNotificationManager.notif(context);       //check by user input
            }
        }
    }

    static void cycleChecked(){
        currentCycle.hasChecked = true;
        accurateCycles++;
    }
}
