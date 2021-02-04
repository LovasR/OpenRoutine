package tk.lakatstudio.timeallocator;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

    boolean startPickerClicked = false;
    boolean endPickerClicked = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_item_add);


        final EditText itemName = findViewById(R.id.addDayItemEditName);

        final int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        startTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hours).setMinute(00).build();

        final Button startTimeButton = findViewById(R.id.addDayItemStartTime);
        startTimeButton.setText(new SimpleDateFormat("HH:00", Locale.getDefault()).format(Calendar.getInstance().getTime()));

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
                                .setHour(hours).setMinute(00).build();
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
                    }
                });
            }
        });

        //TODO make default activity length so if lessons are the same default to 45 mins
        endTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(hours + 1).setMinute(00).build();

        final Button endTimeButton = findViewById(R.id.addDayItemEndTime);
        final Calendar future = Calendar.getInstance();
        future.set(Calendar.HOUR_OF_DAY, future.get(Calendar.HOUR_OF_DAY) + 1);
        //TODO 12 hour format switch
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:00", Locale.getDefault());
        endTimeButton.setText(simpleDateFormat.format(future.getTime()));

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
                                .setHour(hours).setMinute(00).build();
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

        /*Button activityButton = findViewById(R.id.addDayItemActivity);
        Drawable drawable = getDrawable(R.drawable.spinner_background);
        drawable.setColorFilter(Color.parseColor("#3FBF5F"), PorterDuff.Mode.SRC);
        activityButton.setBackground(drawable);
        activityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getBaseContext());
                View dialogView = getLayoutInflater().inflate(R.layout.activity_picker_dialog, null);

            }
        });*/

        Button done = findViewById(R.id.addDayItemDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar startTime = Calendar.getInstance();
                if(startPickerClicked) {
                    startTime.set(Calendar.HOUR_OF_DAY, startTimePicker.getHour());
                    startTime.set(Calendar.MINUTE, startTimePicker.getMinute());
                } else {
                    startTime.set(Calendar.HOUR_OF_DAY, hours);
                    startTime.set(Calendar.MINUTE, 00);
                }
                Calendar endTime = (Calendar) startTime.clone();
                if(endPickerClicked) {
                    endTime.set(Calendar.HOUR_OF_DAY, endTimePicker.getHour());
                    endTime.set(Calendar.MINUTE, endTimePicker.getMinute());
                } else {
                    endTime.set(Calendar.HOUR_OF_DAY, future.get(Calendar.HOUR_OF_DAY));
                    endTime.set(Calendar.MINUTE, 00);
                }
                CycleManager.currentDay.addDayItem(new DayItem(itemName.getText().toString(), startTime.getTime(), endTime.getTime(), ActivityType.allActivityTypes.get(0)));//activity type is placeholder
                finish();
            }
        });
    }
}
