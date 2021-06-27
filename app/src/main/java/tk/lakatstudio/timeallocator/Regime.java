package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

import tk.lakatstudio.timeallocator.DayInit.Exclude;

//import com.google.gson.annotations.Exclude;

public class Regime {
    String name;
    Day[] days;
    String[] dayNames;

    int index;
    UUID ID;

    boolean isActive;
    boolean isSaved;
    boolean toDelete;       //when regime is removed it isn`t going to be saved


    static HashMap<UUID, Regime> allRegimes = new HashMap<>();

    //TODO try to refresh days from regime

    @Exclude
    HashMap<Integer, Day> appliedDays = new HashMap<>();

    //change lists
    @Exclude
    ArrayList<DayItem>[] dayItemsChange;
    @Exclude
    ArrayList<Integer>[] changeCodeList;



    /*@Exclude
    ArrayList<DayItem> dayItemsAdd = new ArrayList<>();*/
    @Exclude
    ArrayList<UUID> dayItemsRemove = new ArrayList<>();
    @Exclude
    ArrayList<TodoItem> todoItemsAdd = new ArrayList<>();
    @Exclude
    ArrayList<UUID> todoItemsRemove = new ArrayList<>();

    private final static int DEFAULT_DAYS_IN_WEEK = 7;

    private final static int CODE_ADD = 1;
    private final static int CODE_CHANGE = 2;
    private final static int CODE_REMOVE = 3;

    Regime (String[] dayNames){
        this.ID = UUID.randomUUID();
        this.dayNames = dayNames;
        this.days = new Day[DEFAULT_DAYS_IN_WEEK];//{new Day(true), new Day(true), new Day(true), new Day(true), new Day(true), new Day(true), new Day(true)};
        for(int i = 0; i < DEFAULT_DAYS_IN_WEEK; i++){
            this.days[i] = new Day(true, i);
        }
        this.dayItemsChange = new ArrayList[DEFAULT_DAYS_IN_WEEK];
        this.changeCodeList = new ArrayList[DEFAULT_DAYS_IN_WEEK];
        for(int i = 0; i < DEFAULT_DAYS_IN_WEEK; i++){
            this.dayItemsChange[i] = new ArrayList<>();
            this.changeCodeList[i] = new ArrayList<>();
        }
        this.isActive = false;
        this.toDelete = false;
    }

    static void addRegime(Regime regime){
        regime.index = allRegimes.size();
        //regime.dayItemsAdd = new ArrayList<>();
        //regime.dayItemsRemove = new ArrayList<>();
        allRegimes.put(regime.ID, regime);

        regime.dayItemsChange = new ArrayList[DEFAULT_DAYS_IN_WEEK];
        regime.changeCodeList = new ArrayList[DEFAULT_DAYS_IN_WEEK];
        for(int i = 0; i < DEFAULT_DAYS_IN_WEEK; i++){
            regime.dayItemsChange[i] = new ArrayList<>();
            regime.changeCodeList[i] = new ArrayList<>();
        }

        regime.todoItemsAdd = new ArrayList<>();
        regime.todoItemsRemove = new ArrayList<>();

        Log.v("regime_null", "added regime: " + Regime.allRegimes.toString());
    }

    //there are by default 7 lists in a changes list for changes by day

    //adds an add request
    void addDayItem(DayItem dayItem, int dayOfWeek){
        int index;
        if((index = dayItemsChange[dayOfWeek].indexOf(dayItem)) >= 0){
            dayItemsChange[dayOfWeek].set(index, dayItem);
        } else {
            dayItemsChange[dayOfWeek].add(dayItem);
            changeCodeList[dayOfWeek].add(CODE_ADD);
        }
    }

    //adds a change request
    void changeDayItem(DayItem dayItem, int dayOfWeek){
        int index;
        if((index = dayItemsChange[dayOfWeek].indexOf(dayItem)) >= 0){
            dayItemsChange[dayOfWeek].set(index, dayItem);
        } else {
            dayItemsChange[dayOfWeek].add(dayItem);
            changeCodeList[dayOfWeek].add(CODE_CHANGE);
        }
    }

    //removes add request if there is one, and adds a remove request
    void removeDayItem(DayItem dayItem, int dayOfWeek){
        int index;
        if((index = dayItemsChange[dayOfWeek].indexOf(dayItem)) >= 0){
            dayItemsChange[dayOfWeek].remove(index);
            int r = changeCodeList[dayOfWeek].remove(index);
            Log.v("dayChangeCommit", "removeDayOtem\t@: " + index + " code: " + r);
            if(r == CODE_ADD){
                return;
            }
        }

        dayItemsChange[dayOfWeek].add(dayItem);
        changeCodeList[dayOfWeek].add(CODE_REMOVE);
    }

    void nullCheck(){
        if(appliedDays == null){
            appliedDays = new HashMap<>();
        }
    }

    //TODO multi-thread
    void refreshDays(Context context){
        Log.v("set_RMS_pre", "first day: " + (appliedDays.values().size() > 0 ? ((Day)appliedDays.values().toArray()[0]).dayIndex : "null"));
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        for(Day day : appliedDays.values()){
            calendar.setTimeInMillis(day.start.getTime());
            int dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2);

            Log.v("regime_refresh", "dayOfWeek: " + dayOfWeek + " index: " + day.dayIndex + " size: " + dayItemsChange[dayOfWeek].size());
            for(int i = 0; i < dayItemsChange[dayOfWeek].size(); i++){
                DayItem dayItem = dayItemsChange[dayOfWeek].get(i);

                switch(changeCodeList[dayOfWeek].get(i)){
                    case CODE_ADD:
                        Log.v("regime_refresh", "CODE_ADD");
                        Log.v("regime_refresh", "before_dayItem: " + dayItem.toString());
                        DayItem nDayItem = DayItem.clone(dayItem);
                        Log.v("regime_refresh", "after_dayItem: " + nDayItem.toString());
                        Log.v("regime_refresh", "after_time" + nDayItem.start.getTime());
                        day.dayItems.put(nDayItem.ID, nDayItem);
                        day.addRegimeNotifications(context, this);
                        day.regimeDayItems.put(nDayItem.ID, nDayItem);
                        break;
                    case CODE_CHANGE:
                        Log.v("regime_refresh", "CODE_CHANGE");
                        DayItem cDayItem = DayItem.clone(dayItem);
                        Log.v("regime_refresh", "after_dayItem: " + cDayItem.toString());
                        Log.v("regime_refresh", "after_time" + cDayItem.start.getTime());
                        day.dayItems.put(cDayItem.ID, cDayItem);
                        day.addRegimeNotifications(context, this);
                        day.regimeDayItems.put(cDayItem.ID, cDayItem);
                        break;
                    case CODE_REMOVE:
                        Log.v("regime_refresh", "CODE_REMOVE");
                        Log.v("regime_refresh", "dayItemID: " + dayItem.ID.toString() + " " + day.dayItems.size() + " " + day.dayItems + " " + (day.dayItems.get(dayItem.ID) != null ? day.dayItems.get(dayItem.ID) : "null"));
                        DayItem rDayItem = day.dayItems.get(dayItem.ID);
                        if(rDayItem != null) {
                            Log.v("regime_refresh", "dayItem was applied");
                            rDayItem.removeNotifications(context, day, false);
                        }
                        day.dayItems.remove(dayItem.ID);
                        day.regimeDayItems.remove(dayItem.ID);

                        days[dayOfWeek].dayItems.remove(dayItem.ID);
                        break;
                    default:
                        break;
                }
            }
            for(TodoItem todoItem : todoItemsAdd){
                day.todoItems.put(todoItem.ID, todoItem);
            }
            for(UUID ID : todoItemsRemove){
                day.todoItems.remove(ID);
            }

            if(day.dayIndex == MainFragment1.todayIndex){
                Log.v("regime_RMS_add", "it is Today: " + day.dayIndex + " " + MainFragment1.todayIndex);
                day.addRegimeRemoteNotificationTimes(context, days[dayOfWeek]);
            } else {
                Log.v("regime_RMS_add", "it is not Today: " + day.dayIndex + " " + MainFragment1.todayIndex);
            }
        }
        Log.v("regime_refresh", "refreshed: " + appliedDays.size());

        //changes have been applied, reset change lists
        this.dayItemsChange = new ArrayList[DEFAULT_DAYS_IN_WEEK];
        this.changeCodeList = new ArrayList[DEFAULT_DAYS_IN_WEEK];
        for(int i = 0; i < DEFAULT_DAYS_IN_WEEK; i++){
            this.dayItemsChange[i] = new ArrayList<>();
            this.changeCodeList[i] = new ArrayList<>();
        }
    }

    //checks all active regimes to add all dayItems from regime
    static void setAllActiveRegimesDays(Context context, Day day){
        Log.v("regime_null", "setAllActiveRegimesDays");
        for (Regime regime : Regime.allRegimes.values()) {
            Log.v("regime_null", "regime.isActive: " + regime.isActive);
            //TODO calculate if its on schedule here
            if(regime.isActive && (day.setRegimes.get(regime.ID) == null)) {
                day.addRegimeDays(context, regime);
                day.setRegimes.put(regime.ID, regime);
                regime.appliedDays.put(day.dayIndex, day);
            }
        }
    }

    static void removeRegimeDays(Context context, Day day){
        //TODO optimize
        for (Regime regime : Regime.allRegimes.values()) {
            if(!regime.isSaved || !regime.isActive){
                removeRegimeItems(context, day);
            }
        }
    }

    void deleteItems(Context context){
        for(Day day : appliedDays.values()) {
            removeRegimeItems(context, day);
        }
    }

    static void removeRegimeItems(Context context, Day day){
        day.isRegimeSet = false;
        for (DayItem dayItem : new ArrayList<>(day.regimeDayItems.values())) {
            day.dayItems.remove(dayItem.ID);
            day.regimeDayItems.remove(dayItem.ID);
            dayItem.removeNotifications(context, day, false);
        }
        for (TodoItem todoItem : day.regimeTodoItems.values()) {
            day.todoItems.remove(todoItem.ID);
            day.regimeTodoItems.remove(todoItem.ID);
        }
    }
}
