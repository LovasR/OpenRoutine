package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DayItemActivity extends FragmentActivity {

    MaterialTimePicker startTimePicker;
    MaterialTimePicker endTimePicker;

    boolean startPickerClicked = false;
    boolean endPickerClicked = false;

    ActivityType selectedActivity;

    DayItem dayItem;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_item_add);

        selectedActivity = ActivityType.allActivityTypes.get(0);

        final EditText itemName = findViewById(R.id.addDayItemEditName);

        final int startHour;
        final int startMinute;
        final Button startTimeButton = findViewById(R.id.addDayItemStartTime);

        final int endHour;
        final int endMinute;
        final Button endTimeButton = findViewById(R.id.addDayItemEndTime);

        final Button activityButton = findViewById(R.id.addDayItemActivity);

        try{
            Log.v("intent_debug", "" + getIntent().getExtras().getInt("index"));
            dayItem = CycleManager.currentDay.dayItems.get(getIntent().getExtras().getInt("index"));
        } catch (NullPointerException np){
            Log.v("intent_debug", "failed");
        }

        //this if/else sets all the UI elements if the activity was started with the intention of editing
        if(dayItem != null){
            selectedActivity = dayItem.type;

            itemName.setText(dayItem.name);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dayItem.start);
            startHour = calendar.get(Calendar.HOUR_OF_DAY);
            startMinute = calendar.get(Calendar.MINUTE);
            startTimeButton.setText(new SimpleDateFormat("HH:mm").format(calendar.getTime()));

            calendar.setTime(dayItem.end);
            endHour = calendar.get(Calendar.HOUR_OF_DAY);
            endMinute = calendar.get(Calendar.MINUTE);
            endTimeButton.setText(new SimpleDateFormat("HH:mm").format(calendar.getTime()));

            activityButton.setText(dayItem.type.name);
            Drawable drawable = getDrawable(R.drawable.spinner_background);
            assert drawable != null;
            drawable.setColorFilter(dayItem.type.color, PorterDuff.Mode.SRC);
            activityButton.setBackground(drawable);
        } else {
            startHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            startMinute = 0;
            startTimeButton.setText(new SimpleDateFormat("HH:00", Locale.getDefault()).format(Calendar.getInstance().getTime()));

            //TODO set end time to the preferred length of the activityType
            endHour = startHour + 1;
            endMinute = 0;
            final Calendar future = Calendar.getInstance();
            future.set(Calendar.HOUR_OF_DAY, endHour);
            endTimeButton.setText(new SimpleDateFormat("HH:00", Locale.getDefault()).format(future.getTime()));

            activityButton.setText(selectedActivity.name);
            Drawable drawable = getDrawable(R.drawable.spinner_background);
            drawable.setColorFilter(selectedActivity.color, PorterDuff.Mode.SRC);
            activityButton.setBackground(drawable);
        }

        startTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(startHour).setMinute(startMinute).build();


        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimePicker.show(getSupportFragmentManager(), "fragment_tag");
                startPickerClicked = true;
                startTimePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        startTimePicker = new MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(startHour).setMinute(00).build();
                    }
                });
                startTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar startTime = Calendar.getInstance();
                        startTime.set(Calendar.HOUR_OF_DAY, startTimePicker.getHour());
                        startTime.set(Calendar.MINUTE, startTimePicker.getMinute());
                        SimpleDateFormat simple = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        startTimeButton.setText(simple.format(startTime.getTime()));

                        // when user selects first time, the second updates
                        endTimePicker = new MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(startTimePicker.getHour() + 1).setMinute(00).build();
                    }
                });
            }
        });


        endTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(endHour).setMinute(endMinute).build();

        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTimePicker.show(getSupportFragmentManager(), "fragment_tag");
                endPickerClicked = true;
                endTimePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        endTimePicker = new MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(startHour).setMinute(00).build();
                    }
                });
                endTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("timepicker", "positive press");
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(Calendar.HOUR_OF_DAY, endTimePicker.getHour());
                        endTime.set(Calendar.MINUTE, endTimePicker.getMinute());
                        SimpleDateFormat simple = new SimpleDateFormat("HH:mm", Locale.getDefault());
                        endTimeButton.setText(simple.format(endTime.getTime()));
                    }
                });
            }
        });

        activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(DayItemActivity.this);
                View dialogView = getLayoutInflater().inflate(R.layout.activity_picker_dialog, null);
                ListView pickerList = dialogView.findViewById(R.id.activityPickerDialogListview);
                builder.setView(dialogView);
                final AlertDialog alertDialog = builder.create();

                ArrayAdapter<ActivityType> adapter = new ArrayAdapter<ActivityType>(getBaseContext(), R.layout.spinner_item, ActivityType.allActivityTypes){
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        if(convertView==null){
                            convertView = getLayoutInflater().inflate(R.layout.spinner_item, null);
                        }
                        ActivityType activityType = ActivityType.allActivityTypes.get(position);

                        TextView textView = (TextView) convertView;
                        textView.setText(activityType.name);
                        final Drawable textBackground = getDrawable(R.drawable.spinner_background);
                        textBackground.setColorFilter(activityType.color, PorterDuff.Mode.SRC);
                        textView.setBackground(textBackground);

                        return convertView;
                    }
                };
                pickerList.setAdapter(adapter);
                pickerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        selectedActivity = ActivityType.allActivityTypes.get(i);

                        //set activityindicator to newly selected type
                        activityButton.setText(selectedActivity.name);
                        Drawable drawable = getDrawable(R.drawable.spinner_background);
                        drawable.setColorFilter(selectedActivity.color, PorterDuff.Mode.SRC);
                        activityButton.setBackground(drawable);

                        alertDialog.cancel();
                    }
                });

                alertDialog.show();
            }
        });

        final Button done = findViewById(R.id.addDayItemDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //messy code for setting Calendars with the two options being if the user the material dialog or not
                Calendar startTime = Calendar.getInstance();
                if (startPickerClicked) {
                    startTime.set(Calendar.HOUR_OF_DAY, startTimePicker.getHour());
                    startTime.set(Calendar.MINUTE, startTimePicker.getMinute());
                } else {
                    startTime.set(Calendar.HOUR_OF_DAY, startHour);
                    startTime.set(Calendar.MINUTE, startMinute);
                }

                Calendar endTime = (Calendar) startTime.clone();
                if (endPickerClicked) {
                    endTime.set(Calendar.HOUR_OF_DAY, endTimePicker.getHour());
                    endTime.set(Calendar.MINUTE, endTimePicker.getMinute());
                } else {
                    endTime.set(Calendar.HOUR_OF_DAY, endHour);
                    endTime.set(Calendar.MINUTE, endMinute);
                }


                //if it was only started for editing, it only edits existing item
                if (dayItem != null) {
                    dayItem.name = itemName.getText().toString();
                    dayItem.type = selectedActivity;
                    dayItem.start = startTime.getTime();
                    dayItem.end = endTime.getTime();
                } else {
                    CycleManager.currentDay.addDayItem(new DayItem(itemName.getText().toString(), startTime.getTime(), endTime.getTime(), selectedActivity));
                }
                finish();
            }
        });
    }
}
