package tk.lakatstudio.timeallocator;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
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

public class DayFragment extends Fragment {

    ListView dayPlanner;
    Day fragmentDay;
    boolean isRunning = false;

    TextView dayDateText;

    int fragmentIndex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.day_fragment, container, false);

        dayPlanner = view.findViewById(R.id.testDayPlanner);
        dayDateText = view.findViewById(R.id.dayDate);
        fragmentIndex = getArguments().getInt("test", -1);
        Log.e("UI_test", "oncreateview fragment " + fragmentIndex);


        //when the setDateText was called before the view was inflated
        if(dayDateText.getText().length() == 0) {
            SpannableString dateText = new SpannableString(new SimpleDateFormat("y.M.d.", Locale.getDefault()).format(Calendar.getInstance().getTime()));
            dateText.setSpan(new UnderlineSpan(), 0, dateText.length(), 0);
            dayDateText.setText(dateText);
        }

        return view;
    }

    @Override
    public void onResume() {
        Log.e("UI_test", "onresume_fragment " + fragmentIndex);
        if(!isRunning){
            isRunning = true;
            dayPlannerInit(this);
        } else {
            dayPlannerInit(this);
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.e("UI_test", "ondestroy_fragment " + fragmentIndex);
        super.onDestroy();
    }

    void setDateText(int fragmentIndex, int todayIndex){
        //TODO settings_todo make this user selectable format
        SpannableString dateText = new SpannableString(new SimpleDateFormat("y.M.d.", Locale.getDefault()).format(fragmentDay.start.getTime()));;
        if(fragmentIndex == todayIndex){
            Log.v("fragment_date", "underline void");
            dateText.setSpan(new UnderlineSpan(), 0, dateText.length(), 0);

            //TODO set to user color to underline
            /*Paint paint = new Paint();
            paint.setColor(getResources().getColor(R.color.colorPrimary));
            paint.setFlags(Paint.UNDERLINE_TEXT_FLAG);
            dayDateText.setPaintFlags(paint.getFlags());
            dayDateText.setLayerPaint(paint);
            dateText.setSpan(new ForegroundColorSpan(paint.getColor()), 0, dateText.length(), 0);*/
        }

        try {
            dayDateText.setText(dateText);
        } catch (NullPointerException np){
            np.printStackTrace();
        }
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

        if(fragmentDay == null){
            Log.v("fragment_preload", fragmentIndex + " fragmentDay null");
            return;
        }
        if(fragmentDay.dayItems.size() == 0){
            Log.v("fragment_preload", fragmentIndex + " b " + fragmentDay.dayItems.size());
            //if(dayPlanner != null){
                dayPlanner.setAdapter(null);
            //}
            return;
        }
        //test.setText("test " + fragmentIndex + " " +  new SimpleDateFormat("D").format(fragmentDay.start));
        //Log.v("fragment_preload", fragmentIndex + " b " + fragmentDay.dayItems.size());
        final ArrayAdapter<DayItem> arrayAdapter = new ArrayAdapter<DayItem>(requireContext(), R.layout.dayplanner_item, fragmentDay.dayItems){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView==null){
                    convertView = getLayoutInflater().inflate(R.layout.dayplanner_item, null);
                }

                //Log.v("fragment_preload", fragmentIndex + " a " + fragmentDay.dayItems.size() + " postion " + position);
                final DayItem dayItem = fragmentDay.dayItems.get(position);

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
                fragmentDay.removeDayItem(i);
                arrayAdapter.notifyDataSetChanged();
                return false;
            }
        });
    }
}
