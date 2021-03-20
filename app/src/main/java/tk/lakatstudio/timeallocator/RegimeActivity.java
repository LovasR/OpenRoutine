package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class RegimeActivity extends AppCompatActivity {

    ViewPager regimeViewPager;
    CollectionPagerAdapter collectionPagerAdapter;
    boolean isRunning = false;

    RegimeDayFragment[] dayFragments;

    Regime regime;
    int regimeIndex;

    RegimeTodoFragment todoFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.regime_add);

        todoFragment = new RegimeTodoFragment();

        final FrameLayout regimeFrame = findViewById(R.id.regimeFrame);
        regimeViewPager = findViewById(R.id.regimeViewPager);



        regimeIndex = getIntent().getExtras().getInt("regimeIndex", -1);
        if(regimeIndex != -1){
            regime = Regime.allRegimes.get(regimeIndex);
        } else {
            //TODO popup regime config dialog
            regimeDialog();
            regime = new Regime();
            regime.dayNames = getResources().getStringArray(R.array.days_of_week);
            regime.days = new Day[]{new Day(), new Day(), new Day(), new Day(), new Day(), new Day(), new Day()};
            Regime.addRegime(regime);
            regimeIndex = Regime.allRegimes.size() - 1;
        }
        todoFragment.dayIndex = regimeViewPager.getCurrentItem();
        todoFragment.regime = regime;
        todoFragment.day = regime.days[regimeViewPager.getCurrentItem()];
        fragmentChange(todoFragment);

        dayFragments = new RegimeDayFragment[]{new RegimeDayFragment(), new RegimeDayFragment(), new RegimeDayFragment(), new RegimeDayFragment(), new RegimeDayFragment(), new RegimeDayFragment(), new RegimeDayFragment()};
        for(int i = 0; i < dayFragments.length; i++){
            dayFragments[i].fragmentIndex = i;
            dayFragments[i].regime = regime;
        }
        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager(), dayFragments);
        regimeViewPager.setAdapter(collectionPagerAdapter);
        regimeViewPager.setSaveEnabled(false);

        final BottomNavigationView bottomNavigation = findViewById(R.id.regimeBottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.regime_menu_1:
                        regimeFrame.setVisibility(View.GONE);
                        regimeViewPager.setVisibility(View.VISIBLE);

                        return true;
                    case R.id.regime_menu_2:
                        regimeFrame.setVisibility(View.VISIBLE);
                        regimeViewPager.setVisibility(View.GONE);
                        todoFragment.day = regime.days[regimeViewPager.getCurrentItem()];
                        //fragmentChange(todoFragment);
                        return true;
                }
                return false;
            }
        });

        FloatingActionButton fab = findViewById(R.id.regimeFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (bottomNavigation.getSelectedItemId()){
                    case R.id.regime_menu_1:
                        Intent intent1 = new Intent(RegimeActivity.this, DayItemActivity.class);
                        intent1.putExtra("", -1);
                        intent1.putExtra("regimeIndex", regime.index);
                        intent1.putExtra("regimeDayIndex", regimeViewPager.getCurrentItem());
                        regime.isSaved = false;
                        startActivity(intent1);
                        break;
                    case R.id.regime_menu_2:
                        Intent intent2 = new Intent(RegimeActivity.this, TodoItemActivity.class);
                        intent2.putExtra("regimeIndex", regimeIndex);
                        intent2.putExtra("regimeDayIndex", regimeViewPager.getCurrentItem());
                        regime.isSaved = false;
                        startActivity(intent2);
                        break;
                }


            }
        });


    }
    void regimeDialog(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(RegimeActivity.this);
        View dialogView = getLayoutInflater().inflate(R.layout.regime_add_dialog, null);
        final EditText nameEditText = dialogView.findViewById(R.id.addRegimeEditName);
        Button done = dialogView.findViewById(R.id.addRegimeDone);

        
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nameEditText.getText().length() > 0) {
                    regime.name = nameEditText.getText().toString();
                    alertDialog.cancel();
                }
            }
        });

        alertDialog.show();
    }
    
    void fragmentChange(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.regimeFrame, fragment);
        transaction.setReorderingAllowed(true);
        //transaction.addToBackStack(null);
        transaction.show(fragment);
        transaction.commit();
    }


}
