package tk.lakatstudio.timeallocator;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainFragment1 extends Fragment {

    ViewPager viewPager;
    CollectionPagerAdapter collectionPagerAdapter;
    boolean isRunning = false;

    DayFragment[] dayFragments;
    static int fragmentIndex = -1;
    int todayIndex;
    int dayStartIndex;
    int dayRange;

    int focusedPage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_1, container, false);

        Log.e("UI_test", "oncreateview main_fragment1");
        //dayPlanner = view.findViewById(R.id.testDayPlanner);
        //dayPlannerInit(this);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, 1);
        Log.v("date test", new SimpleDateFormat("y.M.d", Locale.getDefault()).format(calendar.getTime()));
        Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.e("UI_test", "onviewcreated main_fragment1");
        dayFragments = new DayFragment[]{new DayFragment(), new DayFragment(), new DayFragment()};
        collectionPagerAdapter = new CollectionPagerAdapter(getChildFragmentManager(), dayFragments);
        viewPager = view.findViewById(R.id.verticalViewPager);
        viewPager.setAdapter(collectionPagerAdapter);
        viewPager.setCurrentItem(1);
        viewPager.setSaveEnabled(false);
        //viewPager.setCurrentItem(viewPager.getChildCount() * 1000 / 2);
        //dayFragments[0].onStart();7821111111111

        //only sets current day on first run
        if(fragmentIndex == -1){
            fragmentIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        }

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.v("fragment_preload_", "position: " + position);
                focusedPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.v("fragment_preload_", "focusedPAge: " + focusedPage);
                if(state == ViewPager.SCROLL_STATE_IDLE){
                    if(focusedPage == 0){
                        fragmentIndex--;
                        refreshAllFragments();
                        //dayStartIndex--;
                        /*dayFragments[0].dayPlannerInit(dayFragments[0]);
                        dayFragments[1].dayPlannerInit(dayFragments[1]);
                        dayFragments[2].dayPlannerInit(dayFragments[2]);*/
                        //dayFragments[1].dayPlanner.setAdapter(dayFragments[0].dayPlanner.getAdapter());
                    } else if(focusedPage == 2){
                        fragmentIndex++;
                        refreshAllFragments();
                        /*dayFragments[0].dayPlannerInit(dayFragments[0]);
                        dayFragments[1].dayPlannerInit(dayFragments[1]);
                        dayFragments[2].dayPlannerInit(dayFragments[2]);*/
                        //dayFragments[1].dayPlanner.setAdapter(dayFragments[2].dayPlanner.getAdapter());
                    }

                    //prevent flashing of old ListView
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    //dayFragments[0].fragmentDay = DayInit.daysHashMap.get(fragmentIndex - 1) != null ? DayInit.daysHashMap.get(fragmentIndex - 1) :
                    //gson.fromJson(DayInit.readFromFile(getContext(), "day_" + dateFormat.format(calendar.getTime())).get(0), Day.class);

                    //this is a joke
                    //dayFragments[0].fragmentDay = DayInit.daysHashMap.get(fragmentIndex - 1) != null ? DayInit.daysHashMap.get(fragmentIndex - 1) : new Gson().fromJson(DayInit.readFromFile(getContext(), "day_" + new SimpleDateFormat("y.M.d", Locale.getDefault()).format(calendar.getTime())).get(0), Day.class);
                    viewPager.setCurrentItem(1, false);
                }
            }
        });
        viewPager.setOffscreenPageLimit(3);

        /*for(int i = 0; i < 3; i++) {
            final int finalI = i;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    dayFragments[finalI].onStart();
                }
            }).start();
        }*/

        super.onViewCreated(view, savedInstanceState);
        todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
        refreshAllFragments();
    }

    void refreshAllFragments(){
        for(int i = 0; i < 3; i++){
            final int finalIndex = i;
            /*new Thread(new Runnable() {
                @Override
                public void run() {
                    setFragmentDay(finalIndex);
                }
            }).start();*/
            new Thread(){
                @Override
                public void run() {
                    Log.v("fragment_preload", "start load: " + finalIndex);
                    setFragmentDay(finalIndex);

                    try{
                        synchronized (this){

                                Log.v("fragment_preload", "dayPlannerInit " + finalIndex);

                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(dayFragments[finalIndex].getContext() != null) {
                                            dayFragments[finalIndex].dayPlannerInit(dayFragments[finalIndex]);
                                        }
                                        Log.v("fragment_date", "dateindex: " + todayIndex + " " + fragmentIndex);
                                        dayFragments[finalIndex].setDateText(fragmentIndex + (finalIndex - 1), todayIndex);
                                    }
                                });

                        }
                    } catch (NullPointerException np){
                        np.printStackTrace();
                        try {
                            Thread.sleep(50);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dayFragments[finalIndex].setDateText(fragmentIndex + (finalIndex - 1), todayIndex);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
    }

    void setFragmentDay(int index){
        //new index offsets to the two positons realative to the changed fragmentIndex
        //newIndex is the position of the day to be loaded
        int newIndex = fragmentIndex + (index - 1);
        Log.v("fragment_preload", "newindex: " + newIndex + " index: " + index + " fragmentIndex: " + fragmentIndex);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_YEAR, newIndex);

        Gson gson = new Gson();
        SimpleDateFormat dateFormat = new SimpleDateFormat("y.M.d", Locale.getDefault());

        //retrieve Day from the hashmap, if null, try to load from file
        if((dayFragments[index].fragmentDay = DayInit.daysHashMap.get(newIndex)) == null){

            ArrayList<String> dayJsons = DayInit.readFromFile(getContext(), "day_" + dateFormat.format(calendar.getTime()));

            if(dayJsons != null){
                dayFragments[index].fragmentDay = gson.fromJson(dayJsons.get(0), Day.class);
                Log.v("fragment_preload", "loaded from file: " + new SimpleDateFormat("D").format(dayFragments[index].fragmentDay.start.getTime()));
                dayFragments[index].fragmentDay.isSaved = true;
                //dayFragments[index].dayPlannerInit(dayFragments[index]);
            } else {
                Log.v("fragment_preload", "new Day");
                dayFragments[index].fragmentDay = new Day();

                Calendar date = Calendar.getInstance();
                date.set(Calendar.DAY_OF_YEAR, newIndex);
                date.set(Calendar.HOUR_OF_DAY, 0);
                date.set(Calendar.MINUTE, 0);
                date.set(Calendar.SECOND, 0);
                dayFragments[index].fragmentDay.start = date.getTime();

                dayFragments[index].fragmentDay.isSaved = true;
            }
            //put the value not yet in the hashmap
            DayInit.daysHashMap.put(newIndex, dayFragments[index].fragmentDay);
        } else {
            Log.v("fragment_preload", "loaded from hashmap: " + new SimpleDateFormat("D").format(dayFragments[index].fragmentDay.dayItems.size()));

            Regime.removeRegimeDays(dayFragments[index].fragmentDay);
            //dayFragments[index].dayPlannerInit(dayFragments[index]);
        }
        if(!dayFragments[index].fragmentDay.isRegimeSet){
            Regime.setAllActiveRegimesDays(dayFragments[index].fragmentDay);
        }
        //dayFragments[index].dayPlannerInit(dayFragments[index]);
    }

    @Override
    public void onDestroyView() {
        Log.e("UI_test", "ondestroyview main_fragment1");
        super.onDestroyView();
    }
}


