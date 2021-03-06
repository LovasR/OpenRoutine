package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

    boolean isSaved;
    boolean toDelete;       //when regime is removed it isn`t going to be saved


    static HashMap<UUID, Regime> allRegimes = new HashMap<>();

    @Exclude
    HashMap<Integer, Day> appliedDays = new HashMap<>();

    //change lists
    @Exclude
    ArrayList<DayItem>[] dayItemsChange;
    @Exclude
    ArrayList<Integer>[] changeCodeList;
    //scheduleItems hold the planned on/off states of the regime
    class ScheduleItem{

        //Regime IS applied on start day and end day
        //if start is 0, it means until end
        Date start;
        //if end is 0, it means regime is active forever
        Date end;
        boolean isActive;
        UUID ID;
        ScheduleItem (Date start, Date end, boolean isActive){
            ID = UUID.randomUUID();
            this.start = start;
            this.end = end;
            this.isActive = isActive;
        }
    }
    ArrayList<ScheduleItem> schedule;
    @Exclude
    ScheduleItem lastAdded;

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
        this.schedule = new ArrayList<>();
        //default scheduleItem, on forever
        this.schedule.add(new ScheduleItem(new Date(0), new Date(0), true));
        Log.v("scheduleItem_debug", "" + schedule.toString());

        this.dayItemsChange = new ArrayList[DEFAULT_DAYS_IN_WEEK];
        this.changeCodeList = new ArrayList[DEFAULT_DAYS_IN_WEEK];
        for(int i = 0; i < DEFAULT_DAYS_IN_WEEK; i++){
            this.dayItemsChange[i] = new ArrayList<>();
            this.changeCodeList[i] = new ArrayList<>();
        }
        //this.isActive = false;
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

    //boolean isActive;
    boolean isActive(long date){
        //checks schedule
        //default is false, gets the forever item of schedule
        boolean foreverStatus = false;
        for(ScheduleItem scheduleItem : schedule){
            if(date >= scheduleItem.start.getTime() && date <= scheduleItem.end.getTime()){
                return scheduleItem.isActive;
            } else if(scheduleItem.end.getTime() == 0){
                foreverStatus = scheduleItem.isActive;
            }
        }
        return foreverStatus;
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
        if(schedule == null){
            schedule = new ArrayList<>();
            schedule.add(new ScheduleItem(new Date(System.currentTimeMillis()), new Date(0), true));
        }
    }
    //TODO multi-thread

    void refreshDays(Context context){
        //Log.v("set_RMS_pre", "first day: " + (appliedDays.values().size() > 0 ? ((Day)appliedDays.values().toArray()[0]).dayIndex : "null"));
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

    void refreshRegimesDays(Context context, ScheduleItem scheduleItem){
        for(Day day : appliedDays.values()){
            if(day.start.getTime() >= scheduleItem.start.getTime() && day.start.getTime() <= scheduleItem.end.getTime()){
                //simple scheduleItem
                refreshRegimeDaysRefresh(context, day, scheduleItem);
            } else if(scheduleItem.end.getTime() == 0) {
                //if default status is changed
                boolean isPlanned = false;
                //check if day is in the schedule
                for(ScheduleItem scheduleItemI : schedule){
                    if(day.start.getTime() >= scheduleItemI.start.getTime() && day.start.getTime() <= scheduleItemI.end.getTime()){
                        isPlanned = true;
                    }
                }
                //if it isn't then it falls under the default status, apply
                if(!isPlanned){
                    refreshRegimeDaysRefresh(context, day, scheduleItem);
                }
            }
        }
    }
    void refreshRegimeDaysRefresh(Context context, Day day, ScheduleItem scheduleItem){
        if(scheduleItem.isActive && !day.setRegimes.contains(ID)){
            day.addRegimeDays(context, this);
        } else if(!scheduleItem.isActive && day.setRegimes.contains(ID)) {
            removeRegimeItems(context, day);
            day.setRegimes.remove(ID);
        }
    }

    //checks all active regimes to add all dayItems from regime
    static void setAllActiveRegimesDays(Context context, Day day){
        for (Regime regime : Regime.allRegimes.values()) {
            if(regime.isActive(day.start.getTime()) && !day.setRegimes.contains(regime.ID)) {
                day.addRegimeDays(context, regime);
                day.setRegimes.add(regime.ID);
                regime.appliedDays.put(day.dayIndex, day);
            }
        }
    }

    static void removeRegimeDays(Context context, Day day){
        //TODO optimize
        for (Regime regime : Regime.allRegimes.values()) {
            if(!regime.isSaved || !regime.isActive(day.start.getTime())){
                removeRegimeItems(context, day);
                day.setRegimes.remove(regime.ID);
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

    ScheduleItem addScheduleItem(Date start, Date end, boolean isActive){
        ScheduleItem newScheduleItem = new ScheduleItem(start, end, isActive);
        schedule.add(newScheduleItem);
        defaultTimeSortSchedule(schedule);
        lastAdded = newScheduleItem;
        return newScheduleItem;
    }

    static ArrayList<ScheduleItem> defaultTimeSortSchedule(ArrayList<ScheduleItem> schedule){
        Collections.sort(schedule, new Comparator<ScheduleItem>() {
            @Override
            public int compare(ScheduleItem item1, ScheduleItem item2) {
                return Long.compare(item1.start.getTime(), item2.start.getTime());
            }
        });
        return schedule;
    }

    ArrayList<ScheduleItem> checkOverlap(long start, long end){
        //returns list with scheduleItems whose end time conflict
        ArrayList<ScheduleItem> outSchedule = new ArrayList<>();
        ScheduleItem lastItem = null;
        Log.v("scheduleItem_debug_chck", "schedule_size: " + schedule.size());
        for(ScheduleItem scheduleItem : schedule){
            if(lastItem == null){
                lastItem = scheduleItem;
                continue;
            }
            Log.v("scheduleItem_debug_chck", "" + lastItem.end.getTime() + " " + scheduleItem.start.getTime());
            //check for conflict
            if(lastItem.end.getTime() > scheduleItem.start.getTime()){
                outSchedule.add(lastItem);
            }
            lastItem = scheduleItem;
        }
        if(outSchedule.size() == 0){
            return null;
        } else {
            return outSchedule;
        }
    }
    boolean checkOverlapL(long start, long end){
        ScheduleItem lastItem = null;
        for(Regime.ScheduleItem scheduleItem : schedule){
            if(lastItem == null){
                if(scheduleItem.start.getTime() < start) {
                    lastItem = scheduleItem;
                    continue;
                }
                lastItem = scheduleItem;
                continue;
            }
            if(start > lastItem.start.getTime() && start < scheduleItem.start.getTime() && end > scheduleItem.start.getTime()){
                return true;
            }
            if(start > scheduleItem.start.getTime() && start <= scheduleItem.end.getTime()){
                return true;
            }
            lastItem = scheduleItem;
        }
        return false;
    }
    boolean checkOverlapSI(ScheduleItem scheduleItemIn){
        ScheduleItem lastItem = null;
        for(Regime.ScheduleItem scheduleItem : schedule){
            if(lastItem == null){
                if(scheduleItem.start.getTime() < scheduleItemIn.start.getTime()) {
                    lastItem = scheduleItem;
                    continue;
                }
                lastItem = scheduleItem;
                continue;
            }
            if(scheduleItem == scheduleItemIn){
                lastItem = scheduleItem;
                continue;
            }
            if(scheduleItemIn.start.getTime() > lastItem.start.getTime() && scheduleItemIn.start.getTime() < scheduleItem.start.getTime() && scheduleItemIn.end.getTime() > scheduleItem.start.getTime()){
                return true;
            }
            if(scheduleItemIn.start.getTime() > scheduleItem.start.getTime() && scheduleItemIn.start.getTime() <= scheduleItem.end.getTime()){
                return true;
            }
            lastItem = scheduleItem;
        }
        return false;
    }
    void resolveConflicts(){
        if(lastAdded == null){
            return;
        }
        //TODO ohno
        ScheduleItem lastItem = null;
        ScheduleItem lastAddedLocal = lastAdded;
        ArrayList<ScheduleItem> scheduleLocal = (ArrayList<ScheduleItem>) schedule.clone();
        for(ScheduleItem scheduleItem : scheduleLocal){
            if(lastItem == null){
                lastItem = scheduleItem;
                continue;
            }
            //if(scheduleItem == lastAdded) continue;
            /*if(lastAddedLocal.start.getTime() >= scheduleItem.start.getTime() &&
                    (lastAddedLocal.end.getTime() <= scheduleItem.end.getTime() || scheduleItem.end.getTime() == 0)){
                //handles new scheduleItem in the middle of existing scheduleItem
                if(scheduleItem.start.before(lastAddedLocal.start)){
                    //make new scheduleItem and cut the later one down to size
                    addScheduleItem(scheduleItem.start, lastAddedLocal.start, scheduleItem.isActive);
                    scheduleItem.start = lastAddedLocal.end;
                } else if(scheduleItem.start.equals(lastAddedLocal.start)){
                    scheduleItem.start = lastAddedLocal.end;
                } else if(scheduleItem.end.equals(lastAddedLocal.end) && !scheduleItem.start.equals(lastAddedLocal.start)){

                }
            }*/
/*
            if(lastAddedLocal.start.getTime() >= scheduleItem.start.getTime() &&
                    (lastAddedLocal.end.getTime() <= scheduleItem.end.getTime() || scheduleItem.end.getTime() == 0)){
                if(lastAddedLocal.start.getTime() == scheduleItem.start.getTime()){
                    if(lastAddedLocal.end.getTime() < scheduleItem.end.getTime()){
                        //see 1.1
                        scheduleItem.start = (Date) lastAddedLocal.end.clone();
                    } else if(lastAddedLocal.end.getTime() >= scheduleItem.end.getTime()){
                        //TODO figure out how to remove from schedule multiple times
                        //schedule.remove(scheduleLocal.indexOf(scheduleItem));
                        //see 1.2, 1.3
                    }
                } else if(lastAddedLocal.start.getTime() < scheduleItem.start.getTime()){
                    if(lastAddedLocal.end.getTime() < scheduleItem.end.getTime()){
                        //see 2.1
                        scheduleItem.start = (Date) lastAddedLocal.end.clone();
                    } else if(lastAddedLocal.end.getTime() == scheduleItem.end.getTime()){
                        //see 2.2
                    }
                }
            }*/

            if(lastItem.end.getTime() >= scheduleItem.start.getTime()){
                Log.v("schedule_overlap_corr", "overlap");
                if(lastItem.isActive == scheduleItem.isActive){
                    //merge
                    scheduleItem.start.setTime(lastItem.start.getTime());
                    schedule.remove(lastItem);
                    continue;
                }
                Log.v("schedule_overlap_corr", "start_cut");
                if(lastItem.end.getTime() >= scheduleItem.end.getTime()){
                    //remove this
                    schedule.remove(scheduleItem);
                    Log.v("schedule_overlap_corr", "remove");
                } else if(lastItem.end.getTime() == scheduleItem.start.getTime()) {
                    //if only one day is the overlap than add a day
                    scheduleItem.start.setTime(scheduleItem.start.getTime() + (24 * 60 * 60 * 1000));
                    lastItem = scheduleItem;
                    Log.v("schedule_overlap_corr", "start_plus_day");
                } else {
                    scheduleItem.start.setTime(lastItem.end.getTime() + (24 * 60 * 60 * 1000));
                    lastItem = scheduleItem;
                }
            } else if (lastItem.start.getTime() == scheduleItem.start.getTime()){
                Log.v("schedule_overlap_corr", "same_start");
                if(lastItem.end.getTime() > scheduleItem.end.getTime()){
                    lastItem.start.setTime(scheduleItem.end.getTime() + (24 * 60 * 60 * 1000));
                } else if(lastItem.end.getTime() == scheduleItem.end.getTime()){
                    schedule.remove(scheduleItem);
                } else {
                    scheduleItem.start.setTime(lastItem.end.getTime() + (24 * 60 * 60 * 1000));
                }
                lastItem = scheduleItem;
            } else {
                Log.v("schedule_overlap_corr", "no_correction");
                lastItem = scheduleItem;
            }
        }
    }
}
