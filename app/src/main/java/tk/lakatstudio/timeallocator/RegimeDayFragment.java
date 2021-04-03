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

import java.util.ArrayList;

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

        //when the setDateText was called before the view was inflated
        dayDateText.setText(regime.dayNames[fragmentIndex]);
        fragmentDay = regime.days[fragmentIndex];


        dayPlanner.setLayoutManager(new LinearLayoutManager(getContext()));
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
                editIntent.putExtra("regimeIndex", regime.index);
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

        adapter.refreshContents(fragmentDay.dayItems);
    }


    /*
    public void dayPlannerInit(final Fragment fragment){

        if(fragmentDay == null){
            Log.v("fragment_preload", fragmentIndex + " fragmentDay null");
            return;
        }
        if(fragmentDay.dayItems.size() == 0){
            Log.v("fragment_preload", fragmentIndex + " b " + fragmentDay.dayItems.size());
            dayPlanner.setAdapter(null);
            return;
        }



        /*
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
                editIntent.putExtra("regimeIndex", regime.index);
                editIntent.putExtra("regimeDayIndex", fragmentIndex);
                Log.v("regime_intent_debug", "from: " + regime.index);
                startActivity(editIntent);
            }
        });
        dayPlanner.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Log.e("Day_list", "Item removed @: " + i);
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                fragmentDay.removeDayItem(i);
                                arrayAdapter.notifyDataSetChanged();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                builder.setMessage(getString(R.string.remove_activity,
                        getString(R.string.day_item_singular))).setPositiveButton(getString(R.string.yes),
                        dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
                return true;
            }
        });
        */
//    }

}
