package tk.lakatstudio.timeallocator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.AlarmManagerCompat;
import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import tk.lakatstudio.timeallocator.DayInit.Exclude;

public class Day {
    Date start;         //Start of day to serve as base line for cycles
    int dayIndex;

    HashMap<UUID, DayItem> dayItems = new HashMap<>();
    //dayItems must be sorted when used in list

    HashMap<UUID, TodoItem> todoItems = new HashMap<>();

    //used to identify dayItems from regimes
    //TODO make the regime added dayItems savable as hashes
    @Exclude
    HashMap<UUID, DayItem> regimeDayItems = new HashMap<>();
    @Exclude
    HashMap<UUID, TodoItem> regimeTodoItems = new HashMap<>();
    @Exclude
    ArrayList<UUID> setRegimes = new ArrayList<>();

    HashMap<UUID, RemoteScheduledNotification> remoteScheduledNotifications = new HashMap<>();

    class RemoteScheduledNotification extends DayItem{
        long notificationTime;
        UUID notificationID;
        boolean isSet;
        RemoteScheduledNotification(DayItem dayItem, long notificationTime, UUID notificationID) {
            //remoteScheduledNotifications don`t contain notificationTimes
            super(dayItem.name, dayItem.start, dayItem.end, dayItem.type, dayItem.ID);
            this.notificationTime = notificationTime;
            this.notificationID = notificationID;
            this.isSet = false;
        }
    }

    boolean isSaved;
    //for when regime`s dayItems are set
    boolean isRegimeSet = false;
    ArrayList<UUID> regimesSet;
    boolean isRegimesSet(){
        for(Regime regime : Regime.allRegimes.values()){
            if(regime.isActive(start.getTime()) && !regimesSet.contains(regime)){
                return false;
            }
        }
        return true;
    }

    Day(){};

    //constructor for regimes
    Day(boolean isRegimeDay, int dayOfWeek){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.DAY_OF_WEEK, (dayOfWeek == 6 ? Calendar.SUNDAY : dayOfWeek + 2));
        this.start = calendar.getTime();
        //calendar.setTimeInMillis(this.start.getTime());
        this.dayIndex = calendar.get(Calendar.YEAR) * 366 + calendar.get(Calendar.DAY_OF_YEAR);
    };

    DayItem getDayItem(Date time){
        for(int i = 0; i < dayItems.size(); i++){
            if(dayItems.get(i).start.getTime() < time.getTime() && dayItems.get(i).end.getTime() > time.getTime()){
                //check current dayItem type
                return dayItems.get(i);
            }
        }
        return null;
    }

    void addDayItem(DayItem newDayItem){
        dayItems.put(newDayItem.ID, newDayItem);
        isSaved = false;
    }

    static ArrayList<DayItem> defaultTimeSortDayItems(ArrayList<DayItem> dayItems){
        Collections.sort(dayItems, new Comparator<DayItem>() {
            @Override
            public int compare(DayItem item1, DayItem item2) {
                return Long.compare(item1.start.getTime(), item2.start.getTime());
            }
        });
        return dayItems;
    }

    //Day dayFrom

    void removeDayItem(final Context context, final UUID index, final boolean isRegimeDay){
        Log.v("Day_list", "Item removed size before: " + dayItems.size() + " @: " + index);
        final Day day = this;
        final DayItem dayItem = dayItems.get(index);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(dayItem != null){
                    dayItem.removeNotifications(context, day, isRegimeDay);
                }
            }
        }).start();
        regimeDayItems.remove(index);
        dayItems.remove(index);
        isSaved = false;
        Log.v("Day_list", "Item removed size now: " + dayItems.size());
    }

    void addTodoItem(TodoItem todoItem){
        todoItems.put(todoItem.ID, todoItem);
        isSaved = false;
    }
    void removeTodoItem(TodoItem todoItem){
        todoItems.remove(todoItem.ID);
        isSaved = false;
    }

    void nullCheck(){
        for(DayItem dayItem : dayItems.values()){
            dayItem.nullCheck();
        }
        if(dayIndex == 0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(this.start.getTime());
            dayIndex = calendar.get(Calendar.YEAR) * 366 + calendar.get(Calendar.DAY_OF_YEAR);
        }

    }

    void addRegimeDays(Context context, Regime regime){
        //TODO make it compatible custom length regime
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        //corrects for java weeks sunday
        int dayOfWeek = startCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6 : startCalendar.get(Calendar.DAY_OF_WEEK) - 2;

        boolean isToday = (dayIndex == MainFragment1.todayIndex);

        Log.v("set_RMS_add", "dayIndex: " + dayIndex + " todayIndex: " + MainFragment1.todayIndex);

        //dayItems.putAll(regime.days[dayOfWeek].dayItems);
        Calendar correctionCalendar = (Calendar) startCalendar.clone();
        correctionCalendar.setTimeInMillis(start.getTime());

        Calendar dayItemCalendar = (Calendar) correctionCalendar.clone();
        for(DayItem regimeDayItem : regime.days[dayOfWeek].dayItems.values()){
            DayItem dayItem = DayItem.clone(regimeDayItem);

            //start time correction
            dayItemCalendar.setTimeInMillis(dayItem.start.getTime());
            correctionCalendar.set(Calendar.HOUR_OF_DAY, dayItemCalendar.get(Calendar.HOUR_OF_DAY));
            correctionCalendar.set(Calendar.MINUTE, dayItemCalendar.get(Calendar.MINUTE));
            dayItem.start = correctionCalendar.getTime();


            //end time correction
            dayItemCalendar.setTimeInMillis(dayItem.end.getTime());
            correctionCalendar.set(Calendar.HOUR_OF_DAY, dayItemCalendar.get(Calendar.HOUR_OF_DAY));
            correctionCalendar.set(Calendar.MINUTE, dayItemCalendar.get(Calendar.MINUTE));
            dayItem.end = correctionCalendar.getTime();

            dayItems.put(dayItem.ID, dayItem);
            regimeDayItems.put(dayItem.ID, dayItem);

            Log.v("regime_notif", "dayItem start: " + dayItem.start.getTime());
            if(isToday) {
                for (DayItem.NotificationTime notificationTime : dayItem.notificationTimes.values()) {
                    Log.v("regime_notif", "offset: " + notificationTime.offset);
                    if(dayItem.start.getTime() + (notificationTime.offset * 1000) > System.currentTimeMillis())
                    setRegimeNotifications(context, regime.days[dayOfWeek], dayItem, notificationTime);
                }
            }
        }


        //correction is needed because remoteScheduledNotification's start time only contains the offset from the Epoch

        final int RMS_CORRECTION = 86400;

        if(isToday) {
            long nowMili = System.currentTimeMillis();
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Log.v("regime_RMS_add_", "it is Today: " + dayOfWeek + " " + regime.days[dayOfWeek].remoteScheduledNotifications.values().size() + " @: " + regime.days[dayOfWeek].toString());


            for (RemoteScheduledNotification remoteScheduledNotification : regime.days[dayOfWeek].remoteScheduledNotifications.values()) {
                //in this instance (regime days) start time is from zero
                long alarmTime = 0;
                alarmTime += (remoteScheduledNotification.notificationTime) * 1000;
                Log.v("regime_RMS_add_", "alarmTime1 : " + alarmTime);
                alarmTime += this.start.getTime();
                Log.v("regime_RMS_add_", "alarmTime2 : " + alarmTime);
                alarmTime += startCalendar.get(Calendar.ZONE_OFFSET);
                Log.v("set_RMS", "alarmTime3 : " + alarmTime + " timezone: " + Calendar.getInstance().get(Calendar.ZONE_OFFSET));

                if (alarmTime > nowMili) {
                    Log.v("regime_RMS_add_", "set.");
                    AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, alarmTime, makeAlarm(context, remoteScheduledNotification));
                } else {
                    Log.v("regime_RMS_add_", "setn't.");
                }

            }
        } else {
            Log.v("regime_RMS_add_", "it is not Today");
        }

        for(TodoItem todoItem : regime.days[dayOfWeek].todoItems.values()) {
            todoItems.put(todoItem.ID, todoItem);
            regimeTodoItems.put(todoItem.ID, todoItem);
        }

        isRegimeSet = true;
    }

    void setRegimeNotifications(Context context, Day day, DayItem dayItem, DayItem.NotificationTime notificationTime){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent notificationIntent = new Intent(context, NotificationReceiver.class);

        notificationIntent.setPackage(context.getPackageName());
        notificationIntent.setAction("Notification.Create");
        notificationIntent.putExtra("dayItemID", dayItem.ID.toString());
        notificationIntent.putExtra("dayItemTypeName", dayItem.type.name);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dayItem.start.getTime());

        if(day.start.getTime() + (24 * 60 * 60 * 1000) < dayItem.start.getTime() + (notificationTime.offset * 1000)
                || day.start.getTime() > dayItem.start.getTime() + (notificationTime.offset * 1000)){
            Calendar dayItemStart = (Calendar) calendar.clone();
            dayItemStart.add(Calendar.SECOND, notificationTime.offset);
            Date offsetDate = dayItemStart.getTime();
            Log.v("notificationSet", "remote offsetDAte: " + new SimpleDateFormat("d. HH:mm").format(offsetDate));
            Log.v("notificationSet", "notification time: " + new SimpleDateFormat("M.d. HH:mm").format(new Date(dayItem.start.getTime() + notificationTime.offset * 1000)));
            if(dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR) == MainFragment1.todayIndex) {
                notificationSetAlarm(context, notificationIntent, am, dayItem, notificationTime);
            }
            Day targetDay = Day.getDay(context, offsetDate);
            targetDay.addRemoteScheduledNotification(dayItem, notificationTime.offset, dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR), notificationTime.ID);
        } else {
            notificationSetAlarm(context, notificationIntent, am, dayItem, notificationTime);
        }
    }

    void addRegimeNotifications(Context context, Regime regime){
        boolean isToday = (dayIndex == MainFragment1.todayIndex);
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        int dayOfWeek = startCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6 : startCalendar.get(Calendar.DAY_OF_WEEK) - 2;

        Log.v("regime_RMS_add", "add_attempt");

        if(isToday) {
            long nowMili = System.currentTimeMillis();
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Log.v("regime_RMS_add", "it is Today: " + dayOfWeek + " " + regime.days[dayOfWeek].remoteScheduledNotifications.values().size() + " @: " + regime.days[dayOfWeek].toString());

            for (RemoteScheduledNotification remoteScheduledNotification : regime.days[dayOfWeek].remoteScheduledNotifications.values()) {
                //in this instance (regime days) start time is from zero
                long alarmTime = 0;
                alarmTime += (remoteScheduledNotification.notificationTime) * 1000;
                Log.v("regime_RMS_add", "alarmTime1 : " + alarmTime);
                alarmTime += this.start.getTime();
                Log.v("regime_RMS_add", "alarmTime2 : " + alarmTime);
                alarmTime += startCalendar.get(Calendar.ZONE_OFFSET);
                Log.v("regime_RMS_add", "alarmTime3 : " + alarmTime + " timezone: " + Calendar.getInstance().get(Calendar.ZONE_OFFSET));
                Log.v("regime_RMS_add", "day : " + new SimpleDateFormat("HH:mm").format(new Date(alarmTime)));

                if (alarmTime > nowMili) {
                    Log.v("regime_RMS_add", "set.");
                    AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, alarmTime, makeAlarm(context, remoteScheduledNotification));
                } else {
                    Log.v("regime_RMS_add", "setn't.");
                }

                //if(!remoteScheduledNotification.isSet) {
                    //in this instance (regime days) start time is from zero
                    /*long alarmTime = remoteScheduledNotification.start.getTime();
                    Log.v("setRMS", "alarmTime1 : " + alarmTime);
                    alarmTime += (remoteScheduledNotification.notificationTime) * 1000;
                    Log.v("setRMS", "alarmTime2 : " + alarmTime);
                    alarmTime += this.start.getTime();
                    Log.v("setRMS", "alarmTime3 : " + alarmTime);

                    if (alarmTime > nowMili) {
                        Log.v("setRMS", "set.");
                        AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, alarmTime, makeAlarm(context, remoteScheduledNotification));
                    }
                    remoteScheduledNotification.isSet = true;*/
                //}
            }
        }
    }

    void addRegimeRemoteNotificationTimes(Context context, Day regimeDay){
        long nowMili = System.currentTimeMillis();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Log.v("regime_RMS_add_", "it is Today: " + regimeDay.remoteScheduledNotifications.values().size() + " @: " + regimeDay.toString());


        for (RemoteScheduledNotification remoteScheduledNotification : regimeDay.remoteScheduledNotifications.values()) {
            //in this instance (regime days) start time is from zero
            long alarmTime = 0;
            alarmTime += (remoteScheduledNotification.notificationTime) * 1000;
            Log.v("regime_RMS_add_", "alarmTime1 : " + alarmTime);
            alarmTime += this.start.getTime();
            Log.v("regime_RMS_add_", "alarmTime2 : " + alarmTime);
            alarmTime += Calendar.getInstance().get(Calendar.ZONE_OFFSET);
            Log.v("set_RMS", "alarmTime3 : " + alarmTime + " timezone: " + Calendar.getInstance().get(Calendar.ZONE_OFFSET));
            Log.v("regime_RMS_add_", "day : " + new SimpleDateFormat("HH:mm").format(new Date(alarmTime)));

            if (alarmTime > nowMili) {
                Log.v("regime_RMS_add_", "set.");
                AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, alarmTime, makeAlarm(context, remoteScheduledNotification));
            } else {
                Log.v("regime_RMS_add_", "setn't.");
            }

        }
    }

    void removeRegimeNotifications(){

    }

    void notificationSetAlarm(Context context, Intent notificationIntent, AlarmManager alarmManager, DayItem dayItem, DayItem.NotificationTime timeOffset){
        notificationIntent.putExtra("requestID", timeOffset.requestID);
        notificationIntent.putExtra("dayItemStart", dayItem.start.getTime());
        notificationIntent.putExtra("dayItemEnd", dayItem.end.getTime());
        notificationIntent.putExtra("notificationOffset", timeOffset.offset);
        notificationIntent.putExtra("notificationOffsetR", timeOffset.fromEnd);

        Log.v("notificationSet", "sameday");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, timeOffset.requestID, notificationIntent, 0);

        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, dayItem.start.getTime() + (timeOffset.offset * 1000), pendingIntent);
    }

    static void setAllNotifications(Context context){
        Day currentDay = getDay(context, Calendar.getInstance().getTime());

        System.out.println("tk.lakatstudio setAllNotifications ");

        if(currentDay == null){
            return;
        }

        DayInit.initGson();
        DayInit.loadRegimes(context);

        //TODO calculate remoteschedulednotification in routines
        Regime.setAllActiveRegimesDays(context, currentDay);

        DayInit.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert am != null;
        long nowMili = System.currentTimeMillis();

        for(DayItem dayItem : currentDay.dayItems.values()){
            dayItem.nullCheck();
            Log.v("uuid_debug", dayItem.ID.toString());
            if(dayItem.notificationTimes.size() > 0){
                for(DayItem.NotificationTime notificationTime : dayItem.notificationTimes.values()){
                    if((dayItem.start.getTime() + notificationTime.offset * 1000) > nowMili) {
                        AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, (dayItem.start.getTime() + notificationTime.offset * 1000), makeAlarm(context, dayItem));
                    }
                }
            }

        }

        //Load scheduled notifications
        if(currentDay.remoteScheduledNotifications.size() > 0){
            Log.v("tk.lakatstudio_notifSet", "more than 0");
            System.out.println("tk.lakatstudio notificationSet_RMS ");
            for(RemoteScheduledNotification rSN : currentDay.remoteScheduledNotifications.values()){
                Log.v("notificationSet_RMS", "pre_set");
                long alarmTime = rSN.start.getTime() + rSN.notificationTime * 1000;
                if(alarmTime > nowMili) {
                    AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, alarmTime, makeAlarm(context, rSN));
                }
            }
        } else {
            Log.v("tk.lakatstudio_notifSet", "00");
            System.out.println("tk.lakatstudio notificationSet_RMS 0");
        }
    }

    static PendingIntent makeAlarm(Context context, DayItem dayItem){

        Intent notificationIntent = new Intent(context, NotificationReceiver.class);

        notificationIntent.setPackage(context.getPackageName());
        notificationIntent.setAction("Notification.Create");

        notificationIntent.putExtra("dayItemID", dayItem.ID.toString());
        notificationIntent.putExtra("dayItemTypeName", dayItem.type.name);
        notificationIntent.putExtra("dayItemStart", dayItem.start.getTime());
        notificationIntent.putExtra("dayItemEnd", dayItem.end.getTime());
        notificationIntent.putExtra("requestID", DayInit.notificationRequestID);

        DayInit.increaseNotificationRequestID();

        return PendingIntent.getBroadcast(context, DayInit.notificationRequestID - 1, notificationIntent, 0);
    }

    static Day getDay(Context context, Date date){
        Day out;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date.getTime());
        if(DayInit.daysHashMap != null){
            if((out = DayInit.daysHashMap.get(calendar.get(Calendar.YEAR) * 366 + calendar.get(Calendar.DAY_OF_YEAR))) != null){
                return out;
            }
        }

        ArrayList<String> days = DayInit.readFromFile(context, "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(date));

        if(days == null){
            return new Day();
        }

        out = DayInit.gson.fromJson(days.get(0), Day.class);
        return out;
    }

    static Day getDay(int dayIndex){
        Day out;
        if(DayInit.daysHashMap != null){
            if((out = DayInit.daysHashMap.get(dayIndex)) != null){
                return out;
            }
        }

        return null;
    }

    void addRemoteScheduledNotification(DayItem dayItem, long notificationTime, int dayIndex, UUID ID){
        remoteScheduledNotifications.put(ID, new RemoteScheduledNotification(dayItem, notificationTime, ID));
        isSaved = false;
        if(dayIndex != -1 && DayInit.daysHashMap != null) {
            if(DayInit.daysHashMap.get(dayIndex) != null){
                return;
            }
            DayInit.daysHashMap.put(dayIndex, this);
        }
    }

    void removeRemoteScheduledNotification(UUID remoteScheduledNotificationIndex, int dayIndex){
        remoteScheduledNotifications.remove(remoteScheduledNotificationIndex);
        isSaved = false;
        if(dayIndex != -1 && DayInit.daysHashMap != null) {
            if(DayInit.daysHashMap.get(dayIndex) != null){
                return;
            }
            DayInit.daysHashMap.put(dayIndex, this);
        }
    }
}
