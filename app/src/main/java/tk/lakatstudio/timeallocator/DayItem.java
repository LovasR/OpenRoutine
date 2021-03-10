package tk.lakatstudio.timeallocator;

import android.util.Log;

import java.util.Date;
import java.util.HashMap;

public class DayItem {
    String name;
    Date start;
    Date end;
    boolean isRunning;
    ActivityType type;

    static HashMap<Integer, DayItem> allDayItemHashes = new HashMap<>();

    DayItem(String n, Date s, Date e, ActivityType t){
        name = n;
        start = s;
        end = e;
        type = t;
        isRunning = false;
    }


    //TODO hash
    static void addItemHash(DayItem item){
        allDayItemHashes.put(item.hashCode(), item);
        Log.v("hash_debug", item.hashCode() + "");
    }
}
