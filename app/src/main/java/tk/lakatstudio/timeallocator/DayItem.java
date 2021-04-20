package tk.lakatstudio.timeallocator;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class DayItem {
    String name;
    Date start;
    Date end;
    boolean isRunning;
    ActivityType type;

    //notifications associated with this, times are relative
    @Expose
    ArrayList<Integer> notificationTimes;

    ArrayList<NotificationTime> notificationTimesOA;

    UUID ID;

    static HashMap<UUID, DayItem> allDayItemHashes = new HashMap<>();

    DayItem(String n, Date s, Date e, ActivityType t, ArrayList<NotificationTime> nT){
        name = n;
        start = s;
        end = e;
        type = t;
        isRunning = false;
        notificationTimesOA = nT;
        ID = UUID.randomUUID();
    }

    static class NotificationTime{
        int offset;
        boolean fromEnd;
        NotificationTime(int offset, boolean fromEnd){
            this.offset = offset;
            this.fromEnd = fromEnd;
        }
    } 

    void nullCheck(){
        if(ID == null){
            this.ID = UUID.randomUUID();
        }
        if(notificationTimesOA == null){
            this.notificationTimesOA = new ArrayList<>();
        }
    }

}
