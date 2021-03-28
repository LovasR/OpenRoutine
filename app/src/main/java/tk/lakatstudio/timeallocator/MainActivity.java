package tk.lakatstudio.timeallocator;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static boolean isRunning = true;

    MainFragment1 fragment1;
    MainFragment2 fragment2;
    MainFragment3 fragment3;
    MainFragment4 fragment4;

    TextView mainTitle;
    LinearLayout mainTitleLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainTitle = findViewById(R.id.mainTitle);
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
                        setMainTitle(MainFragment1.fragmentIndex);
                        fragmentChange(fragment2);
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
                        Intent intent2 = new Intent(MainActivity.this, TodoItemActivity.class);
                        intent2.putExtra("fragmentIndex", MainFragment1.fragmentIndex);
                        startActivity(intent2);
                        break;
                    case R.id.main_menu_3:
                        Intent intent3 = new Intent(MainActivity.this, RegimeActivity.class);
                        intent3.putExtra("regimeIndex", -1);
                        startActivity(intent3);
                        break;
                    case R.id.main_menu_4:
                        fragment4.activityTypeAdd(-1, fragment4);
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

        DayInit.init(this, this);
    }

    void setMainTitle(String title){
        mainTitleLayout.setVisibility(View.VISIBLE);
        mainTitle.setText(title);
    }

    void setMainTitle(int dayIndex){
        mainTitleLayout.setVisibility(View.VISIBLE);

        Day day = DayInit.daysHashMap.get(dayIndex);
        //TODO settings
        SpannableString dateText = new SpannableString(new SimpleDateFormat(DayInit.getDateFormat(getBaseContext()), Locale.getDefault()).format(day.start));
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        if(day.start.getTime() == today.getTime().getTime()) {
            dateText.setSpan(new UnderlineSpan(), 0, dateText.length(), 0);
        }
        mainTitle.setText(dateText);
    }

    void fragmentChange(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFrame, fragment);
        transaction.setReorderingAllowed(true);
        //transaction.addToBackStack(null);
        transaction.show(fragment);
        transaction.commit();
    }

    @Override
    protected void onPause() {
        DayInit.saveAll(this);
        isRunning = false;
        super.onPause();
    }
}