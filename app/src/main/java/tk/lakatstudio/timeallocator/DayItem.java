package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class DayItem implements Cloneable {
    String name;
    Date start;
    Date end;
    boolean isRunning;
    ActivityType type;

    //notifications associated with this, times are relative
    HashMap<UUID, NotificationTime> notificationTimes;

    UUID ID;

    static HashMap<UUID, DayItem> allDayItemHashes = new HashMap<>();

    DayItem(String name, Date start, Date end, ActivityType type, HashMap<UUID, NotificationTime> notificationTimes){
        this.name = name;
        this.start = start;
        this.end = end;
        this.type = type;
        this.notificationTimes = notificationTimes;
        ID = UUID.randomUUID();
        isRunning = false;
    }

    //RemoteScheduledNotification constructor
    DayItem(String name, Date start, Date end, ActivityType type, UUID ID){
        this.name = name;
        this.start = start;
        this.end = end;
        this.type = type;
        this.ID = ID;
        this.notificationTimes = null;
        isRunning = false;
    }

    /*static DayItem clone_(DayItem dayItem){
        DayItem outDayItem = new DayItem();
        Field[] fields = dayItem.getClass().getFields();
        for(int i = 0; i < fields.length; i++){
            Field field = fields[i];
            outDayItem.getClass().getFields()[i] = field;
        }
        return outDayItem;
    }*/

    static DayItem clone(DayItem dayItem){
        DayItem outDayItem = new DayItem();
        outDayItem.name = dayItem.name;
        outDayItem.start = dayItem.start;
        outDayItem.end = dayItem.end;
        outDayItem.type = dayItem.type;
        outDayItem.ID = dayItem.ID;
        outDayItem.notificationTimes = (HashMap<UUID, NotificationTime>) dayItem.notificationTimes.clone();
        outDayItem.isRunning = false;
        return outDayItem;
    }

    DayItem(){
        ID = UUID.randomUUID();
    }

    static class NotificationTime{
        int offset;
        boolean fromEnd;
        int requestID;
        UUID ID;
        NotificationTime(int offset, boolean fromEnd, int requestID){
            this.offset = offset;
            this.fromEnd = fromEnd;
            this.requestID = requestID;
            this.ID = UUID.randomUUID();
        }
    } 

    void nullCheck(){
        if(ID == null){
            this.ID = UUID.randomUUID();
        }
        if(notificationTimes == null){
            this.notificationTimes = new HashMap<>();
        }
    }

    void removeNotifications(Context context, Day day, boolean isRegimeDay){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(day.start.getTime());
        Log.v("notificationSet_Del", "dayinde4x: " + day.dayIndex);
        for (NotificationTime notificationTime : new ArrayList<>(notificationTimes.values())){
            Log.v("notificationSet_Del", "loop: " + notificationTime.ID);
            if (day.start.getTime() + (24 * 60 * 60 * 1000) < start.getTime() + (notificationTime.offset * 1000)
                    || day.start.getTime() > start.getTime() + (notificationTime.offset * 1000)) {
                if (!isRegimeDay) {
                    Calendar dayItemStart = (Calendar) calendar.clone();
                    dayItemStart.add(Calendar.SECOND, notificationTime.offset);
                    Date offsetDate = dayItemStart.getTime();
                    Log.v("notificationSet_Del", "RSM: " + notificationTime.ID);
                    if (dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR) == MainFragment1.todayIndex) {
                        //cancels if alarm is scheduled
                        DayInit.cancelAlarm(context, notificationTime.requestID);
                        Log.v("notificationSet_Del", "alert set, canceling: " + notificationTime.requestID);
                    }
                    Day targetDay = Day.getDay(context, offsetDate);
                    targetDay.removeRemoteScheduledNotification(notificationTime.ID, dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR));
                } else {
                    //for regime days the remotes are stored in themselves
                    Log.v("notificationSet_Del", "regime: " + notificationTime.ID);
                    day.removeRemoteScheduledNotification(notificationTime.ID, -1);
                }
            } else {
                Log.v("notificationSet_Del", "sameday: " + notificationTime.ID);
                DayInit.cancelAlarm(context, notificationTime.requestID);
            }
            notificationTimes.remove(notificationTime.ID);
        }
    }

    @NonNull
    @Override
    protected Object clone() {
        try {
            return super.clone();
        } catch (Exception e){
            return null;
        }
    }
}
