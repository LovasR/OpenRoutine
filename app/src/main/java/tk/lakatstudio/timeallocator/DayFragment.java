package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DayFragment extends Fragment {

    TextView noDayItemText;
    RecyclerView rDayPlanner;
    DayItemAdapter adapter;
    Day fragmentDay;
    boolean isRunning = false;

    TextView dayDateText;
    ImageButton daySelect;

    int fragmentIndex;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.day_fragment, container, false);

        rDayPlanner = view.findViewById(R.id.rDayPlanner);
        dayDateText = view.findViewById(R.id.dayDate);
        noDayItemText = view.findViewById(R.id.dayNoItems);
        Log.e("UI_test", "oncreateview fragment " + fragmentIndex);


        //when the setDateText was called before the view was inflated
        if(dayDateText.getText().length() == 0) {
            SpannableString dateText = new SpannableString(new SimpleDateFormat(DayInit.getDateFormat(getContext()), Locale.getDefault()).format(Calendar.getInstance().getTime()));
            dateText.setSpan(new UnderlineSpan(), 0, dateText.length(), 0);
            dayDateText.setText(dateText);
        }

        daySelect = view.findViewById(R.id.daySelect);
        daySelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(fragmentDay.start);
                calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + 1);
                final MaterialDatePicker datePicker = MaterialDatePicker.Builder.datePicker()
                        .setSelection(calendar.getTime().getTime()).setTitleText("Select day").build();

                datePicker.show(getFragmentManager(), "datePicker");
                final Calendar calendarOut = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                    @Override
                    public void onPositiveButtonClick(Object selection) {
                        calendarOut.setTimeInMillis((Long) selection);

                        MainFragment1.fragmentIndex = calendarOut.get(Calendar.YEAR) * 365 + calendarOut.get(Calendar.DAY_OF_YEAR);
                        final int SCROLL_FORWARD = -1;
                        MainFragment1.staticClass.refreshAllFragments(SCROLL_FORWARD);
                    }
                });
            }
        });
        daySelect.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                MainFragment1.fragmentIndex = calendar.get(Calendar.YEAR) * 365 + calendar.get(Calendar.DAY_OF_YEAR);
                final int SCROLL_FORWARD = -1;
                MainFragment1.staticClass.refreshAllFragments(SCROLL_FORWARD);
                return true;
            }
        });

        ImageButton daySettings = view.findViewById(R.id.daySettings);
        daySettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(DayFragment.this.getContext(), SettingsActivity.class);
                startActivity(settingsIntent);
            }
        });



        rDayPlanner.setLayoutManager(new LinearLayoutManager(getContext()));
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getResources().getDrawable(R.drawable.divider_nothing));
        rDayPlanner.addItemDecoration(itemDecor);
        ArrayList<DayItem> dayItems = fragmentDay.dayItems;

        adapter = new DayItemAdapter(getContext(), dayItems, this);
        adapter.setClickListener(new DayItemAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent editIntent = new Intent(getContext(), DayItemActivity.class);
                editIntent.putExtra("index", position);
                editIntent.putExtra("fragmentIndex", fragmentIndex);
                startActivity(editIntent);
            }
        });
        adapter.setLongClickListener(new DayItemAdapter.ItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                fragmentDay.removeDayItem(position);
                                adapter.notifyItemRemoved(position);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage(getString(R.string.remove_activity,
                        getString(R.string.day_item_singular))).setPositiveButton(getString(R.string.yes),
                        dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
            }
        });
        rDayPlanner.setAdapter(adapter);


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

    boolean setDateText(int fragmentIndex, int todayIndex, Context context){
        //TODO settings_todo make this user selectable format
        SpannableString dateText = new SpannableString(new SimpleDateFormat(DayInit.getDateFormat(context), Locale.getDefault()).format(fragmentDay.start.getTime()));;
        if(fragmentIndex == todayIndex){
            Log.v("fragment_date", "underline void");
            //dateText.setSpan(new UnderlineSpan(), 0, dateText.length(), 0);

            Drawable drawable = context.getResources().getDrawable(R.drawable.selected_background);
            assert drawable != null;
            drawable.setColorFilter(context.getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC);
            try{
                daySelect.setBackground(drawable);
            } catch (NullPointerException np){
                np.printStackTrace();
            }
        } else {
            try{
                daySelect.setBackground(context.getResources().getDrawable(R.drawable.selected_background));
            } catch (NullPointerException np){
                np.printStackTrace();
            }
        }

        try {
            dayDateText.setText(dateText);
        } catch (NullPointerException np){
            np.printStackTrace();
        }
        return true;
    }

    String lengthAdapter(Date start, Date end){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(end);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        calendar.setTime(start);
        hours -= calendar.get(Calendar.HOUR_OF_DAY);
        minutes -= calendar.get(Calendar.MINUTE);


        calendar.set(Calendar.HOUR_OF_DAY, 2);
        Log.v("adaptertest", hours + " 1 " + calendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        Log.v("adaptertest", hours + " 2 " + calendar.getTime());
        calendar.set(Calendar.MINUTE, minutes);

        //very weird error with the calendar, when set to 2 it returns 3 when get
        String out = "";
        if(hours > 0 && hours != 2){
            out += new SimpleDateFormat("k", Locale.getDefault()).format(calendar.getTime()) + " " + getString(R.string.hour_short) + " ";
        } else if(hours == 2){
            out += hours + " " + getString(R.string.hour_short) + " ";
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
            Log.v("fragment_preload", fragmentIndex + " b ");
            rDayPlanner.setVisibility(View.GONE);
            noDayItemText.setVisibility(View.VISIBLE);
            return;
        } else {
            rDayPlanner.setVisibility(View.VISIBLE);
            noDayItemText.setVisibility(View.GONE);
        }

        adapter.refreshContents(fragmentDay.dayItems);
    }
}

