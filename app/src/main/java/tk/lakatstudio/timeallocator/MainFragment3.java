package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainFragment3 extends Fragment {

    ListView regimeList;
    TextView noRegimeText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_3, container, false);

        regimeList = view.findViewById(R.id.regimeListview);
        noRegimeText = view.findViewById(R.id.regimeNoRegime);

        return view;
    }

    @Override
    public void onResume() {
        regimeListInit(this);
        super.onResume();
    }

    void regimeListInit(final Fragment fragment){

        Log.e("regime_test", "regime list init " + Regime.allRegimes.size());

        if(Regime.allRegimes.size() == 0){
            noRegimeText.setVisibility(View.VISIBLE);
            regimeList.setVisibility(View.GONE);
            return;
        } else {
            noRegimeText.setVisibility(View.GONE);
            regimeList.setVisibility(View.VISIBLE);
        }
        ArrayList<Regime> regimeArrayList = new ArrayList<>(Regime.allRegimes.values());
        for(Regime regime : regimeArrayList){
            if(regime.toDelete){
                regimeArrayList.remove(regime);
            }
        }
        ArrayAdapter<Regime> arrayAdapter = new ArrayAdapter<Regime>(fragment.getContext(), R.layout.regime_item, regimeArrayList){
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.regime_item, null);
                }

                final Regime regime = this.getItem(position);

                //if regime is going to be deleted, don`t show anything
                if(regime.toDelete){
                    return new View(getContext());
                }

                Log.e("regime_test", "regime list init " + regime.name);

                TextView itemName = convertView.findViewById(R.id.regimeItemName);
                itemName.setText(regime.name);

                SwitchMaterial itemActive = convertView.findViewById(R.id.regimeItemActive);
                itemActive.setChecked(regime.isActive(System.currentTimeMillis()));
                itemActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        //regime.isActive(System.currentTimeMillis()) = b;
                        //TODO make temporary scheduleitem for one day
                        if(!b){
                            regime.deleteItems(getContext());
                        }
                    }
                });

                ImageButton scheduleEdit = convertView.findViewById(R.id.regimeItemScheduleEdit);
                scheduleEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        regimeDialog(regime);
                    }
                });

                LinearLayout itemLayout = convertView.findViewById(R.id.regimeItemLinearLayout);
                itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent editIntent = new Intent(fragment.getContext(), RegimeActivity.class);
                        editIntent.putExtra("regimeIndex", getItem(position).ID.toString());
                        startActivity(editIntent);
                    }
                });
                itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        regime.toDelete = true;
                                        regime.deleteItems(getContext());
                                        notifyDataSetChanged();
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                        builder.setMessage(getString(R.string.remove_activity,
                                getString(R.string.regime_singular))).setPositiveButton(getString(R.string.yes),
                                dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show().getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
                        return true;
                    }
                });

                return convertView;
            }
        };
        regimeList.setAdapter(arrayAdapter);
    }


    void regimeDialog(Regime regime){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.regime_add_dialog, null);
        final TextInputLayout nameEditTextParent = dialogView.findViewById(R.id.addRegimeEditNameField);
        final EditText nameEditText = dialogView.findViewById(R.id.addRegimeEditName);
        ImageButton addScheduleItem = dialogView.findViewById(R.id.addScheduleItem);
        RecyclerView scheduleList = dialogView.findViewById(R.id.add_regime_dialog_schedule_list);
        Button done = dialogView.findViewById(R.id.addRegimeDone);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final boolean wasRegimeNull;
        if(regime == null) {
            wasRegimeNull = true;
            regime = new Regime(getResources().getStringArray(R.array.days_of_week));
        } else {
            wasRegimeNull = true;
            nameEditText.setText(regime.name);
        }

        nameEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(i2 != 0 && nameEditTextParent.getError() != null){
                    nameEditTextParent.setError(null);
                }
            }
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        scheduleList.setLayoutManager(new LinearLayoutManager(getContext()));
        final ScheduleAdapter scheduleAdapter = new ScheduleAdapter(getContext(), regime.schedule);
        scheduleList.setAdapter(scheduleAdapter);
        DividerItemDecoration itemDecor = new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL);
        itemDecor.setDrawable(getResources().getDrawable(R.drawable.divider_nothing));
        scheduleList.addItemDecoration(itemDecor);

        final Regime finalRegime = regime;
        scheduleAdapter.setClickListener(new ScheduleAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                scheduleItemDialog(finalRegime, finalRegime.schedule.get(position), scheduleAdapter);
            }
        });

        scheduleAdapter.setLongClickListener(new ScheduleAdapter.ItemLongClickListener() {
            @Override
            public void onItemLongClick(View view, final int position) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                finalRegime.schedule.remove(position);
                                scheduleAdapter.notifyItemRemoved(position);
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                if(scheduleAdapter.getItem(position).end.getTime() == 0){
                    builder.setMessage(R.string.schedule_item_default_info).show();
                } else {
                    builder.setMessage(getString(R.string.remove_activity,
                            getString(R.string.day_item_singular))).setPositiveButton(getString(R.string.yes),
                            dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show().getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
                }
            }
        });

        addScheduleItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scheduleItemDialog(finalRegime, null, scheduleAdapter);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(nameEditText.getText().length() > 0) {
                    if(wasRegimeNull) {
                        Regime.addRegime(finalRegime);
                    }
                    finalRegime.name = nameEditText.getText().toString();
                    regimeListInit(MainFragment3.this);
                    alertDialog.cancel();
                } else {
                    nameEditTextParent.setError(getString(R.string.todo_set_text_error));
                }
            }
        });

        alertDialog.show();
    }

    long selectedStart = 0;
    //selectedStart + a week
    long selectedEnd = 0;
    void scheduleItemDialog(final Regime regime, final Regime.ScheduleItem scheduleItem, final ScheduleAdapter scheduleAdapter){
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.schedule_item_dialog, null);
        final Button datePickerButton = dialogView.findViewById(R.id.scheduleDialogDatePicker);
        final SwitchCompat activeSwitch = dialogView.findViewById(R.id.scheduleItemActive);
        final ImageView warningImage = dialogView.findViewById(R.id.scheduleItemWarningIcon);
        final TextView warningText = dialogView.findViewById(R.id.scheduleItemWarningText);
        Button done = dialogView.findViewById(R.id.scheduleItemDone);

        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.MILLISECOND);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.HOUR_OF_DAY);
        selectedStart = calendar.getTime().getTime();
        selectedEnd = selectedStart + (7 * 24 * 60 * 60 * 1000);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY MMM dd", Locale.getDefault());
        boolean isDefaultItem = false;
        if(scheduleItem == null) {
            //scheduleItem = new Regime.ScheduleItem(getResources().getStringArray(R.array.days_of_week));
            datePickerButton.setText(simpleDateFormat.format(new Date(selectedStart)) + " - " + simpleDateFormat.format(new Date(selectedEnd)));
        } else {
            //nameEditText.setText(scheduleItem.name);
            if(scheduleItem.end.getTime() == 0){
                isDefaultItem = true;
                datePickerButton.setText(R.string.regime_dialog_schedule_forever);
                datePickerButton.setBackground(getResources().getDrawable(R.drawable.spinner_background));
            } else {
                datePickerButton.setText(simpleDateFormat.format(scheduleItem.start) + " - " + simpleDateFormat.format(scheduleItem.end));
            }
            activeSwitch.setChecked(scheduleItem.isActive);
            selectedStart = scheduleItem.start.getTime();
            selectedEnd = scheduleItem.end.getTime();
        }

        if(!isDefaultItem) {
            datePickerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final MaterialDatePicker<Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                            .setSelection(new Pair<>(selectedStart, selectedEnd)).setTitleText("Select range").build();

                    datePicker.show(getFragmentManager(), "datePicker");
                    datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
                        @Override
                        public void onPositiveButtonClick(Object selection) {
                            Pair<Long, Long> selectionPair = (Pair<Long, Long>) selection;
                            selectedStart = selectionPair.first;
                            selectedEnd = selectionPair.second;

                            //ArrayList<Regime.ScheduleItem> overlaps = regime.checkOverlap();

                            boolean isOverlapping = (scheduleItem == null ? regime.checkOverlapL(selectedStart, selectedEnd) : regime.checkOverlapSI(scheduleItem));
                            if (isOverlapping) {
                                Log.v("scheduleItem_debug_", "overlaps");
                                warningImage.setVisibility(View.VISIBLE);
                                warningText.setVisibility(View.VISIBLE);
                            } else {
                                Log.v("scheduleItem_debug_", "null");
                                warningImage.setVisibility(View.INVISIBLE);
                                warningText.setVisibility(View.INVISIBLE);
                            }

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY MMM dd", Locale.getDefault());
                            datePickerButton.setText(simpleDateFormat.format(new Date(selectedStart)) + " - " + simpleDateFormat.format(new Date(selectedEnd)));
                        }
                    });
                }
            });
        }

        //TODO disclaimers, suggestions when scheduleItems overlap

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Regime.ScheduleItem scheduleItemL;
                        if(scheduleItem == null) {
                            scheduleItemL = regime.addScheduleItem(new Date(selectedStart), new Date(selectedEnd), activeSwitch.isChecked());
                            if (regime.checkOverlapL(selectedStart, selectedEnd)) {
                                regime.resolveConflicts();
                            }
                        } else {
                            scheduleItem.start.setTime(selectedStart);
                            scheduleItem.end.setTime(selectedEnd);
                            scheduleItem.isActive = activeSwitch.isChecked();
                            if (regime.checkOverlapL(selectedStart, selectedEnd)) {
                                regime.resolveConflicts();
                            }
                            scheduleItemL = scheduleItem;
                        }
                        regime.refreshRegimesDays(getContext(), scheduleItemL);
                    }
                }).start();
                if(scheduleItem == null){
                    scheduleAdapter.notifyDataSetChanged();
                } else {
                    scheduleAdapter.notifyItemChanged(scheduleAdapter.schedule.indexOf(scheduleItem));
                }
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }
}


class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    ArrayList<Regime.ScheduleItem> schedule;
    LayoutInflater inflater;
    ItemClickListener clickListener;
    ItemLongClickListener longClickListener;
    Context context;

    ScheduleAdapter(Context context, ArrayList<Regime.ScheduleItem> schedule) {
        this.inflater = LayoutInflater.from(context);
        this.schedule = schedule;
        this.context = context;
        Log.v("scheduleItem_debug", "" + schedule.size());
    }

    @NonNull
    @Override
    public ScheduleAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.schedule_item, parent, false);
        Log.v("scheduleItem_debug", "onCreateViewHolder)");
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Regime.ScheduleItem scheduleItem = schedule.get(position);
        View view = holder.itemView;
        TextView scheduleStatus = view.findViewById(R.id.scheduleTextView1);
        TextView scheduleStart = view.findViewById(R.id.scheduleTextView3);
        TextView scheduleEnd = view.findViewById(R.id.scheduleTextView5);

        Log.v("scheduleItem_debug", scheduleItem.start.toString());

        if(scheduleItem.isActive) {
            scheduleStatus.setText(R.string.regime_dialog_schedule_on);
        } else {
            scheduleStatus.setText(R.string.regime_dialog_schedule_off);
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("YYYY MMM dd", Locale.getDefault());
        if(scheduleItem.end.getTime() == 0){
            TextView scheduleFrom = view.findViewById(R.id.scheduleTextView2);
            scheduleFrom.setText(R.string.regime_dialog_schedule_default);
            scheduleStart.setVisibility(View.INVISIBLE);
            scheduleEnd.setVisibility(View.INVISIBLE);
            view.findViewById(R.id.scheduleTextView5).setVisibility(View.INVISIBLE);
            view.findViewById(R.id.scheduleTextView4).setVisibility(View.INVISIBLE);
            //scheduleEnd.setText(R.string.regime_dialog_schedule_forever);
        } else {
            scheduleStart.setText(simpleDateFormat.format(scheduleItem.start));
            scheduleEnd.setText(simpleDateFormat.format(scheduleItem.end));
        }
    }

    @Override
    public int getItemCount() {
        return schedule.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(view, getAdapterPosition());
            }
            return true;
        }
    }

    public Regime.ScheduleItem getItem(int position) {
        return schedule.get(position);
    }
    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }
    void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.longClickListener = itemLongClickListener;
    }
    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    public interface ItemLongClickListener{
        void onItemLongClick(View view, int position);
    }
}
