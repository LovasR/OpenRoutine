package tk.lakatstudio.timeallocator;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;

public class ActivityType {
    String name;
    int ID;
    Color color;

    public final static ArrayList<ActivityType> allActivityTypes = new ArrayList<ActivityType>();
    static int currentID;

    static ArrayList<ActivityType> userActivityTypes = new ArrayList<ActivityType>();

    ActivityType(String n, int id, Color c){
        name = n;
        ID = id;
        color = c;
    }

    ActivityType(){ }

    static void addActivityType(String name, Color color){
        int ID = ++currentID;
        ActivityType at = new ActivityType();
        at.name = name;
        at.ID = ID;
        at.color = color;
        allActivityTypes.add(at);
        Log.e("AT_test", name + "\t" + color.toString() + "\t" + allActivityTypes.get(allActivityTypes.size() - 1).name);
    }
}
