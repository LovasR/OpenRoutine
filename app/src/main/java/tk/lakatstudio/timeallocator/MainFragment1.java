package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class MainFragment1 extends Fragment {

    ViewPager2 viewPager;
    CollectionPagerAdapter collectionPagerAdapter;
    boolean isRunning = false;

    DayFragment[] dayFragments;
    static int fragmentIndex = -1;
    static int todayIndex;
    int dayStartIndex;
    int dayRange;

    int focusedPage;

    //there is only one instance of this class
    static public MainFragment1 staticClass;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_1, container, false);

        Log.e("UI_test", "oncreateview main_fragment1");

        staticClass = this;

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
        collectionPagerAdapter = new CollectionPagerAdapter(getChildFragmentManager(), this.getLifecycle(), dayFragments);
        viewPager = view.findViewById(R.id.daysViewPager);
        viewPager.setAdapter(collectionPagerAdapter);

        viewPager.setSaveEnabled(false);
        //viewPager.setCurrentItem(viewPager.getChildCount() * 1000 / 2);
        //dayFragments[0].onStart();

        //only sets current day on first run
        if(fragmentIndex == -1){
            Calendar calendar = Calendar.getInstance();
            //support for multi-year loading
            fragmentIndex = calendar.get(Calendar.YEAR) * 366 + calendar.get(Calendar.DAY_OF_YEAR);
            Log.v("focusedPage_test",  " " + fragmentIndex);
            todayIndex = fragmentIndex;
        }

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                focusedPage = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(state == ViewPager.SCROLL_STATE_IDLE) {
                    if (focusedPage == 0) {
                        fragmentIndex--;
                        refreshAllFragments(null, SCROLL_BACKWARD);
                    } else if (focusedPage == 2) {
                        fragmentIndex++;
                        refreshAllFragments(null, SCROLL_FORWARD);
                    }
                }
                Log.v("focusedPage_test", "focusedPage: " + focusedPage + " " + fragmentIndex);
            }
        });
        viewPager.setOffscreenPageLimit(3);
        viewPager.setCurrentItem(1);

        Log.v("focusedPage_test", "refresh: " + focusedPage + " " + fragmentIndex);
        refreshAllFragments(null, SCROLL_BACKWARD);
        super.onViewCreated(view, savedInstanceState);
    }

    final private static int SCROLL_FORWARD = -1;
    final private static int SCROLL_BACKWARD = 1;
    final private static int FRAGMENTS_N = 3;

    void refreshAllFragments(Context context, int direction){
        if(context == null){
            context = getContext();
        }

        for(int i = 0; i < FRAGMENTS_N; i++){
            final int finalIndex = direction > 0 ? i : FRAGMENTS_N - i - 1;

            Log.v("index_debug", finalIndex + "");
            final Context finalContext = context;
            new Thread(){
                @Override
                public void run() {
                    Log.v("fragment_preload", "start load: " + finalIndex);
                    setFragmentDay(finalContext, finalIndex);
                    final boolean[] failedSetDateText = {false};
                    try{
                        synchronized (this){
                                Log.v("fragment_preload", "dayPlannerInit " + fragmentIndex + " today: " + todayIndex);
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dayFragments[finalIndex].setDateText(fragmentIndex + (finalIndex - 1), todayIndex, finalContext);
                                        if(dayFragments[finalIndex].getContext() != null) {
                                            dayFragments[finalIndex].dayPlannerInit();
                                        }
                                        if(finalIndex == 1) {
                                            viewPager.setCurrentItem(1, false);
                                        }
                                        Log.v("fragment_date", "dateindex: " + todayIndex + " " + fragmentIndex);

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
                                    dayFragments[finalIndex].setDateText(fragmentIndex + (finalIndex - 1), todayIndex, finalContext);
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

    void setFragmentDay(Context context, int index){
        //new index offsets to the two position
        //relative to the changed fragmentIndex
        //newIndex is the position of the day to be loaded
        int newIndex = fragmentIndex + (index - 1);
        //Log.v("fragment_preload", "newindex: " + newIndex + " index: " + index + " fragmentIndex: " + fragmentIndex);

        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.HOUR_OF_DAY);
        //TODO check with leap years
        calendar.set(Calendar.DAY_OF_YEAR, newIndex - (calendar.get(Calendar.YEAR) * 366));

        SimpleDateFormat dateFormat = new SimpleDateFormat("y.M.d");

        //retrieve day from the hashmap, if null, try to load from file
        if((dayFragments[index].fragmentDay = DayInit.daysHashMap.get(newIndex)) == null){

            ArrayList<String> dayJsons = DayInit.readFromFile(context, "day_" + dateFormat.format(calendar.getTime()));

            if(dayJsons != null){
                //load from file if read returned not null
                dayFragments[index].fragmentDay = DayInit.gson.fromJson(dayJsons.get(0), Day.class);
                Log.v("fragment_preload_load", "loaded from file: " + new SimpleDateFormat("D").format(dayFragments[index].fragmentDay.start.getTime()));
            } else {
                //create new day if no file of this day was found
                dayFragments[index].fragmentDay = new Day();
                dayFragments[index].fragmentDay.dayIndex = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR) * 366;
                Log.v("day_management", dayFragments[index].fragmentDay.dayIndex + " ");

                Calendar date = (Calendar) calendar.clone();
                /*date.clear();
                date.set(Calendar.DAY_OF_YEAR, newIndex - (date.get(Calendar.YEAR) * 366));*/
                dayFragments[index].fragmentDay.start = date.getTime();
                Log.v("fragment_preload_load", "new Day " + dateFormat.format(date.getTime()));

            }
            dayFragments[index].fragmentDay.isSaved = true;
            //put the value not yet in the hashmap
            DayInit.daysHashMap.put(newIndex, dayFragments[index].fragmentDay);
        } else {
            Log.v("fragment_preload_load", "loaded from hashmap: " + new SimpleDateFormat("D", Locale.getDefault()).format(dayFragments[index].fragmentDay.start));

            //Regime.removeRegimeDays(dayFragments[index].fragmentDay);
        }
        dayFragments[index].fragmentIndex = newIndex;
        dayFragments[index].fragmentDay.nullCheck();
        Regime.setAllActiveRegimesDays(getContext(), dayFragments[index].fragmentDay);

        //dayFragments[index].fragmentIndex = dayFragments[index].fragmentDay.dayIndex;
    }

    void highlightDayItem(long dayItemStart, UUID dayItemID){
        Day day = Day.getDay(getContext(), new Date(dayItemStart));
        fragmentIndex = day.dayIndex;
        refreshAllFragments(getContext(), SCROLL_FORWARD);
        DayItem dayItem = day.dayItems.get(dayItemID);
        Log.v("highlight", dayItem.ID.toString());
        //TODO highlight dayItem
        dayFragments[1].highlightDayItem(dayItem);
    }

    static DayFragment getFragment(int index){
        for(int i = 0; i < FRAGMENTS_N; i++){
            if(staticClass.dayFragments[i].fragmentIndex == index){
                return staticClass.dayFragments[i];
            }
        }
        return null;
    }

    @Override
    public void onResume() {
        Log.e("UI_test", "onResume main_fragment1");
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        Log.e("UI_test", "ondestroyview main_fragment1");
        super.onDestroyView();
    }
}


