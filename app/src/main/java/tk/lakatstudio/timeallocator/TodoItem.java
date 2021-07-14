package tk.lakatstudio.timeallocator;

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
        if(item.dayItem == null && item.dayItemID != null){
            item.dayItem = DayInit.currentDayItems.get(item.dayItemID);
        }
        allTodoItems.add(item);
    }

    static void removeItem(TodoItem item){
        allTodoItems.remove(item);
    }
}
