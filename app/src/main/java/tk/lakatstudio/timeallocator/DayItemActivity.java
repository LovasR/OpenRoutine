package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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
    ArrayList<NotificationTime> notificationTimes;
    int dayItemLength;

    /*class NotificationTime{
        int offset;
        boolean fromEnd;
        NotificationTime(int offset, boolean fromEnd){
            this.offset = offset;
            this.fromEnd = fromEnd;
        }
        ArrayList<NotificationTime> fromIntArray(ArrayList<Integer> offsets){
            ArrayList<NotificationTime> out = new ArrayList<>();
            for (Integer offset : offsets){
                out.add(new NotificationTime(offsets, ))
            }
        }
    }*/


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

            notificationTimes = dayItem.notificationTimesOA;
            dayItemLength = (int) (dayItem.start.getTime() - dayItem.end.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            startHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            startMinute = 0;
            calendar.set(Calendar.MINUTE, startMinute);
            startTimeButton.setText(new SimpleDateFormat(DayInit.getHourFormat(getBaseContext()), Locale.getDefault()).format(calendar.getTime()));

            //TODO set end time to the preferred length of the activityType
            int preferredHours = (selectedActivity.preferredLength > 0 ? selectedActivity.preferredLength / 60 : 1);
            endHour = startHour + preferredHours;
            int preferredMinutes = (selectedActivity.preferredLength > 0 ? selectedActivity.preferredLength % 60 : 0);
            endMinute = preferredMinutes;
            final Calendar future = Calendar.getInstance();
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
                notificationDialog(null);
            }
        });

        RecyclerView notificationList = findViewById(R.id.addDayItemNotificationsList);
        notificationList.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecor = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getResources().getDrawable(R.drawable.divider_nothing));
        notificationList.addItemDecoration(itemDecor);

        notificationAdapter = new NotificationAdapter(this, notificationTimes, dayItemLength/*, (int) (dayItem.end.getTime() - dayItem.start.getTime()), dayItem.start.getTime()*/);
        notificationAdapter.setClickListener(new NotificationAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                notificationDialog(notificationTimes.get(position));
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
                                if(dayItem != null){
                                    Log.v("cancelAlarm", "reqID " +  notificationTimes.get(position).requestID);
                                    DayInit.cancelAlarm(DayItemActivity.this, notificationTimes.get(position).requestID);
                                }

                                notificationTimes.remove(position);
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

                //messy code for setting Calendars with the two options being if the user the material dialog or not
                Calendar startTime = Calendar.getInstance();
                startTime.setTime(focusedDay.start);
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
                    dayItem.notificationTimesOA = notificationTimes;
                } else {
                    Log.v("fragment_preload", "focusedFragment: "  + focusedFragment);
                    final DayItem newDayItem = new DayItem(itemName.getText().toString(), startTime.getTime(), endTime.getTime(), selectedActivity, notificationTimes);
                    focusedDay.addDayItem(newDayItem);
                    if(focusedFragment == MainFragment1.staticClass.todayIndex){
                        notificationSend(newDayItem, DayItemActivity.this);
                    }
                }
                finish();
            }
        });
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
    void notificationRefresh(ArrayList<NotificationTime> notificationTimes, int oldLength, int newLength){
        notificationAdapter.dayItemLength = newLength;
        for(int i = 0; i < notificationTimes.size(); i++){
            if(notificationTimes.get(i).offset == oldLength){
                notificationTimes.get(i).offset = newLength;
            } else {
                if(notificationTimes.get(i).fromEnd){
                    notificationTimes.get(i).offset += (newLength - oldLength);
                }
                //notificationTimes.set(i, notificationTimes.get(i) + (newLength - oldLength));
            }
        }
        notificationAdapter.notifyDataSetChanged();
    }

    int dayItemLengthCalc(int startHour, int endHour, int startMinute, int endMinute){
        Log.v("dayItemLength", startHour + " " + endHour + " " + startMinute + " " + endMinute);
        int minutes = endMinute - startMinute;
        return (endHour - startHour + (minutes < 0 ? -1 : 0)) * 60 * 60 + (minutes < 0 ? 60 + minutes : minutes) * 60;
    }

    void notificationSend(final DayItem dayItem, final Context context) {
        //inits notification if the add is today
        new Thread(new Runnable() {
            @Override
            public void run() {
                DayInit.currentDayItems.put(dayItem.ID, dayItem);

                //dayItem.notificationTimes.add(0);

                DayInit.addAlarm(context, dayItem);
            }
        }).run();
    }
    void notificationDialog(NotificationTime notificationTime){
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
                } else {
                    offsets.setVisibility(View.VISIBLE);
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

        if(notificationTime != null){
            int pre = ( (notificationTime.offset < 0 ? 1 : (notificationTime.offset > 0 ? 2 : 0) ));
            int suf = (!notificationTime.fromEnd ? 0 : 1);
            if(getResources().getBoolean(R.bool.notification_time_order)){
                relativeSpinnerPre.setSelection(pre);
                relativeSpinner.setSelection(suf);
            } else {
                relativeSpinner.setSelection(pre);
                relativeSpinnerPre.setSelection(suf);
            }
            if(pre > 0){
                int offset = Math.abs(notificationTime.offset);
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
                int notificationTime = Integer.parseInt(dayOffsetString.length() == 0 ? "0" : dayOffsetString) * 60 * 60 * 24
                        + Integer.parseInt(hourOffsetString.length() == 0 ? "0" : hourOffsetString) * 60 * 60
                        + Integer.parseInt(minuteOffsetString.length() == 0 ? "0" : minuteOffsetString) * 60;
                //if user selected before
                if(relativeSpinnerPre.getSelectedItem().equals(getString(R.string.notification_before))){
                    Log.v("dayItemLength", "before");
                    notificationTime = -notificationTime;
                }
                //if user selected end
                if(relativeSpinner.getSelectedItem().equals(getString(R.string.notification_end))){
                    Log.v("dayItemLength", "end");
                    notificationTime += dayItemLength;
                    notificationTimes.add(new NotificationTime(notificationTime, true, DayInit.notificationRequestID));
                } else {
                    notificationTimes.add(new NotificationTime(notificationTime, false, DayInit.notificationRequestID));
                }
                DayInit.increaseNotificationRequestID();
                Toast.makeText(DayItemActivity.this, String.valueOf(notificationTime) + " " + relativeSpinnerPre.getSelectedItem(), Toast.LENGTH_SHORT).show();
                notificationAdapter.notifyItemInserted(notificationTimes.size() - 1);
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

    void refreshContents(ArrayList<Integer> newDayItems){
        //dayItemLength = newDayItems;
        notifyDataSetChanged();
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
        /*else if(notificationTime == dayItemLength){
            notificationText = context.getString(R.string.notification_at_end);
        }*/
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
