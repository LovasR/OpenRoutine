package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
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


    int startHour;
    int startMinute;

    int endHour;
    int endMinute;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_item_add);

        selectedActivity = ActivityType.allActivityTypes.get(0);

        final EditText itemName = findViewById(R.id.addDayItemEditName);

        final Button startTimeButton = findViewById(R.id.addDayItemStartTime);
        int setStartHour;

        final Button endTimeButton = findViewById(R.id.addDayItemEndTime);

        final Button activityButton = findViewById(R.id.addDayItemActivity);

        final int focusedFragment = getIntent().getExtras().getInt("fragmentIndex", -1);
        final Day focusedDay;
        if(focusedFragment != -1){
            focusedDay = DayInit.daysHashMap.get(focusedFragment);
        } else {
            int regimeIndex = getIntent().getExtras().getInt("regimeIndex", -1);
            int regimeDayIndex = getIntent().getExtras().getInt("regimeDayIndex", -1);
            Log.v("regime_intent_debug", "" + getIntent().getExtras().getInt("regimeIndex", -1));
            if(regimeIndex != -1){
                focusedDay = Regime.allRegimes.get(regimeIndex).days[regimeDayIndex];
            } else {
                focusedDay = new Day();
            }
        }

        Log.v("intent_debug", "index: " + getIntent().getExtras().getInt("index", -1) + " fragmentIndex: " + focusedFragment);
        int dayItemIndex = getIntent().getExtras().getInt("index", -1);
        if(dayItemIndex >= 0) {
            dayItem = focusedDay.dayItems.get(dayItemIndex);
        }

        //this if/else sets all the UI elements if the activity was started with the intention of editing
        if(dayItem != null){
            selectedActivity = dayItem.type;

            itemName.setText(dayItem.name);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dayItem.start);
            startHour = calendar.get(Calendar.HOUR_OF_DAY);
            startMinute = calendar.get(Calendar.MINUTE);
            startTimeButton.setText(new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault()).format(calendar.getTime()));

            calendar.setTime(dayItem.end);
            endHour = calendar.get(Calendar.HOUR_OF_DAY);
            endMinute = calendar.get(Calendar.MINUTE);
            endTimeButton.setText(new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault()).format(calendar.getTime()));

            activityButton.setText(dayItem.type.name);
            Drawable drawable = getDrawable(R.drawable.spinner_background);
            assert drawable != null;
            drawable.setColorFilter(dayItem.type.color, PorterDuff.Mode.SRC);
            activityButton.setBackground(drawable);
        } else {
            Calendar calendar = Calendar.getInstance();
            startHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            startMinute = 0;
            calendar.set(Calendar.MINUTE, startMinute);
            startTimeButton.setText(new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault()).format(calendar.getTime()));

            //TODO set end time to the preferred length of the activityType
            endHour = startHour + (selectedActivity.preferredLength > 0 ? selectedActivity.preferredLength / 60 : 1);
            endMinute = (selectedActivity.preferredLength > 0 ? selectedActivity.preferredLength % 60 : 0);
            final Calendar future = Calendar.getInstance();
            future.set(Calendar.HOUR_OF_DAY, endHour);
            future.set(Calendar.MINUTE, endMinute);
            endTimeButton.setText(new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault()).format(future.getTime()));

            activityButton.setText(selectedActivity.name);
            Drawable drawable = getDrawable(R.drawable.spinner_background);
            drawable.setColorFilter(selectedActivity.color, PorterDuff.Mode.SRC);
            activityButton.setBackground(drawable);
        }

        startTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(DayInit.getMaterialTimeFormat(getBaseContext()))
                .setHour(startHour).setMinute(startMinute).build();


        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimePicker.show(getSupportFragmentManager(), "fragment_tag");
                startPickerClicked = true;
                startTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Calendar startTime = Calendar.getInstance();
                        startTime.set(Calendar.HOUR_OF_DAY, startTimePicker.getHour());
                        startTime.set(Calendar.MINUTE, startTimePicker.getMinute());
                        SimpleDateFormat simple = new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault());
                        startTimeButton.setText(simple.format(startTime.getTime()));
                        startHour = startTime.get(Calendar.HOUR_OF_DAY);
                        startMinute = startTime.get(Calendar.MINUTE);

                        endHour = startHour + (selectedActivity.preferredLength > 0 ? selectedActivity.preferredLength / 60 : 1);
                        endMinute = (selectedActivity.preferredLength > 0 ? selectedActivity.preferredLength % 60 : 0);

                        startTime.set(Calendar.HOUR_OF_DAY, endHour);
                        startTime.set(Calendar.MINUTE, endMinute);
                        SimpleDateFormat simpleEnd = new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault());
                        endTimeButton.setText(simpleEnd.format(startTime.getTime()));

                        // when user selects first time, the second updates
                        endTimePicker = new MaterialTimePicker.Builder()
                                .setTimeFormat(DayInit.getMaterialTimeFormat(getBaseContext()))
                                .setHour(endHour).setMinute(endMinute).build();
                    }
                });
            }
        });


        endTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(DayInit.getMaterialTimeFormat(getBaseContext()))
                .setHour(endHour).setMinute(endMinute).build();

        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTimePicker.show(getSupportFragmentManager(), "fragment_tag");
                endPickerClicked = true;
                endTimePicker.addOnPositiveButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("timepicker", "positive press");
                        Calendar endTime = Calendar.getInstance();
                        endTime.set(Calendar.HOUR_OF_DAY, endTimePicker.getHour());
                        endTime.set(Calendar.MINUTE, endTimePicker.getMinute());
                        SimpleDateFormat simple = new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault());
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

                        if(selectedActivity.preferredLength > 0){
                            int lengthPreferred = selectedActivity.preferredLength;
                            int endHourPreferred = startHour + (lengthPreferred > 59 ? lengthPreferred / 60 : 0);
                            int endMinutePreferred = lengthPreferred % 60;
                            final Calendar future = Calendar.getInstance();
                            future.set(Calendar.HOUR_OF_DAY, endHourPreferred);
                            future.set(Calendar.MINUTE, endMinutePreferred);
                            endTimeButton.setText(new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault()).format(future.getTime()));
                            endTimePicker = new MaterialTimePicker.Builder()
                                    .setTimeFormat(DayInit.getMaterialTimeFormat(getBaseContext()))
                                    .setHour(endHourPreferred).setMinute(endMinutePreferred).build();
                        }

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
                Calendar endTime = (Calendar) startTime.clone();
                try {
                    if (startPickerClicked && startTimePicker != null) {
                        startHour = startTimePicker.getHour();
                        startMinute = startTimePicker.getMinute();
                    }
                    startTime.set(Calendar.HOUR_OF_DAY, startHour);
                    startTime.set(Calendar.MINUTE, startMinute);

                    if (endPickerClicked && endTimePicker != null) {
                        endHour = endTimePicker.getHour();
                        endMinute = endTimePicker.getMinute();
                    }
                    endTime.set(Calendar.HOUR_OF_DAY, endHour);
                    endTime.set(Calendar.MINUTE, endMinute);
                } catch (Exception e) {
                    //damn
                }

                //if it was only started for editing, it only edits existing item
                if (dayItem != null) {
                    dayItem.name = itemName.getText().toString();
                    dayItem.type = selectedActivity;
                    dayItem.start = startTime.getTime();
                    dayItem.end = endTime.getTime();
                } else {
                    Log.v("fragment_preload", "focusedFragment: "  + focusedFragment);
                    focusedDay.addDayItem(new DayItem(itemName.getText().toString(), startTime.getTime(), endTime.getTime(), selectedActivity));
                    //if()
                }
                finish();
            }
        });
    }
}
