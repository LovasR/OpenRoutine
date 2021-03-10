package tk.lakatstudio.timeallocator;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainFragment2 extends Fragment {

    ListView todoList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_2, container, false);

        Log.e("UI_test", "oncreateview_fragment2");
        todoList = view.findViewById(R.id.todoListview);
        //todoListInit(this);

        return view;
    }

    @Override
    public void onResume() {
        todoListInit(this);
        super.onResume();
    }

    void todoListInit(final Fragment fragment){

        if(TodoItem.allTodoItems.size() == 0){
            return;
        }
        final ArrayAdapter<TodoItem> arrayAdapter = new ArrayAdapter<TodoItem>(fragment.getContext(), R.layout.todo_item, TodoItem.allTodoItems) {
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.todo_item, null);
                }

                final TodoItem todo = TodoItem.allTodoItems.get(position);

                TextView itemName = convertView.findViewById(R.id.todoItem);
                itemName.setText(todo.name);

                if(todo.dayItem != null) {
                    TextView itemStart = convertView.findViewById(R.id.todoItemStart);
                    itemStart.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(todo.dayItem.start.getTime()));

                    TextView itemType = convertView.findViewById(R.id.todoItemActivity);
                    itemType.setText(todo.dayItem.type.name);
                    final Drawable textBackground = AppCompatResources.getDrawable(fragment.getContext(), R.drawable.spinner_background);
                    textBackground.setColorFilter(todo.dayItem.type.color, PorterDuff.Mode.SRC);
                    itemType.setBackground(textBackground);
                    itemType.setText(todo.dayItem.type.name);
                } else {
                    LinearLayout itemTimes = convertView.findViewById(R.id.todoItemTimes);
                    itemTimes.setVisibility(View.GONE);
                }

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent editIntent = new Intent(fragment.getContext(), TodoItemActivity.class);
                        editIntent.putExtra("index", position);
                        startActivity(editIntent);
                    }
                });

                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        TodoItem.removeItem(todo);
                        notifyDataSetChanged();
                        return false;
                    }
                });


                ImageButton itemCheck = convertView.findViewById(R.id.todoItemCheck);
                itemCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v("__debug", "check clicked");
                        TodoItem.removeItem(todo);
                        notifyDataSetChanged();
                    }
                });

                return convertView;
            }
        };
        todoList.setDividerHeight(10);
        todoList.setAdapter(arrayAdapter);
    }
}
