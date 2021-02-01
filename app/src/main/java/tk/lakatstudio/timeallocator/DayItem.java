package tk.lakatstudio.timeallocator;

import java.util.Date;

public class DayItem {
    Date start;
    Date end;
    boolean isRunning;
    ActivityType type;
    DayItem(Date s, Date e, ActivityType t){
        start = s;
        end = e;
        type = t;
        isRunning = false;
    }
}
