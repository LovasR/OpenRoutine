package tk.lakatstudio.timeallocator;

import java.util.Date;

public class DayItem {
    String name;
    Date start;
    Date end;
    boolean isRunning;
    ActivityType type;


    DayItem(String n, Date s, Date e, ActivityType t){
        name = n;
        start = s;
        end = e;
        type = t;
        isRunning = false;
    }
}
