package tk.lakatstudio.timeallocator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.util.Log;

import androidx.core.app.AlarmManagerCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

public class DayInit {

    static HashMap<Integer, Day> daysHashMap = new HashMap<>();
    static SharedPreferences sharedPreferences;
    
    static Day currentDay;
    static HashMap<UUID, DayItem> currentDayItems = new HashMap<>();
    //log each set alarm
    static int notificationRequestID;

    static void init(final Context context){
        if (!loadAll(context)) {
            Day today;
            today = new Day();

            Calendar date = Calendar.getInstance();
            date.set(Calendar.HOUR_OF_DAY, 0);
            date.set(Calendar.MINUTE, 0);
            date.set(Calendar.SECOND, 0);
            date.set(Calendar.MILLISECOND, 0);
            today.start = date.getTime();

            today.isSaved = false;

            currentDay = today;
            CycleManager.currentItem = today.getDayItem(Calendar.getInstance().getTime());


            int[] defaultColors = context.getResources().getIntArray(R.array.default_colors);
            String[] names = context.getResources().getStringArray(R.array.default_activities);
            for(int i = 0; i < names.length; i++) {
                ActivityType.addActivityType(names[i], defaultColors[i]);
            }
        } else {
            Log.v("save_debug_load", "Loading finished " + currentDay.dayItems.size());
            //mainActivity.fragment1.dayPlannerInit(mainActivity.fragment1);
        }

        if(ActivityType.allActivityTypes.size() > 0) {
            ActivityType.currentID = ActivityType.allActivityTypes.get(ActivityType.allActivityTypes.size() - 1).ID;
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);


        //Other less-important initialization
        new Thread(new Runnable() {
            @Override
            public void run() {
                notificationRequestID = sharedPreferences.getInt("requestID", 0);
                ATNotificationManager.createNotificationChannel(context);
                //am = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
                pendingIntentCleanup(context);
            }
        }).run();
    }

    static void increaseNotificationRequestID(){
        if(notificationRequestID <= 60){
            sharedPreferences.edit().putInt("requestID", (notificationRequestID = 67)).apply();
        }
        if(notificationRequestID <= 2000000000){
            sharedPreferences.edit().putInt("requestID", ++notificationRequestID).apply();
        } else {
            sharedPreferences.edit().putInt("requestID", (notificationRequestID = 1)).apply();
        }
    }

    static void decreaseNotificationRequestID(){
        if(notificationRequestID > 0){
            sharedPreferences.edit().putInt("requestID", --notificationRequestID).apply();
        }
    }

    static void pendingIntentCleanup(Context context){
        Intent alarmIntent = new Intent(context, ATBroadcastReceiver.class);
        alarmIntent.setAction("Notification.Create");
        alarmIntent.putExtra("dayItemID", "");
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            Log.v("notification", "pendingIntent existed ");
            pendingIntent.cancel();
        }
    }

    //static AlarmManager am;
    static void setAlarm(Context context, String dayItemID, long alarmTime){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setPackage(context.getPackageName());
        intent.setAction("Notification.Create");
        intent.putExtra("dayItemID", dayItemID);
        //(int) ((alarmTime / 1000) & 0xFFFFFF)
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

        Log.v("notification", "setting alarm: " + alarmTime + " " + dayItemID + " " + (int) ((alarmTime / 1000) & 0xFFFFFF));
        Log.v("notification", "pendingIntent: " + intent.toString() + (pendingIntent != null ? " !null" : " null"));

        AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, alarmTime, pendingIntent);

        if(am.getNextAlarmClock() != null)
            Log.v("notification", "alarm: " + am.getNextAlarmClock().toString());
        /*if(am.getNextAlarmClock() != null) {
            Log.v("notification", "NextAlarmClock is not null");
            if (am.getNextAlarmClock().getShowIntent().equals(pendingIntent)) {
                Log.v("notification", "same intent: ");
            }
        } else {
            Log.v("notification", "NextAlarmClock is null");
        }*/
    }

    static void addAlarm(Context context, DayItem dayItem){
        if(dayItem.notificationTimesOA.size() > 0){
            DayItem dayItem_ = currentDayItems.get(UUID.fromString(dayItem.ID.toString()));
            Log.v("notifications", (dayItem_ == null ? "dayItem_ is null " : "dayItem_ is !!!null "));
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent notificationIntent = new Intent(context, NotificationReceiver.class);

            notificationIntent.setPackage(context.getPackageName());
            notificationIntent.setAction("Notification.Create");
            notificationIntent.putExtra("dayItemID", dayItem.ID.toString());
            notificationIntent.putExtra("dayItemTypeName", dayItem.type.name);

            for(DayItem.NotificationTime timeOffset : dayItem.notificationTimesOA){
                notificationIntent.putExtra("requestID", timeOffset.requestID);
                notificationIntent.putExtra("dayItemStart", dayItem.start.getTime());

                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, timeOffset.requestID, notificationIntent, 0);

                AlarmManagerCompat.setExactAndAllowWhileIdle(am, AlarmManager.RTC_WAKEUP, dayItem.start.getTime() + (timeOffset.offset * 1000), pendingIntent);

            }
        }
    }

    static void cancelAlarm(Context context, int requestID){
        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setPackage(context.getPackageName());
        intent.setAction("Notification.Create");
        intent.putExtra("dayItemID", "");
        PendingIntent sender = PendingIntent.getBroadcast(context, requestID, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
        decreaseNotificationRequestID();
    }

    static String getHourFormat(Context context){
        //returns either the 24 or 12 hour format based on SharedPreference string list
        String setting = sharedPreferences.getString("hour_format", context.getResources().getStringArray(R.array.hour_format_setting_entries)[0]);
        int options = Arrays.asList(context.getResources().getStringArray(R.array.hour_format_setting_entries)).indexOf(setting);
        Log.v("hour_format", options + "");
        switch (options){
            default:
            case 0:
                // is24HourFormat returns true if 24 hour format is used
                if(DateFormat.is24HourFormat(context)){
                    return context.getString(R.string.format_24_hour);
                }
            case 1:
                return context.getString(R.string.format_12_hour);
            case 2:
                return context.getString(R.string.format_24_hour);
        }
    }

    static int getMaterialTimeFormat(Context context){
        //same as hour format just for material time pickers
        String setting = sharedPreferences.getString("hour_format", context.getResources().getStringArray(R.array.hour_format_setting_entries)[0]);
        int options = Arrays.asList(context.getResources().getStringArray(R.array.hour_format_setting_entries)).indexOf(setting);
        switch (options){
            default:
            case 0:
                // is24HourFormat returns true if 24 hour format is used
                if(DateFormat.is24HourFormat(context)){
                    return TimeFormat.CLOCK_24H;
                }
            case 1:
                return TimeFormat.CLOCK_12H;
            case 2:
                return TimeFormat.CLOCK_24H;
        }
    }

    static String getDateFormat(Context context){
        //get dateformat from string
        Log.v("pref_test", sharedPreferences.getString("date_format", "null"));
        return sharedPreferences.getString("date_format", context.getString(R.string.default_date_format));
    }

    static void saveAll(Context context){
        Gson gson = new Gson();
        String outJson = "";

        //save modified days
        for(Day day : daysHashMap.values()){
            if(!day.isSaved){
                //remove dayItems added by the regimes to make regimes usable
                if(day.start.getTime() > Calendar.getInstance().getTime().getTime()){
                    Regime.removeRegimeDays(day);
                }
                outJson = "";
                outJson += gson.toJson(day);
                outJson += "\n";
                Log.v("day_save", "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(day.start.getTime()));
                writeToFile(context, outJson, "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(day.start.getTime()));
                day.isSaved = true;
            }
        }

        Log.v("day_debug", "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(Calendar.getInstance().getTime()));

        outJson = "";
        for(int i = 0; i < ActivityType.allActivityTypes.size(); i++){
            ActivityType activityType = ActivityType.allActivityTypes.get(i);
            Log.v("save_debug", "test");
            //if(!activityType.isSaved){
                activityType.isSaved = true;
                outJson += gson.toJson(activityType);
                outJson += "\n";
                Log.v("save_debug", "save activity");
            /*} else{
                Log.v("save_debug", "no save activity "/* + ActivityType.allActivityTypes.get(2).isSaved);
            }*/
        }
        Log.v("Out_Json activities", outJson);
        if(outJson.length() > 0){
            writeToFile(context, outJson, "activities");
        }
        ArrayList<String> testRead = readFromFile(context, "activities");
        for(int i = 0; i < testRead.size(); i++){
            Log.e("testreads", testRead.get(i));
        }

        outJson = "";
        for(int i = 0; i < Regime.allRegimes.size(); i++){
            Regime regime = Regime.allRegimes.get(i);
            if(!regime.toDelete) {
                regime.isSaved = true;
                outJson += gson.toJson(regime);
                outJson += "\n";
            }
        }
        Log.v("save_debug", "regimes: " + outJson);
        if(outJson.length() > 0){
            writeToFile(context, outJson, "regimes");
        }


        sharedPreferences.edit().putInt("requestID", notificationRequestID).apply();
    }
    static void writeToFile(Context context, String json, String fileName){
        File dir = new File(context.getFilesDir(), "saves");
        if(!dir.exists()){
            Log.v("save_debug", "directory !exist");
            dir.mkdir();
        }

        try {
            File file = new File(dir, fileName);
            FileWriter writer = new FileWriter(file);
            writer.append(json);
            writer.flush();
            writer.close();
            Log.v("save_debug", "successful file write" + json.length());
        } catch (Exception e){
            e.printStackTrace();
            Log.v("save_debug", "file writing exception");
        }
    }

    static boolean loadAll(Context context){
        ArrayList<String> days = readFromFile(context, "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(Calendar.getInstance().getTime()));

        if(days == null){
            return false;
        }

        Gson gson = new Gson();
        for(String dayJson : days){
            currentDay = gson.fromJson(dayJson, Day.class);
            Calendar calendar = Calendar.getInstance();
            daysHashMap.put(calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 365, currentDay);
            for(DayItem dayItem : currentDay.dayItems){
                dayItem.nullCheck();
                Log.v("uuid_debug", dayItem.ID.toString());
                DayInit.currentDayItems.put(dayItem.ID, dayItem);
                DayItem.allDayItemHashes.put(dayItem.ID, dayItem);
            }
            Log.v("save_debug_load", "load day: " + currentDay.dayItems.size() + " " + (currentDay.dayItems.size() > 0 ? currentDay.dayItems.get(0).type.name : "null"));
        }

        ArrayList<String> activityList = readFromFile(context, "activities");
        Log.v("save_debug_load", "load activities: " + activityList.size());
        for(String activityJson : activityList){
            ActivityType.addActivityType(gson.fromJson(activityJson, ActivityType.class));
            Log.v("save_debug_load", "load activity: \t" + activityJson);
        }

        ArrayList<String> regimeList = readFromFile(context, "regimes");
        if(regimeList != null) {
            for (String regimeJson : regimeList) {
                Regime.addRegime(gson.fromJson(regimeJson, Regime.class));
            }
        }

        return true;
    }

    static ArrayList<String> readFromFile(Context context, String fileName){
        File dir = new File(context.getFilesDir(), "saves");
        if(!dir.exists()){
            Log.v("save_debug_load", "directory !exist");
            return null;
        }

        ArrayList<String> out = new ArrayList<>();

        try {
            File file = new File(dir, fileName);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String object;
            while ((object = reader.readLine()) != null){
                out.add(object);
                Log.v("save_debug_load", "read: \t" + object);
            }
            reader.close();
            Log.v("save_debug_load", "successful read" + out.size());
        } catch (Exception e){
            //e.printStackTrace();
            Log.v("save_debug_load", "file error");
            return null;
        }
        return out;
    }
}
