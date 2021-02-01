package tk.lakatstudio.timeallocator;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DayItemActivity extends FragmentActivity {

    MaterialTimePicker startTimePicker;
    MaterialTimePicker endTimePicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_item_add);

        final int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        startTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hours).setMinute(00).build();

        Button startTimeButton = findViewById(R.id.addDayItemStartTime);
        startTimeButton.setText(new SimpleDateFormat("HH:00", Locale.getDefault()).format(Calendar.getInstance().getTime()));

        startTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimePicker.show(getSupportFragmentManager(), "fragment_tag");
                startTimePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        startTimePicker = new MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(hours).setMinute(00).build();
                    }
                });
            }
        });

        //TODO make default activity length so if lessons are the same default to 45 mins
        endTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hours + 1).setMinute(00).build();

        Button endTimeButton = findViewById(R.id.addDayItemEndTime);
        Calendar future = Calendar.getInstance();
        future.set(Calendar.HOUR_OF_DAY, future.get(Calendar.HOUR_OF_DAY) + 1);
        //TODO 12 hour format switch
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:00", Locale.getDefault());
        endTimeButton.setText(simpleDateFormat.format(future.getTime()));

        endTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endTimePicker.show(getSupportFragmentManager(), "fragment_tag");
                endTimePicker.addOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        endTimePicker = new MaterialTimePicker.Builder()
                                .setTimeFormat(TimeFormat.CLOCK_24H)
                                .setHour(hours).setMinute(00).build();
                    }
                });
            }
        });

        Button done = findViewById(R.id.addDayItemDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(startTimePicker != null && endTimePicker != null) {
                    Calendar startTime = Calendar.getInstance();
                    startTime.set(Calendar.HOUR_OF_DAY, startTimePicker.getHour());
                    startTime.set(Calendar.MINUTE, startTimePicker.getMinute());
                    Calendar endTime = startTime;
                    endTime.set(Calendar.HOUR_OF_DAY, endTimePicker.getHour());
                    endTime.set(Calendar.MINUTE, endTimePicker.getMinute());
                    CycleManager.currentDay.addDayItem(new DayItem(startTime.getTime(), endTime.getTime(), new ActivityType()));//activity type is placeholder
                }
            }
        });
    }
}
