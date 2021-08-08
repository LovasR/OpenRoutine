package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class TodoItemActivity extends FragmentActivity {

    Day day;
    DayItem assoc;
    TodoItem todoItem;

    boolean isRegimeDay = false;
    Regime regime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_item_add);

        //get item for editing
        final int dayIndex = getIntent().getExtras().getInt("fragmentIndex", -1);
        if(dayIndex != -1){
            day = DayInit.daysHashMap.get(dayIndex);
            String todoIDRaw = getIntent().getExtras().getString("ID", "");
            if(todoIDRaw.length() != 0) {
                todoItem = day.todoItems.get(UUID.fromString(todoIDRaw));
            }
        } else {
            String rawRegimeIndex = getIntent().getExtras().getString("regimeIndex", "");
            Log.v("regime_intent_debug", "" + getIntent().getExtras().getInt("regimeIndex", -1));
            int regimeDayIndex = getIntent().getExtras().getInt("regimeDayIndex", -1);
            String regimeTodoIndexRaw = getIntent().getExtras().getString("regimeTodoIndex", "");
            if(rawRegimeIndex.length() > 0) {
                UUID regimeIndex = UUID.fromString(rawRegimeIndex);
                regime = Regime.allRegimes.get(regimeIndex);
                day = regime.days[regimeDayIndex];
                isRegimeDay = true;
                if(regimeTodoIndexRaw.length() > 0){
                    todoItem = day.todoItems.get(UUID.fromString(regimeTodoIndexRaw));
                }
            }
        }
        /*try{
            Log.v("intent_debug", "" + getIntent().getExtras().getInt("index"));
            todoItem = TodoItem.allTodoItems.get(getIntent().getExtras().getInt("index"));
        } catch (NullPointerException np){
            Log.v("intent_debug", "failed");
        }*/

        final EditText itemName = findViewById(R.id.addTodoEditName);
        final TextInputLayout itemNameParent = findViewById(R.id.addTodoEditNameLayout);
        itemName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(i2 != 0 && itemNameParent.getError() != null){
                    itemNameParent.setError(null);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        final ImageButton removeDayItem = findViewById(R.id.addTodoDayItemRemove);

        final TextView dayItemTime = findViewById(R.id.addTodoDayItemTime);

        assoc = null;
        final Button selectDayItem = findViewById(R.id.addTodoDayItem);
        selectDayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(day.dayItems.size() == 0){
                    Toast.makeText(TodoItemActivity.this, R.string.todo_no_day_item, Toast.LENGTH_SHORT).show();
                    return;
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(TodoItemActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.day_item_picker_dialog, null);
                final ListView pickerList = dialogView.findViewById(R.id.dayItemPickerDialogListview);
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();

                alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                final ArrayAdapter<DayItem> adapter = new ArrayAdapter<DayItem>(getBaseContext(), R.layout.dayitem_item, new ArrayList<>(day.dayItems.values())) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getLayoutInflater().inflate(R.layout.dayitem_item, null);
                        }
                        final DayItem dayItem = day.dayItems.get(getItem(position).ID);
                        Log.v("save_debug_load", "a" + dayItem.type.name);

                        TextView itemStart = convertView.findViewById(R.id.dayPlannerItemStart);
                        itemStart.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dayItem.start.getTime()));

                        TextView itemName = convertView.findViewById(R.id.dayPlannerItem);
                        if (dayItem.name.length() != 0) {
                            itemName.setText(dayItem.name);
                            itemName.setVisibility(View.VISIBLE);
                        } else {
                            itemName.setVisibility(View.GONE);
                        }

                        TextView itemType = convertView.findViewById(R.id.dayPlannerItemActivity);
                        itemType.setText(dayItem.type.name);
                        final Drawable textBackground = AppCompatResources.getDrawable(this.getContext(), R.drawable.spinner_background);
                        textBackground.setColorFilter(dayItem.type.color, PorterDuff.Mode.SRC);
                        itemType.setBackground(textBackground);

                        return convertView;
                    }
                };
                pickerList.setAdapter(adapter);
                pickerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        assoc = day.dayItems.get(((DayItem) pickerList.getAdapter().getItem(i)).ID);

                        if(assoc != null) {
                            selectDayItem.setText(assoc.type.name);
                            Drawable drawable = AppCompatResources.getDrawable(TodoItemActivity.this, R.drawable.spinner_background);
                            drawable.setColorFilter(assoc.type.color, PorterDuff.Mode.SRC);
                            selectDayItem.setBackground(drawable);

                            removeDayItem.setVisibility(View.VISIBLE);

                            dayItemTime.setText(getString(R.string.at_colon) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(assoc.start.getTime()));
                            dayItemTime.setVisibility(View.VISIBLE);
                        }
                        alertDialog.cancel();
                    }
                });

                alertDialog.show();
            }
        });

        if(todoItem != null){
            itemName.setText(todoItem.name);
            if(todoItem.dayItem != null){
                assoc = todoItem.dayItem;
                selectDayItem.setText(assoc.type.name);
                Drawable drawable = AppCompatResources.getDrawable(TodoItemActivity.this, R.drawable.spinner_background);
                drawable.setColorFilter(assoc.type.color, PorterDuff.Mode.SRC);
                selectDayItem.setBackground(drawable);

                removeDayItem.setVisibility(View.VISIBLE);

                dayItemTime.setText(getString(R.string.at_colon) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(assoc.start.getTime()));
                dayItemTime.setVisibility(View.VISIBLE);
            }
        }

        removeDayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeDayItem.setVisibility(View.GONE);
                selectDayItem.setText(R.string.todo_add_time);
                selectDayItem.setBackground(AppCompatResources.getDrawable(TodoItemActivity.this, R.drawable.spinner_background));
                dayItemTime.setVisibility(View.GONE);
                assoc = null;
            }
        });


        Button done = findViewById(R.id.addTodoDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = itemName.getText().toString();
                if(name.length() != 0) {
                    if(todoItem != null){
                        todoItem.name = name;
                        todoItem.dayItem = assoc;
                        todoItem.dayItemID = (assoc == null ? null : assoc.ID);
                    } else {
                        //if associated dayItem isn`t set, random uuid
                        TodoItem todoItem = new TodoItem(name,
                                (assoc == null ? null : assoc.ID));
                        TodoItem.addItem(todoItem);
                        day.addTodoItem(todoItem);
                        if(isRegimeDay){
                            regime.todoItemsAdd.add(todoItem);
                        }
                    }
                    finish();
                } else {
                    itemNameParent.setError(getString(R.string.todo_set_text_error));
                }
            }
        });
    }

    /*
    private void setDayItemView(DayItem dayItem){
        selectDayItem.setText(assoc.type.name);
        Drawable drawable = AppCompatResources.getDrawable(TodoItemActivity.this, R.drawable.spinner_background);
        drawable.setColorFilter(assoc.type.color, PorterDuff.Mode.SRC);
        selectDayItem.setBackground(drawable);

        removeDayItem.setVisibility(View.VISIBLE);

        dayItemTime.setText(getString(R.string.at) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(assoc.start.getTime()));
        dayItemTime.setVisibility(View.VISIBLE);
    }*/
}
