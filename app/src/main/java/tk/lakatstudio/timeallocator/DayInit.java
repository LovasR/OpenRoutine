package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Calendar;

public class DayInit {



    @RequiresApi(api = Build.VERSION_CODES.O)
    static void init(Context context){
        Day today;
        today = new Day();

        Calendar date = Calendar.getInstance();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);
        today.start = date.getTime();

        //TODO load from save the daily activities

        //else {
        for(String name : context.getResources().getStringArray(R.array.default_activities)) {
            //ActivityType.userActivityTypes.add(new ActivityType(name, ));
            ActivityType.addActivityType(name, Color.valueOf(Color.parseColor("#3FBF5F")));
            //TODO make compatible with less than 26 API
        }
        //}

        CycleManager.currentDay = today;
        CycleManager.currentItem = today.getDayItem(Calendar.getInstance().getTime());

        /*for(int i = 0; i < 24 * (60 / CycleManager.cycleTime); i++){
            Cycle cycle = new Cycle();
            cycle.index = i;
            cycle.dayItem = today.getDayItem(Calendar.getInstance().getTime());
        }*/

    }
}
