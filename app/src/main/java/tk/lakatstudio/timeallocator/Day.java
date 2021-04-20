package tk.lakatstudio.timeallocator;


import android.util.Log;

import com.google.gson.annotations.Expose;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class Day {
    Date start;         //Start of day to serve as base line for cycles

    ArrayList<Cycle> cycles = new ArrayList<Cycle>();
    ArrayList<DayItem> dayItems = new ArrayList<DayItem>();


    ArrayList<TodoItem> todoItems = new ArrayList<TodoItem>();

    //used to identify dayItems from regimes
    //TODO make the regime added dayItems savable as hashes
    @Expose
    ArrayList<DayItem> regimeDayItems = new ArrayList<DayItem>();
    @Expose
    ArrayList<TodoItem> regimeTodoItems = new ArrayList<TodoItem>();


    int cycleIndex;

    boolean isSaved;
    //for when regime`s dayItems are set
    boolean isRegimeSet = false;

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
                isSaved = false;
                return;
            } else if (dayItems.size() == (i + 1) ? false : (dayItems.get(i + 1).start.getTime() > newDayItem.start.getTime())){

            }
        }
        if(dayItems.size() == 0){
            dayItems.add(0, newDayItem);
        } else if(dayItems.get(0).start.getTime() > newDayItem.start.getTime()) {
            dayItems.add(0, newDayItem);
        }
        isSaved = false;
        Log.v("Day_list", "Item added, item: " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dayItems.get(0).start.getTime()) + " list size now is: " + dayItems.size());
    }

    ArrayList<DayItem> defaultTimeSortDayItems(ArrayList<DayItem> dayItems){
        Collections.sort(dayItems, new Comparator<DayItem>() {
            @Override
            public int compare(DayItem item1, DayItem item2) {
                return Long.compare(item1.start.getTime(), item2.start.getTime());
            }
        });
        return dayItems;
    }

    //Day dayFrom

    void removeDayItem(int index){
        Log.v("Day_list", "Item removed size before: " + dayItems.size() + " @: " + index);
        dayItems.remove(index);
        isSaved = false;
        Log.v("Day_list", "Item removed size now: " + dayItems.size());
    }

    void addTodoItem(TodoItem todoItem){
        todoItems.add(todoItem);
        isSaved = false;
    }
    void removeTodoItem(TodoItem todoItem){
        todoItems.remove(todoItem);
        isSaved = false;
    }

    void nullCheck(){
        for(DayItem dayItem : dayItems){
            dayItem.nullCheck();
        }
    }

    void addRegimeDays(Regime regime){
        //TODO make it compatible custom length regime
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        //corrects for java weeks sunday
        int dayOfWeek = startCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6 : startCalendar.get(Calendar.DAY_OF_WEEK) - 2;
        dayItems.addAll(regime.days[dayOfWeek].dayItems);
        regimeDayItems.addAll(regime.days[dayOfWeek].dayItems);
        todoItems.addAll(regime.days[dayOfWeek].todoItems);
        regimeTodoItems.addAll(regime.days[dayOfWeek].todoItems);
        defaultTimeSortDayItems(dayItems);
        isRegimeSet = true;
    }
}
