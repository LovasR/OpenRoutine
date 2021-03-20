package tk.lakatstudio.timeallocator;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    protected void onPause() {
        DayInit.saveAll(this);
        isRunning = false;
        super.onPause();
    }
}