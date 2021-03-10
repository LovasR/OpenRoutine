package tk.lakatstudio.timeallocator;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainFragment1 extends Fragment {

    ListView dayPlanner;
    boolean isRunning = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_1, container, false);

        Log.e("UI_test", "oncreateview");
        dayPlanner = view.findViewById(R.id.testDayPlanner);
        //dayPlannerInit(this);

        return view;
    }

    @Override
    public void onResume() {
        Log.e("UI_test", "onresume_fragment");
        if(!isRunning){
            isRunning = true;
            dayPlannerInit(this);
        } else {
            dayPlannerInit(this);
        }
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        Log.e("UI_test", "ondestroyview");
        super.onDestroyView();
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

        Log.e("adaptertest", minutes + " ");

        String out = "";
        if(hours > 0){
            out += new SimpleDateFormat("H", Locale.getDefault()).format(calendar.getTime()) + " " + getString(R.string.hour_short) + " ";
        }
        if(minutes > 0 || (hours == 0 && minutes == 0)){
            out += new SimpleDateFormat("mm", Locale.getDefault()).format(calendar.getTime()) + " " + getString(R.string.minutes_short);
        }
        return out;
    }

    public void dayPlannerInit(final Fragment fragment){

        if(CycleManager.currentDay.dayItems.size() == 0){
            return;
        }
        final ArrayAdapter<DayItem> arrayAdapter = new ArrayAdapter<DayItem>(fragment.getContext(), R.layout.dayplanner_item, CycleManager.currentDay.dayItems){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView==null){
                    convertView = getLayoutInflater().inflate(R.layout.dayplanner_item, null);
                }

                final DayItem dayItem = CycleManager.currentDay.dayItems.get(position);
                Log.v("save_debug_load", "a" + dayItem.type.name);

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
                final Drawable textBackground = AppCompatResources.getDrawable(fragment.getContext(), R.drawable.spinner_background);
                textBackground.setColorFilter(dayItem.type.color, PorterDuff.Mode.SRC);
                itemType.setBackground(textBackground);
                return convertView;
            }
        };
        dayPlanner.setDividerHeight(10);
        dayPlanner.setAdapter(arrayAdapter);
        dayPlanner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent editIntent = new Intent(fragment.getContext(), DayItemActivity.class);
                editIntent.putExtra("index", i);
                startActivity(editIntent);
            }
        });
        dayPlanner.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e("Day_list", "Item removed @: " + i);
                CycleManager.currentDay.removeDayItem(i);
                arrayAdapter.notifyDataSetChanged();
                return false;
            }
        });

        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
