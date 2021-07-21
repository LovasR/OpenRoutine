package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.UUID;

public class RegimeDayFragment extends DayFragment {

    RecyclerView dayPlanner;
    Day fragmentDay;
    DayItemAdapter adapter;

    Regime regime;

    TextView dayDateText;

    int fragmentIndex;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.day_fragment, container, false);

        dayPlanner = view.findViewById(R.id.rDayPlanner);
        dayDateText = view.findViewById(R.id.dayDate);
        //fragmentIndex = getArguments().getInt("test", -1);
        Log.e("UI_test", "oncreateview fragment " + fragmentIndex);

        /*int regimeIndex = getArguments().getInt("regimeIndex", -1);
        if(regimeIndex != -1){
            regime = Regime.allRegimes.get(regimeIndex);
        }*/

        daySelect = view.findViewById(R.id.daySelect);
        daySelect.setVisibility(View.GONE);

        //when the setDateText was called before the view was inflated
        dayDateText.setText(regime.dayNames[fragmentIndex]);
        fragmentDay = regime.days[fragmentIndex];


        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        dayPlanner.setLayoutManager(layoutManager);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getResources().getDrawable(R.drawable.divider_nothing));
        dayPlanner.addItemDecoration(itemDecor);
        final HashMap<UUID, DayItem> dayItems = fragmentDay.dayItems;

        adapter = new DayItemAdapter(getContext(), dayItems.values(), this, layoutManager);
        adapter.setClickListener(new DayItemAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent editIntent = new Intent(getContext(), DayItemActivity.class);
                editIntent.putExtra("index", adapter.getItem(position).ID.toString());
                editIntent.putExtra("regimeIndex", regime.ID.toString());
                editIntent.putExtra("regimeDayIndex", fragmentIndex);
                Log.v("regime_intent_debug", "from: " + regime.index);
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
                                //UUID ID = adapter.getItem(position).ID;
                                //fragmentDay.removeDayItem(getContext(), ID, true);
                                Calendar calendar = Calendar.getInstance();
                                calendar.setFirstDayOfWeek(Calendar.MONDAY);
                                calendar.setTimeInMillis(fragmentDay.start.getTime());
                                Log.v("regime_refresh", "original size: " + fragmentDay.dayItems.size());
                                regime.removeDayItem(adapter.getItem(position), (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY ? 6 : calendar.get(Calendar.DAY_OF_WEEK) - 2));
                                adapter.removedDayItem(position);
                                Log.v("regime_refresh", "original size: " + fragmentDay.dayItems.size() + " " + fragmentDay.dayItems.toString());
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
        dayPlanner.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        dayPlannerInit(this);
        super.onResume();
    }

    public void dayPlannerInit(final Fragment fragment){

        if(fragmentDay == null){
            Log.v("fragment_preload", fragmentIndex + " fragmentDay null");
            return;
        }
        if(fragmentDay.dayItems.size() == 0){
            Log.v("fragment_preload", fragmentIndex + " b ");
            dayPlanner.setVisibility(View.GONE);
            //noDayItemText.setVisibility(View.VISIBLE);
            return;
        } else {
            dayPlanner.setVisibility(View.VISIBLE);
            //noDayItemText.setVisibility(View.GONE);
        }

        adapter.refreshContents(fragmentDay.dayItems.values());
    }

}
