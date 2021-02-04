package tk.lakatstudio.timeallocator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*Spinner testSpinner = findViewById(R.id.testCycleSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cycles, android.R.layout.simple_spinner_item);
        for(int i = 0; i < ActivityType.userActivityTypes.size(); i++) {
            adapter.add(ActivityType.userActivityTypes.get(i).name);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        testSpinner.setAdapter(adapter);
        testSpinner.setSelection(4);*/

        //ATNotificationManager.notif(this);

        for(String aTName : getResources().getStringArray(R.array.default_activities)){
            Log.e("AT_test", aTName);
            ActivityType.addActivityType(aTName, Color.valueOf(Color.parseColor("#883FBF5F")));
        }

        ImageButton addDayItem = findViewById(R.id.addDayItemButton);
        addDayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DayItemActivity.class);
                startActivity(intent);
            }
        });

        DayInit.init(getBaseContext());

    }

    @Override
    protected void onResume() {
        dayPlannerInit();
        Log.e("UI_test", "onresume");
        super.onResume();
    }

    private String lengthAdapter(Date start, Date end){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        calendar.setTime(start);
        hours -= calendar.get(Calendar.HOUR_OF_DAY);
        minutes -= calendar.get(Calendar.MINUTE);

        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);

        String out = "";
        if(hours > 0){
            out += new SimpleDateFormat("H", Locale.getDefault()).format(calendar.getTime()) + " " + getString(R.string.hour_short) + " ";
        }
        if(minutes > 0 || (hours == 0 && minutes == 0)){
            out += new SimpleDateFormat("mm", Locale.getDefault()).format(calendar.getTime()) + " " + getString(R.string.minutes_short);
        }
        return out;
    }

    public void dayPlannerInit(){
        ListView dayPlanner = findViewById(R.id.testDayPlanner);

        ArrayAdapter<DayItem> arrayAdapter = new ArrayAdapter<DayItem>(this, R.layout.dayplanner_item, CycleManager.currentDay.dayItems){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView==null){
                    convertView = getLayoutInflater().inflate(R.layout.dayplanner_item, null);
                }
                final DayItem dayItem = CycleManager.currentDay.dayItems.get(position);

                convertView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), dayItem.name, Toast.LENGTH_SHORT).show();
                    }
                });

                TextView itemStart = convertView.findViewById(R.id.dayPlannerItemStart);
                itemStart.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(dayItem.start.getTime()));

                TextView itemLength = convertView.findViewById(R.id.dayPlannerItemLength);
                itemLength.setText(lengthAdapter(dayItem.start, dayItem.end));

                TextView itemName = convertView.findViewById(R.id.dayPlannerItem);
                if (dayItem.name.length() != 0){
                    itemName.setText(dayItem.name);
                    itemName.setVisibility(View.VISIBLE);
                } else {
                    itemName.setVisibility(View.GONE);
                }

                TextView itemType = convertView.findViewById(R.id.dayPlannerItemActivity);
                itemType.setText(dayItem.type.name);

                Drawable drawable = getDrawable(R.drawable.spinner_background);
                drawable.setColorFilter(Color.parseColor("#3FBF5F"), PorterDuff.Mode.SRC);
                itemType.setBackground(drawable);
                return convertView;
            }
        };
        dayPlanner.setDividerHeight(10);
        dayPlanner.setAdapter(arrayAdapter);
    }
}