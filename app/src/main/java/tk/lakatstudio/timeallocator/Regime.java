package tk.lakatstudio.timeallocator;

import java.util.ArrayList;

public class Regime {
    String name;
    Day[] days;
    String[] dayNames;

    int index;

    boolean isActive;
    boolean isSaved;
    boolean toDelete;       //when regime is removed it isn`t going to be saved

    static ArrayList<Regime> allRegimes = new ArrayList<Regime>();

    static void addRegime(Regime regime){
        regime.index = allRegimes.size();
        regime.isActive = false;
        regime.toDelete = false;
        allRegimes.add(regime);
    }

    //checks all active regimes to add all dayItems from regime
    static void setAllActiveRegimesDays(Day day){
        for (int i = 0; i < allRegimes.size(); i++) {
            Regime regime = allRegimes.get(i);
            if(regime.isActive) {
                day.addRegimeDays(regime);
            }
        }
    }

    static void removeRegimeDays(Day day){
        //TODO optimize
        for (Regime regime : Regime.allRegimes) {
            if(!regime.isSaved || !regime.isActive){
                day.isRegimeSet = false;
                for(DayItem dayItem : day.regimeDayItems){
                    day.dayItems.remove(dayItem);
                    day.regimeDayItems.remove(dayItem);
                }
                for(TodoItem todoItem : day.regimeTodoItems){
                    day.todoItems.remove(todoItem);
                    day.regimeTodoItems.remove(todoItem);
                }
            }
        }
    }
}
