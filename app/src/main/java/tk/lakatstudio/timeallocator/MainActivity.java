package tk.lakatstudio.timeallocator;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    static boolean isRunning = true;

    MainFragment1 fragment1;
    MainFragment2 fragment2;
    MainFragment3 fragment3;
    MainFragment4 fragment4;

    TextView mainTitle;
    ImageButton mainDaySelect;
    LinearLayout mainTitleLayout;

    BroadcastReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DayInit.init(this);

        mainTitle = findViewById(R.id.mainTitle);
        mainDaySelect = findViewById(R.id.mainDaySelect);
        mainTitleLayout = findViewById(R.id.mainTitleLayout);

        fragment1 = new MainFragment1();
        mainTitleLayout.setVisibility(View.GONE);
        fragmentChange(fragment1);

        fragment2 = new MainFragment2();
        fragment3 = new MainFragment3();
        fragment4 = new MainFragment4();

        final BottomNavigationView bottomNav = findViewById(R.id.mainBottomNavigation);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.main_menu_1:
                        mainTitleLayout.setVisibility(View.GONE);
                        fragmentChange(fragment1);
                        return true;
                    case R.id.main_menu_2:
                        fragmentChange(fragment2);
                        setMainTitle(MainFragment1.fragmentIndex);
                        fragment2.setDateText(mainTitle, mainDaySelect, fragment1.dayFragments[1].fragmentDay.start.getTime(), MainFragment1.fragmentIndex, fragment1.todayIndex, MainActivity.this);
                        return true;
                    case R.id.main_menu_3:
                        setMainTitle(getString(R.string.regime_title));
                        fragmentChange(fragment3);
                        return true;
                    case R.id.main_menu_4:
                        setMainTitle(getString(R.string.activity_title));
                        fragmentChange(fragment4);
                        return true;
                }
                return false;
            }
        });

        //the main add button acting in the context of the selected tab
        FloatingActionButton fab = findViewById(R.id.mainFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (bottomNav.getSelectedItemId()){
                    case R.id.main_menu_1:
                        Intent intent1 = new Intent(MainActivity.this, DayItemActivity.class);
                        intent1.putExtra("fragmentIndex", MainFragment1.fragmentIndex);
                        startActivity(intent1);
                        break;
                    case R.id.main_menu_2:
                        mainDaySelect.setVisibility(View.VISIBLE);
                        Intent intent2 = new Intent(MainActivity.this, TodoItemActivity.class);
                        intent2.putExtra("fragmentIndex", MainFragment1.fragmentIndex);
                        startActivity(intent2);
                        break;
                    case R.id.main_menu_3:
                        fragment3.regimeDialog(null);
                        /*Intent intent3 = new Intent(MainActivity.this, RegimeActivity.class);
                        intent3.putExtra("regimeIndex", -1);
                        startActivity(intent3);*/
                        break;
                    case R.id.main_menu_4:
                        fragment4.activityTypeAdd(null, fragment4);
                        break;
                }


            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        ImageButton mainSettings = findViewById(R.id.mainSettings);
        mainSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });

        mainDaySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(fragment2.day.start);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
                final MaterialDatePicker datePicker = MaterialDatePicker.Builder.datePicker()
                        .setSelection(calendar.getTime().getTime()).setTitleText("Select day").build();

                //TODO refresh everything when this

                datePicker.show(getSupportFragmentManager(), "datePicker");
                final Calendar calendarOut = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        calendarOut.setTimeInMillis((Long) selection);

                        MainFragment1.fragmentIndex = calendarOut.get(Calendar.YEAR) * 366 + calendarOut.get(Calendar.DAY_OF_YEAR);

                        fragment2.setDateText(mainTitle, mainDaySelect, calendarOut.getTime().getTime(), MainFragment1.fragmentIndex, MainFragment1.todayIndex, MainActivity.this);
                        fragment2.changeDay(Day.getDay(MainFragment1.fragmentIndex));

                        final int SCROLL_FORWARD = -1;
                        MainFragment1.staticClass.refreshAllFragments(MainActivity.this, SCROLL_FORWARD);
                    }
                });
            }
        });

        //if launched from notification check intent
        if(getIntent().getExtras() != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bundle extras = getIntent().getExtras();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            fragment1.highlightDayItem(extras.getLong("dayItemStart"), UUID.fromString(extras.getString("dayItemID")));
                        }
                    });
                }
            }).start();
        }
    }

    void registerDAyItemActivity(){

    }

    void setMainTitle(String title){
        mainTitleLayout.setVisibility(View.VISIBLE);
        mainTitle.setText(title);
        mainDaySelect.setVisibility(View.GONE);
    }

    void setMainTitle(int dayIndex){
        mainTitleLayout.setVisibility(View.VISIBLE);

        Day day = DayInit.daysHashMap.get(dayIndex);
        String dateText = new SimpleDateFormat(DayInit.getDateFormat(getBaseContext()), Locale.getDefault()).format(day.start);
        /*Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);*/
        mainTitle.setText(dateText);
    }

    void fragmentChange(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFrame, fragment);
        transaction.setReorderingAllowed(true);
        transaction.show(fragment);
        transaction.commit();
    }

    @Override
    protected void onPause() {
        DayInit.saveAll(this);
        isRunning = false;
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //unregisterReceiver(alarmReceiver);
        super.onDestroy();
    }
}