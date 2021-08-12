package tk.lakatstudio.timeallocator;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.UUID;

public class RegimeActivity extends AppCompatActivity {

    ViewPager2 regimeViewPager;
    CollectionPagerAdapter collectionPagerAdapter;
    boolean isRunning = false;

    RegimeDayFragment[] dayFragments;

    Regime regime;
    UUID regimeIndex;

    RegimeTodoFragment todoFragment;

    private final int DAYS_N = 7;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DayInit.setLocale(getResources(), null);
        setContentView(R.layout.regime_add);


        final FrameLayout regimeFrame = findViewById(R.id.regimeFrame);
        regimeViewPager = findViewById(R.id.regimeViewPager);

        String rawRegimeIndex = getIntent().getExtras().getString("regimeIndex", "");
        if(rawRegimeIndex.length() > 0){
            regimeIndex = UUID.fromString(rawRegimeIndex);
            regime = Regime.allRegimes.get(regimeIndex);
        } else {
            regime = new Regime(getResources().getStringArray(R.array.days_of_week));
            Regime.addRegime(regime);
        }

        todoFragment = new RegimeTodoFragment();
        todoFragment.dayIndex = regimeViewPager.getCurrentItem();
        todoFragment.regime = regime;
        todoFragment.day = regime.days[regimeViewPager.getCurrentItem()];
        fragmentChange(todoFragment);

        dayFragments = new RegimeDayFragment[DAYS_N];
        for(int i = 0; i < DAYS_N; i++){
            dayFragments[i] = new RegimeDayFragment();
            dayFragments[i].fragmentIndex = i;
            dayFragments[i].regime = regime;
        }

        collectionPagerAdapter = new CollectionPagerAdapter(getSupportFragmentManager(), this.getLifecycle(), dayFragments);
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
                        todoFragment.dayIndex = regimeViewPager.getCurrentItem();
                        todoFragment.refreshFragment();
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
                        intent1.putExtra("regimeIndex", regime.ID.toString());
                        intent1.putExtra("regimeDayIndex", regimeViewPager.getCurrentItem());
                        regime.isSaved = false;
                        startActivity(intent1);
                        break;
                    case R.id.regime_menu_2:
                        Intent intent2 = new Intent(RegimeActivity.this, TodoItemActivity.class);
                        intent2.putExtra("regimeIndex", regime.ID.toString());
                        intent2.putExtra("regimeDayIndex", regimeViewPager.getCurrentItem());
                        regime.isSaved = false;
                        startActivity(intent2);
                        break;
                }


            }
        });


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

    @Override
    protected void onPause() {
        super.onPause();
        regime.refreshDays(RegimeActivity.this);
        DayInit.saveAll(RegimeActivity.this);
    }
}
