package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class DayInit {

    static HashMap<Integer, Day> daysHashMap = new HashMap<>();

    static void init(Context context, MainActivity mainActivity){


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

            CycleManager.currentDay = today;
            CycleManager.currentItem = today.getDayItem(Calendar.getInstance().getTime());


            int[] defaultColors = context.getResources().getIntArray(R.array.default_colors);
            String[] names = context.getResources().getStringArray(R.array.default_activities);
            for(int i = 0; i < names.length; i++) {
                ActivityType.addActivityType(names[i], defaultColors[i]);
            }
        } else {
            Log.v("save_debug_load", "Loading finished " + CycleManager.currentDay.dayItems.size());
            //mainActivity.fragment1.dayPlannerInit(mainActivity.fragment1);
        }

        if(ActivityType.allActivityTypes.size() > 0) {
            ActivityType.currentID = ActivityType.allActivityTypes.get(ActivityType.allActivityTypes.size() - 1).ID;
        }

        /*for(int i = 0; i < 24 * (60 / CycleManager.cycleTime); i++){
            Cycle cycle = new Cycle();
            cycle.index = i;
            cycle.dayItem = today.getDayItem(Calendar.getInstance().getTime());
        }*/

    }
    static void saveAll(Context context){
        Gson gson = new Gson();
        String outJson = "";
        //for(int i = 0; i < CycleManager.currentDay.dayItems.size(); i++){
            //DayItem dayItem = CycleManager.currentDay.dayItems.get(i);
            //if(dayItem.)
        if(!CycleManager.currentDay.isSaved){
            outJson += gson.toJson(CycleManager.currentDay);
            outJson += "\n";
        }
        //}
        Log.v("Out_Json days", outJson);
        if(outJson.length() > 0) {
            writeToFile(context, outJson, "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(Calendar.getInstance().getTime()));
        }

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

        /*outJson = "";
        for(int i = 0; i < TodoItem.allTodoItems.size(); i++){
            TodoItem todoItem = TodoItem.allTodoItems.get(i);
            outJson += gson.toJson(todoItem);
            outJson += "\n";
        }
        Log.v("save_debug", "todos: " + outJson);
        if(outJson.length() > 0){
            writeToFile(context, outJson, "todos");
        }*/
        outJson = "";
        for(int i = 0; i < Regime.allRegimes.size(); i++){
            Regime regime = Regime.allRegimes.get(i);
            regime.isSaved = true;
            outJson += gson.toJson(regime);
            outJson += "\n";
        }
        Log.v("save_debug", "regimes: " + outJson);
        if(outJson.length() > 0){
            writeToFile(context, outJson, "regimes");
        }

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
            CycleManager.currentDay = gson.fromJson(dayJson, Day.class);
            daysHashMap.put(Calendar.getInstance().get(Calendar.DAY_OF_YEAR), CycleManager.currentDay);
            for(DayItem dayItem : CycleManager.currentDay.dayItems){
                DayItem.allDayItemHashes.put(dayItem.hashCode(), dayItem);
                Log.v("hash_debug", dayItem.hashCode() + "");
            }
            Log.v("save_debug_load", "load day: " + CycleManager.currentDay.dayItems.size() + " " + (CycleManager.currentDay.dayItems.size() > 0 ? CycleManager.currentDay.dayItems.get(0).type.name : "null"));
        }

        ArrayList<String> activityList = readFromFile(context, "activities");
        Log.v("save_debug_load", "load activities: " + activityList.size());
        for(String activityJson : activityList){
            ActivityType.addActivityType(gson.fromJson(activityJson, ActivityType.class));
            Log.v("save_debug_load", "load activity: \t" + activityJson);
        }

        /*ArrayList<String> todoList = readFromFile(context, "todos");
        if(todoList != null) {
            for (String todoJson : todoList) {
                TodoItem todoItem = gson.fromJson(todoJson, TodoItem.class);
                TodoItem.addItem(todoItem);
                //TODdO hash
                //todoItem.dayItem = DayItem.allDayItemHashes.get(todoItem.dayItemHash);
                Log.v("save_debug_load", "load toddo: \t" + todoJson);
            }
        }*/

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
            e.printStackTrace();
            Log.v("save_debug_load", "file error");
            return null;
        }
        return out;
    }
}
