package tk.lakatstudio.timeallocator;

import java.util.Calendar;

public class DayInit {
    void init(){
        Day today;
        today = new Day();

        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        today.start = date.getTime();

        //TODO load from save the daily activities



        for(int i = 0; i < 24 * (60 / CycleManager.cycleTime); i++){
            Cycle cycle = new Cycle();
            cycle.index = i;
            cycle.dayItem = today.getDayItem(Calendar.getInstance().getTime());
        }

    }
}
