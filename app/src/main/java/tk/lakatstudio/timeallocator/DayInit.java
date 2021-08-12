package tk.lakatstudio.timeallocator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
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
import java.util.LinkedHashSet;
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

    static Locale defaultLocale;

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

        setSelectedTheme(null);

        //Other less-important initialization
        new Thread(new Runnable() {
            @Override
            public void run() {
                notificationRequestID = sharedPreferences.getInt("requestID", 0);
                TANotificationManager.createNotificationChannel(context);
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
        Intent alarmIntent = new Intent(context, GeneralBroadcastReceiver.class);
        alarmIntent.setAction("Notification.Create");
        alarmIntent.putExtra("dayItemID", "");
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent,PendingIntent.FLAG_NO_CREATE);
        if (pendingIntent != null) {
            Log.v("notification", "pendingIntent existed ");
            pendingIntent.cancel();
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

    static void refreshWidget(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, LineupWidgetProvider.class));
        if(appWidgetIds.length > 0) {
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.lineupWidgetListView);
        }
    }

    static int getMaterialTimeFormat(Context context){
        //same as hour format just for material time pickers
       int options = Integer.parseInt(sharedPreferences.getString("hour_format", "0"));
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

    static void setSelectedTheme(Object newValue){
        int options = Integer.parseInt(newValue == null ? sharedPreferences.getString("dark_mode", "0") : (String) newValue);
        switch (options){
            default:
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    public static void setLocale(Resources resources, String languageCode) {
        Locale locale;
        if(languageCode == null) {
            locale = new Locale(sharedPreferences.getString("language_select", "en"));
        } else if(languageCode.equals("null")){
            locale = defaultLocale;
        } else {
            locale = new Locale(languageCode);
        }
        Locale.setDefault(locale);
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    static String getDateFormat(Context context){
        //get dateformat from string
        return sharedPreferences.getString("date_format", context.getString(R.string.default_date_format));
    }

    static void saveAll(final Context context){

        //save modified days
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
                for(ActivityType activityType : ActivityType.allActivityTypes.values()){
                    activityType.isSaved = true;
                    outJson.append(gson.toJson(activityType));
                    outJson.append("\n");
                    Log.v("save_debug", "save activity");
                }
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
        } catch (Exception e){
            //e.printStackTrace();
            Log.e("save_debug", "file writing exception");
        }
    }

    static boolean loadAll(Context context){
        ArrayList<String> activityList = readFromFile(context, "activities");
        if(activityList != null) {
            Log.v("save_debug_load", "load activities: " + activityList.size());
            ActivityType.allActivityTypes.clear();
            LinkedHashSet<ActivityType> temporaryActivityTypes = new LinkedHashSet<>();
            for (String activityJson : activityList) {
                ActivityType activityType = gson.fromJson(activityJson, ActivityType.class);
                temporaryActivityTypes.add(activityType);
                Log.v("save_debug_load", "load activity: \t" + activityJson);
            }
            Log.v("save_debug_load", temporaryActivityTypes.toString());
            for(ActivityType activityType : temporaryActivityTypes){
                ActivityType.allActivityTypes.put(activityType.ID, activityType);
            }
        } else {
            return false;
        }

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

    public static void exportData(Context context, Uri outputPath){
        if(outputPath == null){
            return;
        }
        String sourceFolder = context.getFilesDir() + "/saves";
        List<String> fileList = new ArrayList<>();
        generateFileList(new File(sourceFolder), fileList, sourceFolder);
        makeZip(context, outputPath, fileList, sourceFolder);
        Toast.makeText(context, R.string.data_export_success, Toast.LENGTH_SHORT).show();
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
            for (String filename : subNote) {
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
        Toast.makeText(context, R.string.data_import_success, Toast.LENGTH_SHORT).show();
    }

    static void deleteData(Context context){
        String savesPath = context.getFilesDir().getPath() + "/saves";
        File savesDir = new File(savesPath);
        String[] entries = savesDir.list();
        if(entries != null) {
            for (String s : entries) {
                File currentFile = new File(savesDir.getPath(), s);
                currentFile.delete();
            }
            Log.v("import_data", savesDir.list().length + " ");
        }
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
                        Toast.makeText(context, R.string.data_import_format_error, Toast.LENGTH_LONG).show();
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
