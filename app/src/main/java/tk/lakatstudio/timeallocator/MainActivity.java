package tk.lakatstudio.timeallocator;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    static boolean isRunning = true;

    MainFragment1 fragment1;
    MainFragment2 fragment2;
    MainFragment3 fragment3;
    MainFragment4 fragment4;



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

        /*for(String aTName : getResources().getStringArray(R.array.default_activities)){
            Log.e("AT_test", aTName);
            ActivityType.addActivityType(aTName, Color.parseColor("#883FBF5F"));
        }*/

        /*ImageButton addDayItem = findViewById(R.id.addDayItemButton);
        addDayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DayItemActivity.class);
                startActivity(intent);
            }
        });

*/
        fragment1 = new MainFragment1();
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
                        fragmentChange(fragment1);
                        return true;
                    case R.id.main_menu_2:
                        fragmentChange(fragment2);
                        return true;
                    case R.id.main_menu_3:
                        fragmentChange(fragment3);
                        return true;
                    case R.id.main_menu_4:
                        fragmentChange(fragment4);
                        return true;
                }
                return false;
            }
        });

        FloatingActionButton fab = findViewById(R.id.mainFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (bottomNav.getSelectedItemId()){
                    case R.id.main_menu_1:
                        Intent intent1 = new Intent(MainActivity.this, DayItemActivity.class);
                        intent1.putExtra("fragmentIndex", fragment1.fragmentIndex);
                        startActivity(intent1);
                        break;
                    case R.id.main_menu_2:
                        Intent intent2 = new Intent(MainActivity.this, TodoItemActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.main_menu_3:
                        /*Intent intent3 = new Intent(MainActivity.this, DayItemActivity.class);
                        startActivity(intent3);*/
                        break;
                    case R.id.main_menu_4:
                        fragment4.activityTypeAdd(-1, fragment4);
                        break;
                }


            }
        });


        DayInit.init(this, this);
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
    protected void onResume() {
        Log.e("UI_test", "onresume " + isRunning);
        /*if(!isRunning){
            Log.e("UI_test", "plannerinit " + isRunning);
            isRunning = true;
            dayPlannerInit();
        }*/
        super.onResume();
    }



    @Override
    protected void onStop() {

        super.onStop();
    }

    @Override
    protected void onPause() {
        DayInit.saveAll(this);
        isRunning = false;
        super.onPause();
    }
}