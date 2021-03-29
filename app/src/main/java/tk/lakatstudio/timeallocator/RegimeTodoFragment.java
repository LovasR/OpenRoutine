package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

public class RegimeTodoFragment extends Fragment {

    TextView todoDateText;

    TextView noTodoText;
    ListView todoList;
    Day day;
    Regime regime;
    int dayIndex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_2, container, false);

        Log.e("UI_test", "oncreateview_fragment2");
        todoDateText = view.findViewById(R.id.todoDate);
        todoList = view.findViewById(R.id.todoListview);
        noTodoText = view.findViewById(R.id.todoNoTodo);

        todoDateText.setText(regime.dayNames[dayIndex]);
        todoDateText.setVisibility(View.VISIBLE);
        View line = ((ViewGroup) view).getChildAt(((ViewGroup) view).indexOfChild(todoDateText) + 1);
        line.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onResume() {
        todoListInit(this);
        super.onResume();
    }

    void refreshFragment(){
        todoDateText.setText(regime.dayNames[dayIndex]);
        todoListInit(this);
    }

    void todoListInit(final Fragment fragment){

        if(day.todoItems.size() == 0){
            noTodoText.setVisibility(View.VISIBLE);
            todoList.setAdapter(null);
            return;
        } else {
            noTodoText.setVisibility(View.GONE);
        }

        final ArrayAdapter<TodoItem> arrayAdapter = new ArrayAdapter<TodoItem>(fragment.getContext(), R.layout.todo_item, day.todoItems) {
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.todo_item, null);
                }

                final TodoItem todo = day.todoItems.get(position);

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
                        editIntent.putExtra("regimeIndex", regime.index);
                        editIntent.putExtra("regimeDayIndex", dayIndex);
                        editIntent.putExtra("regimeTodoIndex", position);
                        startActivity(editIntent);
                    }
                });

                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        day.removeTodoItem(todo);
                                        notifyDataSetChanged();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                        builder.setMessage(getString(R.string.remove_activity,
                                getString(R.string.todo_singular))).setPositiveButton(getString(R.string.yes),
                                dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
                        return false;
                    }
                });


                ImageButton itemCheck = convertView.findViewById(R.id.todoItemCheck);
                itemCheck.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.v("__debug", "check clicked");
                        //TodoItem.removeItem();
                        day.removeTodoItem(todo);
                        notifyDataSetChanged();
                    }
                });

                return convertView;
            }
        };
        todoList.setAdapter(arrayAdapter);
    }
}
