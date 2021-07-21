package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainFragment2 extends Fragment {

    TextView noTodoText;
    ListView todoList;
    Day day;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_2, container, false);

        Log.e("UI_test", "oncreateview_fragment2");
        //TextView todoDateText = view.findViewById(R.id.todoDate);
        todoList = view.findViewById(R.id.todoListview);
        noTodoText = view.findViewById(R.id.todoNoTodo);
        //todoListInit(this);

        day = DayInit.daysHashMap.get(MainFragment1.fragmentIndex);

        if(day == null){
            Calendar calendar = Calendar.getInstance();
            day = DayInit.daysHashMap.get(calendar.get(Calendar.YEAR) * 366 + calendar.get(Calendar.DAY_OF_YEAR));
        }

        SpannableString dateText = new SpannableString(new SimpleDateFormat(DayInit.getDateFormat(getContext()), Locale.getDefault()).format(day.start));
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        if(day.dayIndex == (today.get(Calendar.YEAR) * 366 + today.get(Calendar.DAY_OF_YEAR))) {
            dateText.setSpan(new UnderlineSpan(), 0, dateText.length(), 0);
        }
        //todoDateText.setText(dateText);

        return view;
    }

    boolean setDateText(TextView title, ImageButton daySelect, long time, int fragmentIndex, int todayIndex, Context context){
        //TODO settings_todo make this user selectable format
        String dateText = new SimpleDateFormat(DayInit.getDateFormat(context), Locale.getDefault()).format(time);
        if(fragmentIndex == todayIndex){
            Log.v("fragment_date", "underline void");
            //dateText.setSpan(new UnderlineSpan(), 0, dateText.length(), 0);

            Drawable drawable = context.getResources().getDrawable(R.drawable.selected_background);
            assert drawable != null;
            drawable.setColorFilter(context.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC);
            try{
                daySelect.setBackground(drawable);
            } catch (NullPointerException np){
                np.printStackTrace();
            }
        } else {
            try{
                daySelect.setBackground(context.getResources().getDrawable(R.drawable.selected_background));
            } catch (NullPointerException np){
                np.printStackTrace();
            }
        }

        daySelect.setVisibility(View.VISIBLE);

        try {
            title.setText(dateText);
        } catch (NullPointerException np){
            np.printStackTrace();
        }
        return true;
    }

    @Override
    public void onResume() {
        todoListInit(this);
        super.onResume();
    }

    void changeDay(Day day){
        this.day = day;
        todoListInit(this);
    }

    void todoListInit(final Fragment fragment){

        if(day.todoItems.size() == 0){
            noTodoText.setVisibility(View.VISIBLE);
            todoList.setVisibility(View.GONE);
            return;
        } else {
            noTodoText.setVisibility(View.GONE);
            todoList.setVisibility(View.VISIBLE);
        }
        final ArrayList<TodoItem> todoItems = new ArrayList<>(day.todoItems.values());
        final ArrayAdapter<TodoItem> arrayAdapter = new ArrayAdapter<TodoItem>(fragment.getContext(), R.layout.todo_item, todoItems) {
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.todo_item, null);
                }

                Log.v("todo_debug", getItem(position).ID.toString());
                final TodoItem todo = day.todoItems.get(getItem(position).ID);

                TextView itemName = convertView.findViewById(R.id.todoItem);
                itemName.setText(todo.name);

                if(todo.dayItem != null) {
                    TextView itemStart = convertView.findViewById(R.id.todoItemStart);
                    itemStart.setText(new SimpleDateFormat(DayInit.getHourFormat(getContext()), Locale.getDefault()).format(todo.dayItem.start.getTime()));

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
                        editIntent.putExtra("fragmentIndex", MainFragment1.fragmentIndex);
                        editIntent.putExtra("index", position);
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
                                        todoItems.remove(todo);
                                        Log.v("todo_debug", "" + todoItems.size());
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
                        TodoItem.removeItem(todo);
                        day.removeTodoItem(todo);
                        todoItems.remove(todo);
                        notifyDataSetChanged();
                    }
                });

                return convertView;
            }
        };
        todoList.setAdapter(arrayAdapter);
    }
}
