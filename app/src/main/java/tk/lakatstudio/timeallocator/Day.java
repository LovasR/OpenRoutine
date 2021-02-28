package tk.lakatstudio.timeallocator;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class Day {
    Date start;         //Start of day to serve as base line for cycles

    ArrayList<Cycle> cycles = new ArrayList<Cycle>();
    ArrayList<DayItem> dayItems = new ArrayList<DayItem>();

    int cycleIndex;
    int dayItemIndex;

    boolean isSaved;

    DayItem getDayItem(Date time){
        for(int i = 0; i < dayItems.size(); i++){
            if(dayItems.get(i).start.getTime() < time.getTime() && dayItems.get(i).end.getTime() > time.getTime()){
                //check current dayItem type
                return dayItems.get(i);
            }
        }
        return null;
    }

    void addDayItem(DayItem newDayItem){
        for(int i = 0; i < dayItems.size(); i++){
            DayItem dayItem = dayItems.get(i);

            //This logic may show up as simplifiable but it breaks when simplified
            //it checks if it needs to compare to the next item but avoids indexoutofbounds.

            if(dayItem.start.getTime() < newDayItem.start.getTime() &&
                    (dayItems.size() == (i + 1) ? true : (dayItems.get(i + 1).start.getTime() > newDayItem.start.getTime()))){
                dayItems.add(i + 1, newDayItem);
                Log.v("Day_list", "Item added, last item: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dayItems.get(i).start.getTime()) + " list size now is: " + dayItems.size());
                Log.v("Day_list", String.valueOf(dayItem.start.getTime() < newDayItem.start.getTime()));
                return;
            } else if (dayItems.size() == (i + 1) ? false : (dayItems.get(i + 1).start.getTime() > newDayItem.start.getTime())){

            }
        }
        if(dayItems.size() == 0){
            dayItems.add(0, newDayItem);
        } else if(dayItems.get(0).start.getTime() > newDayItem.start.getTime()) {
            dayItems.add(0, newDayItem);
        }
        Log.v("Day_list", "Item added, item: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dayItems.get(0).start.getTime()) + " list size now is: " + dayItems.size());
    }

    void removeDayItem(int index){
        Log.v("Day_list", "Item removed size before: " + dayItems.size() + " @: " + index);
        dayItems.remove(index);
        Log.v("Day_list", "Item removed size now: " + dayItems.size());
    }
}
