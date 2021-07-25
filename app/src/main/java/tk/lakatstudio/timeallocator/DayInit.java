package tk.lakatstudio.timeallocator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.AlarmManagerCompat;
import androidx.preference.PreferenceManager;

import com.google.android.material.timepicker.TimeFormat;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DayInit {

    static HashMap<Integer, Day> daysHashMap = new HashMap<>();
    static SharedPreferences sharedPreferences;
    
    static Day currentDay;
    static HashMap<UUID, DayItem> currentDayItems = new HashMap<>();
    //log each set alarm
    static int notificationRequestID;

    //main Gson
    static Gson gson;

    static void init(final Context context){

        initGson();

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

            int[] defaultColors = context.getResources().getIntArray(R.array.default_colors);
            String[] names = context.getResources().getStringArray(R.array.default_activities);
            for(int i = 0; i < names.length; i++) {
                ActivityType.addActivityType(names[i], defaultColors[i]);
            }
        } else {
            //Log.v("save_debug_load", "Loading finished " + currentDay.dayItems.size());
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
        }).start();
    }

    static void initGson(){

        ExclusionStrategy strategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }

            @Override
            public boolean shouldSkipField(FieldAttributes field) {
                return field.getAnnotation(Exclude.class) != null;
            }
        };

        gson = new GsonBuilder().addSerializationExclusionStrategy(strategy).create();
    }

    static void increaseNotificationRequestID(){
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
        if(dayItem.notificationTimes.size() > 0){
            DayItem dayItem_ = currentDayItems.get(UUID.fromString(dayItem.ID.toString()));
            Log.v("notifications", (dayItem_ == null ? "dayItem_ is null " : "dayItem_ is !!!null "));
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent notificationIntent = new Intent(context, NotificationReceiver.class);

            notificationIntent.setPackage(context.getPackageName());
            notificationIntent.setAction("Notification.Create");
            notificationIntent.putExtra("dayItemID", dayItem.ID.toString());
            notificationIntent.putExtra("dayItemTypeName", dayItem.type.name);

            for(DayItem.NotificationTime timeOffset : dayItem.notificationTimes.values()){
                notificationIntent.putExtra("requestID", timeOffset.requestID);
        notificationIntent.putExtra("dayItemStart", dayItem.start.getTime());
        notificationIntent.putExtra("dayItemEnd", dayItem.end.getTime());
                notificationIntent.putExtra("notificationOffset", timeOffset.offset);
                notificationIntent.putExtra("notificationOffsetR", timeOffset.fromEnd);

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

    static void saveAll(final Context context){

        //save modified days
        Log.v("day_save", daysHashMap.toString());
        new Thread(new Runnable() {
            @Override
            public void run() {
                String outJson = "";
                for(Day day : daysHashMap.values()){
                    if(!day.isSaved){
                        //remove dayItems added by the regimes to make regimes usable
                        if(day.start.getTime() > System.currentTimeMillis()){
                            Regime.removeRegimeDays(context, day);
                        }
                        outJson = "";
                        outJson += gson.toJson(day);
                        outJson += "\n";
                        Log.v("day_save", "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(day.start.getTime()));
                        writeToFile(context, outJson, "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(day.start.getTime()));
                        day.isSaved = true;
                    }
                }
            }
        }).start();

        Log.v("day_debug", "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(Calendar.getInstance().getTime()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder outJson = new StringBuilder();
                int lastID = -1;
                for(int i = 0; i < ActivityType.allActivityTypes.size(); i++){
                    ActivityType activityType = ActivityType.allActivityTypes.get(i);
                    Log.v("save_debug", "test");
                    if(activityType.ID > lastID){
                        activityType.isSaved = true;
                        outJson.append(gson.toJson(activityType));
                        outJson.append("\n");
                        Log.v("save_debug", "save activity");
                        lastID = activityType.ID;
                    }
                }
                Log.v("Out_Json activities", outJson.toString());
                if(outJson.length() > 0){
                    writeToFile(context, outJson.toString(), "activities");
                }
            }
        }).start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                String outJson = "";
                for(Regime regime : Regime.allRegimes.values()){
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
            }
        }).start();


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
            Log.v("save_debug", "successful file write: " + json.length() + " @: " + file.getAbsolutePath());
        } catch (Exception e){
            //e.printStackTrace();
            Log.v("save_debug", "file writing exception");
        }
    }

    static boolean loadAll(Context context){
        loadRegimes(context);

        ArrayList<String> days = readFromFile(context, "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(Calendar.getInstance().getTime()));

        if(days != null) {
            for (String dayJson : days) {
                currentDay = gson.fromJson(dayJson, Day.class);
                Calendar calendar = Calendar.getInstance();
                daysHashMap.put(calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 366, currentDay);
                for (DayItem dayItem : currentDay.dayItems.values()) {
                    dayItem.nullCheck();
                    Log.v("uuid_debug", dayItem.ID.toString());
                    DayInit.currentDayItems.put(dayItem.ID, dayItem);
                    DayItem.allDayItemHashes.put(dayItem.ID, dayItem);
                }
                currentDay.isRegimeSet = false;
             }
        }

        ArrayList<String> activityList = readFromFile(context, "activities");
        if(activityList != null) {
            Log.v("save_debug_load", "load activities: " + activityList.size());
            for (String activityJson : activityList) {
                ActivityType.addActivityType(gson.fromJson(activityJson, ActivityType.class));
                Log.v("save_debug_load", "load activity: \t" + activityJson);
            }
        }
        return true;
    }

    static void loadRegimes(Context context){
        Regime.allRegimes = new HashMap<>();
        Log.v("regime_null", "loadRegimes");
        ArrayList<String> regimeList = readFromFile(context, "regimes");
        if(regimeList != null) {
            Log.v("save_Debug_load", "regime file lines: " + regimeList.size());
            for (String regimeJson : regimeList) {
                Regime regime = gson.fromJson(regimeJson, Regime.class);
                regime.nullCheck();
                Regime.addRegime(regime);
            }
        } else {
            Log.v("save_Debug_load", "regime file null");
        }
    }

    static ArrayList<String> readFromFile(Context context, String fileName){
        if(context == null)
            Log.v("save_debug_load", "context_null");
        File dir = new File(context.getFilesDir(), "saves");
        if(!dir.exists()){
            Log.v("save_debug_load", "directory !exist");
            return null;
        }

        ArrayList<String> out = new ArrayList<>();

        try {
            File file = new File(dir, fileName);
            if(!file.exists()){
                return null;
            }
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
            Log.v("save_debug_load", "file error: " + e.toString());
            return null;
        }
        return out;
    }

    String finalOutputPath;

    public static void exportData(Context context, Uri outputPath){
        if(outputPath == null){
            return;
        }
        String sourceFolder = context.getFilesDir() + "/saves";
        List<String> fileList = new ArrayList<>();
        generateFileList(new File(sourceFolder), fileList, sourceFolder);
        makeZip(context, outputPath, fileList, sourceFolder);
        Toast.makeText(context, R.string.data_export_success, Toast.LENGTH_SHORT);
    }

    public static void makeZip(Context context, Uri zipFile, List<String> fileList, String sourceFolder) {
        byte[] buffer = new byte[1024];
        String source = new File(sourceFolder).getName();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = (FileOutputStream) context.getContentResolver().openOutputStream(zipFile, "rw");
            zos = new ZipOutputStream(fos);

            Log.v("export_data", "Output to Zip : " + zipFile);
            FileInputStream in = null;

            for (String file: fileList) {
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(sourceFolder + File.separator + file);
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, length);
                    }
                } finally {
                    in.close();
                }
            }
            zos.closeEntry();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void generateFileList(File node, List<String> fileList, String sourceFolder) {
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString(), sourceFolder));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename: subNote) {
                generateFileList(new File(node, filename), fileList, sourceFolder);
            }
        }
    }

    private static String generateZipEntry(String file, String sourceFolder) {
        return file.substring(sourceFolder.length() + 1, file.length());
    }

    public static void importData(Context context, Uri uri){
        deleteData(context);
        //could be optional to merge the saves folder
        unzip(context, context.getFilesDir().getPath() + "/", uri);
        refreshAppData(context);
        Toast.makeText(context, R.string.data_import_success, Toast.LENGTH_SHORT);
    }

    static void deleteData(Context context){
        String savesPath = context.getFilesDir().getPath() + "/saves";
        File savesDir = new File(savesPath);
        String[] entries = savesDir.list();
        for(String s : entries){
            File currentFile = new File(savesDir.getPath(),s);
            currentFile.delete();
        }
        Log.v("import_data", savesDir.list().length + " ");
    }
    static void refreshAppData(Context context){
        Regime.allRegimes.clear();
        daysHashMap.clear();
        ActivityType.allActivityTypes.clear();
        DayInit.currentDayItems.clear();
        DayItem.allDayItemHashes.clear();

        loadAll(context);
        MainFragment1.staticClass.refreshAllFragments(context, -1);
    }
    public static void unzip(Context context, String path, Uri zipFile) {
        try  {
            FileInputStream fin = (FileInputStream) context.getContentResolver().openInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;

            while ((ze = zin.getNextEntry()) != null) {

                if(ze.isDirectory()) {
                    if(!ze.getName().equals("saves")){
                        Toast.makeText(context, R.string.data_import_format_error, Toast.LENGTH_LONG);
                        return;
                    }
                    File f = new File(path + ze.getName());

                    if(!f.isDirectory()) {
                        f.mkdirs();
                    }
                } else {
                    FileOutputStream fout = new FileOutputStream(path + ze.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = zin.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }
                    bufout.close();
                    zin.closeEntry();
                    fout.close();
                }

            }
            zin.close();
        } catch(Exception e) {
            Log.v("import_data", e.toString());
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Exclude {}
}
