package tk.lakatstudio.timeallocator;

import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;

public class TodoItem {
    String name;
    int dayItemHash;
    UUID ID;
    UUID dayItemID;

    @DayInit.Exclude
    DayItem dayItem;

    static ArrayList<TodoItem> allTodoItems = new ArrayList<>();

    TodoItem (String name, UUID dayItemID){
        this.name = name;
        //this.dayItem = dayItem;
        this.ID = UUID.randomUUID();
        this.dayItemID = dayItemID;
        if(DayInit.currentDayItems != null){
            this.dayItem = DayInit.currentDayItems.get(dayItemID);
        }
    }

    static void addItem(TodoItem item){
        allTodoItems.add(item);
        if(item.dayItem != null) {
            item.dayItemHash = item.dayItem.hashCode();
            Log.v("hash_debug", item.dayItemHash + "");
        }
    }

    static void removeItem(TodoItem item){
        allTodoItems.remove(item);
    }
}
