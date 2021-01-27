package tk.lakatstudio.timeallocator;

public class CycleManager {
    Cycle currentCycle;
    DayItem currentItem;
    int accurateCycles;
    int maxAccurateCycles;

    static ActivityType currentActivityType;

    public static ActivityType getCurrentActivity(){
        return currentActivityType;
    }
}
