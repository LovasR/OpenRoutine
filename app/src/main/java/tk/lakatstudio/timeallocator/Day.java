package tk.lakatstudio.timeallocator;


import java.util.ArrayList;
import java.util.Date;

public class Day {
    Date start;         //Start of day to serve as base line for cycles

    ArrayList<Cycle> cycles = new ArrayList<Cycle>();
    ArrayList<DayItem> dayItems = new ArrayList<DayItem>();

    int cycleIndex;
    int dayItemIndex;

    DayItem getDayItem(Date time){
        for(int i = 0; i < dayItems.size(); i++){
            if(dayItems.get(i).start.getTime() < time.getTime() && dayItems.get(i).end.getTime() > time.getTime()){
                //check current dayItem type
                return dayItems.get(i);
            }
        }
        return null;    //TODO return
    }

    void setDayItem(){

    }


    void addDayItem(DayItem newDayItem){
        for(int i = 0; i < dayItems.size(); i++){
            DayItem dayItem = dayItems.get(i);
            if(dayItem.start.getTime() < newDayItem.start.getTime() && dayItem.end.getTime() < newDayItem.start.getTime()){
                dayItems.add(i + 1, newDayItem);
            }
        }
    }
}
