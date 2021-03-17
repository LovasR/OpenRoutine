package tk.lakatstudio.timeallocator;

import android.util.Log;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class TodoItem {
    String name;
    int dayItemHash;

    @Expose
    DayItem dayItem;


    static ArrayList<TodoItem> allTodoItems = new ArrayList<>();

    TodoItem (String n, DayItem a){
        name = n;
        dayItem = a;
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
