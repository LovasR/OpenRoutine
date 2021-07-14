package tk.lakatstudio.timeallocator;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.AlarmManagerCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

import tk.lakatstudio.timeallocator.DayItem.NotificationTime;

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

    NotificationAdapter notificationAdapter;
    //the visible notification deletes are immediate while notificationTime deletes are committed when Done is pressed
    ArrayList<NotificationTime> visibleNotificationTimes;
    ArrayList<NotificationTime> notificationTimes;
    HashMap<UUID, NotificationTime> notificationTimesAdded = new HashMap<>();
    ArrayList<NotificationTime> changedNotificationTimes = new ArrayList<>();
    ArrayList<UUID> notificationsRemoveIndex = new ArrayList<>();
    int dayItemLength;

    Day focusedDay;

    boolean isRegimeDay = false;
    UUID regimeIndex;
    int regimeDayIndex;
    Regime regime;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_item_add);

        if(ActivityType.allActivityTypes.size() == 0){
            int defaultColor = getResources().getIntArray(R.array.default_colors)[0];
            String name = getResources().getStringArray(R.array.default_activities)[0];
            ActivityType.addActivityType(name, defaultColor);
            selectedActivity = ActivityType.allActivityTypes.get(ActivityType.allActivityTypes.size() - 1);
        } else {
            selectedActivity = ActivityType.allActivityTypes.get(0);
        }

        final EditText itemName = findViewById(R.id.addDayItemEditName);

        final Button startTimeButton = findViewById(R.id.addDayItemStartTime);

        final Button endTimeButton = findViewById(R.id.addDayItemEndTime);

        final Button activityButton = findViewById(R.id.addDayItemActivity);

        final int focusedFragment = getIntent().getExtras().getInt("fragmentIndex", -1);
        //Log.v("regime_null", "focusedFragment: " + focusedFragment + " allregime: " + Regime.allRegimes.toString());
        if(focusedFragment != -1){
            Log.v("focused_fragment", "focusedFragment: " + focusedFragment);
            focusedDay = DayInit.daysHashMap.get(focusedFragment);
        } else {
            String rawRegimeIndex = getIntent().getExtras().getString("regimeIndex", "");
            regimeDayIndex = getIntent().getExtras().getInt("regimeDayIndex", -1);
            Log.v("regime_intent_debug", "" + getIntent().getExtras().getInt("regimeIndex", -1));
            if(rawRegimeIndex.length() > 0) {
                regimeIndex = UUID.fromString(rawRegimeIndex);
                regime = Regime.allRegimes.get(regimeIndex);
                Log.v("regime_null", "regime: " + Regime.allRegimes.toString() + " " + rawRegimeIndex);
                focusedDay = regime.days[regimeDayIndex];
                isRegimeDay = true;
            } else {
                //oh no, this shouldn`t run
                Log.v("regime_null", "oh no: " + rawRegimeIndex);
                focusedDay = new Day();
            }
        }

        Log.v("intent_debug", "index: " + getIntent().getExtras().getString("index", "null") + " fragmentIndex: " + focusedFragment);
        String dayItemIDRaw = getIntent().getExtras().getString("index", null);
        final UUID dayItemID = (dayItemIDRaw != null ? UUID.fromString(dayItemIDRaw) : null);
        if(dayItemID != null) {
            dayItem = focusedDay.dayItems.get(dayItemID);
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

            notificationTimes = new ArrayList<>(dayItem.notificationTimes.values());
            dayItemLength = (int) (dayItem.end.getTime() - dayItem.start.getTime()) / 1000;
        } else {

            Calendar calendar = Calendar.getInstance();
            //get time from currently focused day
            calendar.setTimeInMillis(focusedDay.start.getTime());
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.HOUR_OF_DAY, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));

            startHour = calendar.get(Calendar.HOUR_OF_DAY);
            startMinute = 0;
            calendar.set(Calendar.MINUTE, startMinute);
            startTimeButton.setText(new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault()).format(calendar.getTime()));

            //TODO set end time to the preferred length of the activityType
            int preferredHours = (selectedActivity.preferredLength > 0 ? selectedActivity.preferredLength / 60 : 1);
            endHour = startHour + preferredHours;
            int preferredMinutes = (selectedActivity.preferredLength > 0 ? selectedActivity.preferredLength % 60 : 0);
            endMinute = preferredMinutes;
            final Calendar future = (Calendar) calendar.clone();
            future.set(Calendar.HOUR_OF_DAY, endHour);
            future.set(Calendar.MINUTE, endMinute);
            endTimeButton.setText(new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault()).format(future.getTime()));

            activityButton.setText(selectedActivity.name);
            Drawable drawable = getDrawable(R.drawable.spinner_background);
            drawable.setColorFilter(selectedActivity.color, PorterDuff.Mode.SRC);
            activityButton.setBackground(drawable);

            notificationTimes = new ArrayList<>();
            dayItemLength = dayItemLengthCalc(startHour, endHour, startMinute, endMinute);
            Log.v("dayItemLength", "init length:  " + dayItemLengthCalc(startHour, endHour, startMinute, endMinute));
        }
        visibleNotificationTimes = (ArrayList<NotificationTime>) notificationTimes.clone();

        startTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(DayInit.getMaterialTimeFormat(getBaseContext()))
                .setHour(startHour).setMinute(startMinute).build();


        //TODO update notification time with time change, make notification update function


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

                        notificationRefresh(notificationTimes, dayItemLength, dayItemLengthCalc(startHour, endHour, startMinute, endMinute));

                        dayItemLength = dayItemLengthCalc(startHour, endHour, startMinute, endMinute);
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

                        endHour = endTimePicker.getHour();
                        endMinute = endTimePicker.getMinute();

                        notificationRefresh(notificationTimes, dayItemLength, dayItemLengthCalc(startHour, endHour, startMinute, endMinute));
                        dayItemLength = dayItemLengthCalc(startHour, endHour, startMinute, endMinute);
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

                ArrayAdapter<ActivityType> adapter = new ArrayAdapter<ActivityType>(getBaseContext(), R.layout.activity_item, ActivityType.allActivityTypes){
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        if(convertView==null){
                            convertView = getLayoutInflater().inflate(R.layout.activity_item, null);
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

        final ImageButton notificationAdd = findViewById(R.id.addDayItemNotification);
        notificationAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notificationDialog(null, -1);
            }
        });

        RecyclerView notificationList = findViewById(R.id.addDayItemNotificationsList);
        notificationList.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getResources().getDrawable(R.drawable.divider_nothing));
        notificationList.addItemDecoration(itemDecor);

        notificationAdapter = new NotificationAdapter(this, visibleNotificationTimes, dayItemLength/*, (int) (dayItem.end.getTime() - dayItem.start.getTime()), dayItem.start.getTime()*/);
        notificationAdapter.setClickListener(new NotificationAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                notificationDialog(notificationTimes.get(position), position);
            }
        });
        notificationAdapter.setLongClickListener(new NotificationAdapter.ItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                /*if(dayItem != null){
                                    Log.v("cancelAlarm", "reqID " +  notificationTimes.get(position).requestID);
                                    DayInit.cancelAlarm(DayItemActivity.this, notificationTimes.get(position).requestID);
                                }*/

                                notificationsRemoveIndex.add(visibleNotificationTimes.get(position).ID);
                                notificationTimesAdded.remove(visibleNotificationTimes.get(position).ID);
                                visibleNotificationTimes.remove(position);
                                notificationAdapter.notifyItemRemoved(position);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(DayItemActivity.this);
                builder.setMessage(getString(R.string.remove_activity,
                        getString(R.string.day_item_singular))).setPositiveButton(getString(R.string.yes),
                        dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
            }
        });
        notificationList.setAdapter(notificationAdapter);

        final ExtendedFloatingActionButton done = findViewById(R.id.addDayItemDone);
        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        finishDayItem(dayItemID, itemName.getText().toString(), focusedFragment);
                    }
                }).start();

                finish();
            }
        });
    }

    void finishDayItem(UUID dayItemID, String itemName, int focusedFragment){
        //messy code for setting Calendars with the two options being if the user the material dialog or not
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(focusedDay.start);
        Calendar endTime = (Calendar) startTime.clone();
        getSetTimes(startTime, endTime);

        HashMap<UUID, NotificationTime> notificationTimesHashMap = makeNotificationHash(notificationTimes);
        if(notificationsRemoveIndex.size() > 0){
            notificationTimeRefresh(DayItemActivity.this, notificationTimesHashMap, notificationsRemoveIndex);
        }


        //if it was only started for editing, it only edits existing item
        if (dayItemID != null) {
            dayItem.name = itemName;
            dayItem.type = selectedActivity;
            dayItem.start = startTime.getTime();
            dayItem.end = endTime.getTime();

            dayItem.notificationTimes = notificationTimesHashMap;
            if(isRegimeDay){
                Calendar calendar = Calendar.getInstance();
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.setTimeInMillis(focusedDay.start.getTime());
                regime.changeDayItem(dayItem, (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2));
            }

            //reset changed notification times
            for(NotificationTime notificationTime : changedNotificationTimes){
                resetNotification(notificationTime);
            }
            //TODO set alarms for new notificationTimes
            //TODO update dayItem.notificationTimes as hashmap
        } else {
            dayItem = new DayItem(itemName, startTime.getTime(), endTime.getTime(), selectedActivity, notificationTimesHashMap);
            focusedDay.addDayItem(dayItem);
            DayInit.currentDayItems.put(dayItem.ID, dayItem);

            if(isRegimeDay){
                Calendar calendar = Calendar.getInstance();
                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                calendar.setTimeInMillis(focusedDay.start.getTime());
                regime.addDayItem(dayItem, (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2));
            }
        }

        final DayFragment dayFragment;
        if((dayFragment = MainFragment1.getFragment(focusedFragment)) != null){
            //run on UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dayFragment.dayPlannerInit();
                }
            });
        }

        Log.v("notificationSet", " " + notificationTimesAdded.values().size());
        if(notificationTimesAdded.size() > 0){
            setNotificationAlarms(DayItemActivity.this, dayItem);
        }
    }

    void setNotificationAlarms(Context context, DayItem dayItem){
        if(notificationTimesAdded.size() > 0){
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent notificationIntent = new Intent(context, NotificationReceiver.class);

            notificationIntent.setPackage(context.getPackageName());
            notificationIntent.setAction("Notification.Create");
            notificationIntent.putExtra("dayItemID", dayItem.ID.toString());
            notificationIntent.putExtra("dayItemTypeName", dayItem.type.name);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dayItem.start.getTime());

            for(DayItem.NotificationTime timeOffset : notificationTimesAdded.values()){
                Log.v("notificationSet", "offset format: " + ATNotificationManager.offsetFormat(context, timeOffset.offset));
                if(focusedDay.start.getTime() + (24 * 60 * 60 * 1000) < dayItem.start.getTime() + (timeOffset.offset * 1000)
                        || focusedDay.start.getTime() > dayItem.start.getTime() + (timeOffset.offset * 1000)){
                    if(!isRegimeDay) {
                        Calendar dayItemStart = (Calendar) calendar.clone();
                        dayItemStart.add(Calendar.SECOND, timeOffset.offset);
                        Date offsetDate = dayItemStart.getTime();
                        Log.v("notificationSet", "remote offsetDAte: " + new SimpleDateFormat("d. HH:mm").format(offsetDate));
                        Log.v("notificationSet", "notification time: " + new SimpleDateFormat("M.d. HH:mm").format(new Date(dayItem.start.getTime() + timeOffset.offset * 1000)));
                        if(dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR) == MainFragment1.todayIndex){
                            notificationSetAlarm(context, notificationIntent, am, dayItem, timeOffset);
                            Log.v("notificationSet", "alert set: " + timeOffset.requestID);
                        }
                        Day targetDay = Day.getDay(DayItemActivity.this, offsetDate);
                        targetDay.addRemoteScheduledNotification(dayItem, timeOffset.offset, dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR), timeOffset.ID);
                    } else {
                        //for regime days the remotes are stored in themselves
                        int dayOffset = timeOffset.offset / (24 * 60 * 60);
                        Regime regime = Regime.allRegimes.get(regimeIndex);
                        Log.v("regime_RMS", "routine " + dayOffset + " " + (regimeDayIndex + dayOffset + 35) % regime.days.length + " offset: " + timeOffset.offset + " " + dayItem.start.getTime() + " offsetday: " + ((timeOffset.offset - (dayItem.start.getTime() / 1000)) % (24 * 60 * 60)));
                        //35 days is max with regime days
                        regime.days[(regimeDayIndex + dayOffset + 35) % regime.days.length].addRemoteScheduledNotification(dayItem, Math.abs((timeOffset.offset - (dayItem.start.getTime() / 1000)) % (24 * 60 * 60)), -1, timeOffset.ID);

                        Log.v("regime_RMS", "regime RMS length: " + regime.days[1].remoteScheduledNotifications.size() + " @: " + regime.days[1].toString());
                        //focusedDay.addRemoteScheduledNotification(dayItem, timeOffset.offset, -1, timeOffset.ID);
                    }
                } else {
                    if(!isRegimeDay) {
                        notificationSetAlarm(context, notificationIntent, am, dayItem, timeOffset);
                    } else {
                        Calendar todayCalendar = Calendar.getInstance();
                        todayCalendar.setTimeInMillis(focusedDay.start.getTime());
                        int dayIndex = todayCalendar.get(Calendar.DAY_OF_WEEK);

                        /*todayCalendar.setTimeInMillis();
                        int dayOfWeek = todayCalendar.get(Calendar.DAY_OF_WEEK);*/

                        boolean isToday = ((dayIndex == Calendar.SUNDAY ? 6 : dayIndex - 2) == regimeDayIndex);

                        Log.v("notificationSet_r", "(add) booleans: " + isToday + " " + regime.isActive(focusedDay.start.getTime()) + ";" + (dayIndex == Calendar.SUNDAY ? 6 : dayIndex - 2) + " " + regimeDayIndex);

                        //checks if the day edited in this activity is today
                        //TODO replace regime.isActive with isActive check after schedule is implemented
                        if(isToday && regime.isActive(focusedDay.start.getTime())){
                            Log.v("notificationSet_r", "set alarm ");
                            notificationSetAlarm(context, notificationIntent, am, dayItem, timeOffset);
                        }
                    }
                }
            }
        }
    }

    HashMap<UUID, NotificationTime> makeNotificationHash(ArrayList<NotificationTime> notificationTimes){
        HashMap<UUID, NotificationTime> outHash = new HashMap<>();
        for(NotificationTime notificationTime : notificationTimes){
            outHash.put(notificationTime.ID, notificationTime);
        }
        return outHash;
    }

    //completes the notificationIntent and sets Alarm
    void notificationSetAlarm(Context context, Intent notificationIntent, AlarmManager alarmManager, DayItem dayItem, NotificationTime timeOffset){
        notificationIntent.putExtra("requestID", timeOffset.requestID);
        notificationIntent.putExtra("dayItemStart", dayItem.start.getTime());
        notificationIntent.putExtra("dayItemEnd", dayItem.end.getTime());
        notificationIntent.putExtra("notificationOffset", timeOffset.offset);
        notificationIntent.putExtra("notificationOffsetR", timeOffset.fromEnd);

        Log.v("notification_debug", "notificationIntent: " + notificationIntent.getExtras().toString());
        Log.v("notification_debug", "timeOffset offset: " + timeOffset.offset);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, timeOffset.requestID, notificationIntent, 0);

        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager, AlarmManager.RTC_WAKEUP, dayItem.start.getTime() + (timeOffset.offset * 1000), pendingIntent);
    }

    void getSetTimes(Calendar startTime, Calendar endTime){
        try {
            if (startPickerClicked && startTimePicker != null) {
                startHour = startTimePicker.getHour();
                startMinute = startTimePicker.getMinute();
            }

            if (endPickerClicked && endTimePicker != null) {
                endHour = endTimePicker.getHour();
                endMinute = endTimePicker.getMinute();
            }
        } catch (Exception e) {
            //damn
        }
        startTime.set(Calendar.HOUR_OF_DAY, startHour);
        startTime.set(Calendar.MINUTE, startMinute);
        if(endTime != null) {
            endTime.set(Calendar.HOUR_OF_DAY, endHour);
            endTime.set(Calendar.MINUTE, endMinute);
        }
    }

    void checkOffset(EditText offsetEditText1, EditText offsetEditText2, int offsetMax){
        String offsetText1 = offsetEditText1.getText().toString();
        int result = Integer.parseInt(offsetText1.length() == 0 ? "0" : offsetText1);
        if(result > offsetMax - 1){
            String offsetText2 = offsetEditText2.getText().toString();
            if(offsetText2.length() > 0){
                offsetEditText2.setText(String.valueOf(Integer.parseInt(offsetText2) + result / offsetMax));
            } else{
                offsetEditText2.setText(String.valueOf(result / offsetMax));
            }
            offsetEditText1.setText(String.valueOf(result % offsetMax));
        }
    }

    void checkRegimeOffset(EditText dayEditText, int offsetMax){
        String dayOffsetText = dayEditText.getText().toString();
        int offset = Integer.parseInt(dayOffsetText.length() == 0 ? "0" : dayOffsetText);
        if(offset > offsetMax){
            dayEditText.setText(String.valueOf(offsetMax));
        }
    }

    void notificationRefresh(ArrayList<NotificationTime> notificationTimes, int oldLength, int newLength){
        notificationAdapter.dayItemLength = newLength;
        for(NotificationTime notificationTime : notificationTimes){
            if(notificationTime.offset == oldLength){
                notificationTime.offset = newLength;
            } else {
                if(notificationTime.fromEnd){
                    notificationTime.offset += (newLength - oldLength);
                }
                //notificationTimes.set(i, notificationTimes.get(i) + (newLength - oldLength));
            }
            Log.v("notificationTime_length", "length after: " + notificationTime.offset + " oldlength: " + oldLength + " newLength: " + newLength);
            //add to to-be reset list
            if(!changedNotificationTimes.contains((notificationTime))) {
                changedNotificationTimes.add(notificationTime);
            }
        }
        notificationAdapter.notifyDataSetChanged();
    }

    void notificationTimeRefresh(Context context, HashMap<UUID, NotificationTime> notificationTimes, ArrayList<UUID> notificationsRemoveIndex){
        //deletes notificationTimes listed in the removeIndex
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dayItem.start.getTime());
        Log.v("notificationSet_Del", "notifRemIndxSize: " + notificationsRemoveIndex.size() + " notifTimeSize: " + notificationTimes.size());
        int size = notificationsRemoveIndex.size();
        for(int i = 0; i < size; i++) {
            Log.v("notificationSet_Del", "RemoveIndex cycle " + i);
            NotificationTime notificationTime = notificationTimes.get(notificationsRemoveIndex.get(i));
            if(notificationTime == null){
                Log.v("notificationSet_Del", "RemoveIndex doesnt contain");
                continue;
            }
            //check if dayItem isn't created, if it is, no alarms have been set
            if(dayItem == null){
                notificationTimes.remove(notificationsRemoveIndex.get(i));
                continue;
            }
            Log.v("notificationSet_Del", "RemoveIndex contains");
            if (focusedDay.start.getTime() + (24 * 60 * 60 * 1000) < dayItem.start.getTime() + (notificationTime.offset * 1000)
                    || focusedDay.start.getTime() > dayItem.start.getTime() + (notificationTime.offset * 1000)) {
                if (!isRegimeDay) {
                    Calendar dayItemStart = (Calendar) calendar.clone();
                    dayItemStart.add(Calendar.SECOND, notificationTime.offset);
                    Date offsetDate = dayItemStart.getTime();
                    Log.v("notificationSet_Del", "remote offsetDAte: " + new SimpleDateFormat("d. HH:mm").format(offsetDate));
                    Log.v("notificationSet_Del", "dayItemStart time: " + new SimpleDateFormat("M.d. HH:mm").format(dayItemStart.getTime()));
                    if (dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR) == MainFragment1.todayIndex) {
                        //cancels if alarm is scheduled
                        DayInit.cancelAlarm(context, notificationTime.requestID);
                        Log.v("notificationSet_Del", "Canceling: " + notificationTime.requestID);
                    }
                    Day targetDay = Day.getDay(DayItemActivity.this, offsetDate);
                    targetDay.removeRemoteScheduledNotification(notificationTime.ID, dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR));
                } else {
                    //for regime days the remotes are stored in themselves
                    Log.v("notificationSet", "routine");
                    int dayOffset = notificationTime.offset / (24 * 60 * 60);
                    Regime regime = Regime.allRegimes.get(regimeIndex);
                    regime.days[(regimeDayIndex + dayOffset + 35) % regime.days.length].removeRemoteScheduledNotification(dayItem.ID, -1);
                }
            } else {
                if(!isRegimeDay) {
                    DayInit.cancelAlarm(context, notificationTime.requestID);
                } else {
                    Calendar todayCalendar = Calendar.getInstance();
                    todayCalendar.setTimeInMillis(focusedDay.start.getTime());
                    int dayIndex = todayCalendar.get(Calendar.DAY_OF_WEEK);

                    boolean isToday = (dayIndex == MainFragment1.todayIndex);

                    Log.v("notificationSet_r", "(remove) booleans: " + isToday + " " + regime.isActive(focusedDay.start.getTime()));

                    //checks if the day edited in this activity is today
                    //TODO replace regime.isActive with isActive check after schedule is implemented
                    if(isToday && regime.isActive(focusedDay.start.getTime())){
                        DayInit.cancelAlarm(context, notificationTime.requestID);
                    }
                }
            }
            notificationTimes.remove(notificationsRemoveIndex.get(i));
        }
    }

    void resetNotification(NotificationTime notificationTime){
        boolean isRMS = focusedDay.start.getTime() + (24 * 60 * 60 * 1000) < dayItem.start.getTime() + (notificationTime.offset * 1000)
                || focusedDay.start.getTime() > dayItem.start.getTime() + (notificationTime.offset * 1000);

        //re-sets notification time by first canceling and then setting with the appropriate information
        if(focusedDay.dayIndex == MainFragment1.todayIndex && dayItem != null && !isRMS){
            resetNotificationIntent(notificationTime);
        } else if (regime != null && dayItem != null && regime.isActive(focusedDay.start.getTime())){
            Calendar calendar = Calendar.getInstance();
            int dayOfWeekToday = calendar.get(Calendar.DAY_OF_WEEK);
            calendar.setTimeInMillis(focusedDay.start.getTime());

            //focusedDay.dayIndex == MainFragment1.todayIndex;
            if(calendar.get(Calendar.DAY_OF_WEEK) == dayOfWeekToday){
                resetNotificationIntent(notificationTime);
            }
        } else if(isRMS){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(dayItem.start.getTime());
            Calendar dayItemStart = (Calendar) calendar.clone();
            dayItemStart.add(Calendar.SECOND, notificationTime.offset);
            Date offsetDate = dayItemStart.getTime();
            Log.v("notificationSet", "remote offsetDAte: " + new SimpleDateFormat("d. HH:mm").format(offsetDate));
            Log.v("notificationSet", "notification time: " + new SimpleDateFormat("M.d. HH:mm").format(new Date(dayItem.start.getTime() + notificationTime.offset * 1000)));
            if(dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR) == MainFragment1.todayIndex){
                resetNotificationIntent(notificationTime);
            }
            Day targetDay = Day.getDay(DayItemActivity.this, offsetDate);
            //in this instance addRSN will overwrite as it uses the same notificationID
            targetDay.addRemoteScheduledNotification(dayItem, notificationTime.offset, dayItemStart.get(Calendar.YEAR) * 366 + dayItemStart.get(Calendar.DAY_OF_YEAR), notificationTime.ID);
        }
    }

    void resetNotificationIntent(NotificationTime notificationTime){
        DayInit.cancelAlarm(DayItemActivity.this, notificationTime.requestID);

        AlarmManager am = (AlarmManager) DayItemActivity.this.getSystemService(Context.ALARM_SERVICE);

        notificationTime.requestID = DayInit.notificationRequestID;
        DayInit.increaseNotificationRequestID();

        Intent notificationIntent = new Intent(DayItemActivity.this, NotificationReceiver.class);

        notificationIntent.setPackage(DayItemActivity.this.getPackageName());
        notificationIntent.setAction("Notification.Create");
        notificationIntent.putExtra("dayItemID", dayItem.ID.toString());
        notificationIntent.putExtra("dayItemTypeName", dayItem.type.name);
        notificationSetAlarm(DayItemActivity.this, notificationIntent, am, dayItem, notificationTime);
    }

    int dayItemLengthCalc(int startHour, int endHour, int startMinute, int endMinute){
        Log.v("dayItemLength", startHour + " " + endHour + " " + startMinute + " " + endMinute);
        int minutes = endMinute - startMinute;
        return (endHour - startHour + (minutes < 0 ? -1 : 0)) * 60 * 60 + (minutes < 0 ? 60 + minutes : minutes) * 60;
    }

    boolean isInputNotificationChanged = false;
    void notificationDialog(final NotificationTime notificationTimeIn, final int positionClicked){
        final AlertDialog.Builder builder = new AlertDialog.Builder(DayItemActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.notification_add_dialog, null);
        builder.setView(dialogView);

        Spinner spinner1;
        Spinner spinner2;
        if(getResources().getBoolean(R.bool.notification_time_order)){
            spinner1 = dialogView.findViewById(R.id.addNotificationRelativeSpinner);
            spinner2 = dialogView.findViewById(R.id.addNotificationRelativeSpinnerpre);
        } else {
            spinner1 = dialogView.findViewById(R.id.addNotificationRelativeSpinnerpre);
            spinner2 = dialogView.findViewById(R.id.addNotificationRelativeSpinner);
        }

        final Spinner relativeSpinner = spinner1;
        final Spinner relativeSpinnerPre = spinner2;

        final LinearLayout offsets = dialogView.findViewById(R.id.addNotificationOffsets);

        final EditText offsetDay = dialogView.findViewById(R.id.addNotificationOffsetDay);
        final EditText offsetHour = dialogView.findViewById(R.id.addNotificationOffsetHour);
        final EditText offsetMinute = dialogView.findViewById(R.id.addNotificationOffsetMinute);

        Button done = dialogView.findViewById(R.id.addNotificationDone);

        final AlertDialog alertDialog = builder.create();

        //localization

        ArrayAdapter<CharSequence> preadapter = ArrayAdapter.createFromResource(DayItemActivity.this, R.array.notification_times_pre, R.layout.spinner_item);
        preadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relativeSpinnerPre.setAdapter(preadapter);
        relativeSpinnerPre.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(i == 0){
                    offsets.setVisibility(View.GONE);
                    if(notificationTimeIn != null) {
                        notificationTimeIn.offset = 0;
                    }
                } else {
                    offsets.setVisibility(View.VISIBLE);
                    offsetDay.setText("");
                    offsetHour.setText("");
                    offsetMinute.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(DayItemActivity.this, R.array.notification_times, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        relativeSpinner.setAdapter(adapter);
        relativeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(notificationTimeIn != null){
            int pre = ( (notificationTimeIn.offset < 0 ? 1 : (notificationTimeIn.offset > 0 ? 2 : 0) ));
            int suf = (!notificationTimeIn.fromEnd ? 0 : 1);
            if(getResources().getBoolean(R.bool.notification_time_order)){
                relativeSpinnerPre.setSelection(pre);
                relativeSpinner.setSelection(suf);
            } else {
                relativeSpinner.setSelection(pre);
                relativeSpinnerPre.setSelection(suf);
            }
            if(pre > 0){
                int offset = Math.abs(notificationTimeIn.offset - (notificationTimeIn.fromEnd ? (int) (dayItem.start.getTime() - dayItem.end.getTime()) / 1000 : 0));
                int dayOffset = offset / (24 * 60 * 60);
                offset -= dayOffset * (24 * 60 * 60);
                int hourOffset = offset / (60 * 60);
                offset -= hourOffset * (60 * 60);
                int minuteOffset = offset / (60);
                offsetDay.setText(String.valueOf(dayOffset));
                offsetHour.setText(String.valueOf(hourOffset));
                offsetMinute.setText(String.valueOf(minuteOffset));
            }
        }

        if(isRegimeDay) {
            offsetDay.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
                    if (actionID == EditorInfo.IME_ACTION_SEARCH ||
                            actionID == EditorInfo.IME_ACTION_DONE ||
                            keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN
                                    && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        if (keyEvent == null || !keyEvent.isShiftPressed()) {
                            checkRegimeOffset(offsetDay, 35);
                            return true;
                        }
                    }
                    return false;
                }
            });
            offsetDay.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    checkRegimeOffset(offsetDay, 35);
                }
            });
        }

        offsetHour.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
                if (actionID == EditorInfo.IME_ACTION_SEARCH ||
                        actionID == EditorInfo.IME_ACTION_DONE ||
                        keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN
                                && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (keyEvent == null || !keyEvent.isShiftPressed()) {
                        checkOffset(offsetHour, offsetDay, 24);
                        return true;
                    }
                }
                return false;
            }
        });
        offsetHour.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                checkOffset(offsetHour, offsetDay, 24);
            }
        });

        offsetMinute.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionID, KeyEvent keyEvent) {
                if (actionID == EditorInfo.IME_ACTION_SEARCH ||
                        actionID == EditorInfo.IME_ACTION_DONE ||
                        actionID == EditorInfo.IME_ACTION_NEXT ||
                        actionID == EditorInfo.IME_ACTION_PREVIOUS ||
                        keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_DOWN
                                && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (keyEvent == null || !keyEvent.isShiftPressed()) {
                        checkOffset(offsetMinute, offsetHour, 60);
                        return true;
                    }
                }
                return false;
            }
        });

        offsetMinute.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                checkOffset(offsetMinute, offsetHour, 60);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dayOffsetString = offsetDay.getText().toString();
                String hourOffsetString = offsetHour.getText().toString();
                String minuteOffsetString = offsetMinute.getText().toString();
                //notificationTime is in seconds
                int notificationTimeOffset = Integer.parseInt(dayOffsetString.length() == 0 ? "0" : dayOffsetString) * 60 * 60 * 24
                        + Integer.parseInt(hourOffsetString.length() == 0 ? "0" : hourOffsetString) * 60 * 60
                        + Integer.parseInt(minuteOffsetString.length() == 0 ? "0" : minuteOffsetString) * 60;
                //if user selected before

                //TOdO delete and reset when notnull and changed
                
                if(relativeSpinnerPre.getSelectedItem().equals(getString(R.string.notification_before))){
                    Log.v("dayItemLength", "before");
                    notificationTimeOffset = -notificationTimeOffset;
                }
                if(relativeSpinner.getSelectedItem().equals(getString(R.string.notification_end))){
                    //if user selected end
                    Log.v("dayItemLength", "end");
                    //after this notificationTime contains the offset of the dayItem`s length
                    notificationTimeOffset += dayItemLength;
                    if(notificationTimeIn == null){
                        NotificationTime notificationTime = new NotificationTime(notificationTimeOffset, true, DayInit.notificationRequestID);
                        notificationTimes.add(notificationTime);
                        visibleNotificationTimes.add(notificationTime);
                        notificationTimesAdded.put(notificationTime.ID, notificationTime);
                    } else {
                        if(notificationTimeIn.offset != notificationTimeOffset || !notificationTimeIn.fromEnd){
                            isInputNotificationChanged = true;
                        }
                        notificationTimeIn.offset = notificationTimeOffset;
                        notificationTimeIn.fromEnd = true;
                    }
                } else {
                    if(notificationTimeIn == null){
                        NotificationTime notificationTime = new NotificationTime(notificationTimeOffset, false, DayInit.notificationRequestID);
                        notificationTimes.add(notificationTime);
                        visibleNotificationTimes.add(notificationTime);
                        notificationTimesAdded.put(notificationTime.ID, notificationTime);
                    } else {
                        if(notificationTimeIn.offset != notificationTimeOffset || notificationTimeIn.fromEnd){
                            isInputNotificationChanged = true;
                        }
                        notificationTimeIn.offset = notificationTimeOffset;
                        notificationTimeIn.fromEnd = false;
                    }
                }


                if(notificationTimeIn == null) {
                    DayInit.increaseNotificationRequestID();
                    Toast.makeText(DayItemActivity.this, String.valueOf(visibleNotificationTimes.size()) + " " + relativeSpinnerPre.getSelectedItem(), Toast.LENGTH_SHORT).show();
                    notificationAdapter.notifyItemInserted(visibleNotificationTimes.size() - 1);
                } else if(positionClicked != -1 && isInputNotificationChanged) {
                    notificationAdapter.notifyItemChanged(positionClicked);
                    resetNotification(notificationTimeIn);
                }
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }

}

class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    ArrayList<NotificationTime> notifications;
    LayoutInflater inflater;
    ItemClickListener clickListener;
    ItemLongClickListener longClickListener;
    Context context;

    int dayItemLength;
    long dayItemStart;

    NotificationAdapter(Context context, ArrayList<NotificationTime> notifications, int dayItemLength/*, long dayItemStart*/) {
        this.inflater = LayoutInflater.from(context);
        this.notifications = notifications;
        Log.v("recyclerView_test", notifications.size() + "");
        this.context = context;
        this.dayItemLength = dayItemLength;
        //this.dayItemStart = dayItemStart;
    }

    String offsetFormat(int offset){
        String out = "";
        int dayOffset = offset / (24 * 60 * 60);
        offset -= dayOffset * (24 * 60 * 60);
        int hourOffset = offset / (60 * 60);
        offset -= hourOffset * (60 * 60);
        int minuteOffset = offset / (60);
        //offset -= minuteOffset * (60);
        //Log.v("offsetDump", "offset: " + dayOffset + "dayOffset: " + dayOffset + "hourOffset: " + hourOffset + "minuteOffset: " + minuteOffset);
        if (dayOffset != 0){
            out += Math.abs(dayOffset) + context.getString(R.string.day_short) + " ";
        }
        if (hourOffset != 0){
            out += Math.abs(hourOffset) + context.getString(R.string.hour_short) + " ";
        }
        if (minuteOffset != 0){
            out += Math.abs(minuteOffset) + context.getString(R.string.minute_short);
        }
        return out;
    }

    @NonNull
    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final NotificationTime notificationTime = notifications.get(position);
        TextView notificationView = (TextView) holder.itemView;
        String notificationText = null;
        if(!notificationTime.fromEnd && notificationTime.offset == 0){
            notificationText = context.getString(R.string.notification_at_start);
        } else if(notificationTime.fromEnd && notificationTime.offset == dayItemLength) {
            notificationText = context.getString(R.string.notification_at_end);
        } else if(!notificationTime.fromEnd){
            if(notificationTime.offset > 0){
                notificationText = context.getString(R.string.notification_after);
            } else if(notificationTime.offset < 0){
                notificationText = context.getString(R.string.notification_before);
            }
            notificationText += " " + context.getString(R.string.notification_start) + ": " + offsetFormat(notificationTime.offset);
        } else if(notificationTime.fromEnd){
            if(notificationTime.offset > dayItemLength){
                notificationText = context.getString(R.string.notification_after);
            } else if(notificationTime.offset < dayItemLength){
                notificationText = context.getString(R.string.notification_before);
            }
            notificationText += " " + context.getString(R.string.notification_end) + ": " + offsetFormat(notificationTime.offset - dayItemLength);
        }
        notificationView.setText(notificationText);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(view, getAdapterPosition());
            }
            return true;
        }
    }

    public NotificationTime getItem(int position) {
        return notifications.get(position);
    }
    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }
    void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.longClickListener = itemLongClickListener;
    }
    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    public interface ItemLongClickListener{
        void onItemLongClick(View view, int position);
    }
}
