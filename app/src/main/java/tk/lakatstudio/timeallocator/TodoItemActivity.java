package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.graphics.PorterDuff;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TodoItemActivity extends FragmentActivity {

    DayItem assoc;
    TodoItem todoItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todo_item_add);

        //get item for editing
        try{
            Log.v("intent_debug", "" + getIntent().getExtras().getInt("index"));
            todoItem = TodoItem.allTodoItems.get(getIntent().getExtras().getInt("index"));
        } catch (NullPointerException np){
            Log.v("intent_debug", "failed");
        }

        final EditText itemName = findViewById(R.id.addTodoEditName);
        final TextInputLayout itemNameParent = findViewById(R.id.addTodoEditNameLayout);
        itemName.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(i2 != 0 && itemNameParent.getHelperText() != null){
                    itemNameParent.setHelperText(null);
                    itemNameParent.setBoxStrokeColor(getResources().getColor(R.color.color_box_default));
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
                if(CycleManager.currentDay.dayItems.size() == 0){
                    //TODO relay lack of item to associate to user
                    return;
                }

                final AlertDialog.Builder builder = new AlertDialog.Builder(TodoItemActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.day_item_picker_dialog, null);
                ListView pickerList = dialogView.findViewById(R.id.dayItemPickerDialogListview);
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();

                ArrayAdapter<DayItem> adapter = new ArrayAdapter<DayItem>(getBaseContext(), R.layout.dayplanner_item, CycleManager.currentDay.dayItems) {
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        if (convertView == null) {
                            convertView = getLayoutInflater().inflate(R.layout.dayplanner_item, null);
                        }
                        final DayItem dayItem = CycleManager.currentDay.dayItems.get(position);
                        Log.v("save_debug_load", "a" + dayItem.type.name);

                        TextView itemStart = convertView.findViewById(R.id.dayPlannerItemStart);
                        itemStart.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dayItem.start.getTime()));

                        TextView itemLength = convertView.findViewById(R.id.dayPlannerItemLength);
                        //itemLength.setText(lengthAdapter(dayItem.start, dayItem.end));

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
                        assoc = CycleManager.currentDay.dayItems.get(i);

                        if(assoc != null) {
                            selectDayItem.setText(assoc.type.name);
                            Drawable drawable = getDrawable(R.drawable.spinner_background);
                            drawable.setColorFilter(assoc.type.color, PorterDuff.Mode.SRC);
                            selectDayItem.setBackground(drawable);

                            removeDayItem.setVisibility(View.VISIBLE);

                            dayItemTime.setText(getString(R.string.at) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(assoc.start.getTime()));
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
                Drawable drawable = getDrawable(R.drawable.spinner_background);
                drawable.setColorFilter(assoc.type.color, PorterDuff.Mode.SRC);
                selectDayItem.setBackground(drawable);

                removeDayItem.setVisibility(View.VISIBLE);

                dayItemTime.setText(getString(R.string.at) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(assoc.start.getTime()));
                dayItemTime.setVisibility(View.VISIBLE);
            }
        }

        removeDayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeDayItem.setVisibility(View.GONE);
                selectDayItem.setText(R.string.todo_add_time);
                selectDayItem.setBackground(getDrawable(R.drawable.spinner_background));
                dayItemTime.setVisibility(View.GONE);
                assoc = null;
            }
        });


        Button done = findViewById(R.id.addTodoDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = itemName.getText().toString();
                Log.v("debug_", name.length() + " ");
                if(name.length() != 0) {
                    if(todoItem != null){
                        todoItem.name = name;
                        todoItem.dayItem = assoc;
                    } else {
                        TodoItem.addItem(new TodoItem(name, assoc));
                    }
                    finish();
                } else {
                    //itemName.setError();
                    itemNameParent.setHelperText(getString(R.string.todo_set_text_error));
                    itemNameParent.setBoxStrokeColor(getResources().getColor(R.color.color_error));
                }
            }
        });
    }

    /*
    private void setDayItemView(DayItem dayItem){
        selectDayItem.setText(assoc.type.name);
        Drawable drawable = getDrawable(R.drawable.spinner_background);
        drawable.setColorFilter(assoc.type.color, PorterDuff.Mode.SRC);
        selectDayItem.setBackground(drawable);

        removeDayItem.setVisibility(View.VISIBLE);

        dayItemTime.setText(getString(R.string.at) + " " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(assoc.start.getTime()));
        dayItemTime.setVisibility(View.VISIBLE);
    }*/
}
