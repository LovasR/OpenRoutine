package tk.lakatstudio.timeallocator;

import java.util.LinkedHashMap;
import java.util.UUID;

public class ActivityType {
    String name;
    UUID ID;
    int color;
    boolean isColorCustom;
    int preferredLength;

    boolean isSaved;

    public static LinkedHashMap<UUID, ActivityType> allActivityTypes = new LinkedHashMap<>();

    ActivityType(){ }

    static ActivityType addActivityType(String name, int color){
        ActivityType at = new ActivityType();
        at.name = name;
        at.ID = UUID.randomUUID();
        at.color = color;
        at.isSaved = false;
        at.isColorCustom = false;
        //at.preferredLength = -1;
        allActivityTypes.put(at.ID, at);
        return at;
    }

    static void addActivityType(ActivityType at){
        //allActivityTypes.add(at);
    }
}
