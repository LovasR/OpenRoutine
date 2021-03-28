package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainFragment3 extends Fragment {

    ListView regimeList;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_3, container, false);

        regimeList = view.findViewById(R.id.regimeListview);
        Log.e("UI_test", "oncreateview_fragment3");

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
            Log.e("regime_test", "regime size 0");
            return;
        }
        ArrayAdapter<Regime> arrayAdapter = new ArrayAdapter<Regime>(fragment.getContext(), R.layout.regime_item, Regime.allRegimes){
            @NonNull
            @Override
            public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.regime_item, null);
                }

                final Regime regime = Regime.allRegimes.get(position);

                //if regime is going to be deleted, don`t show anything
                if(regime.toDelete){
                    return new View(getContext());
                }

                Log.e("regime_test", "regime list init " + regime.name);

                TextView itemName = convertView.findViewById(R.id.regimeItemName);
                itemName.setText(regime.name);

                SwitchMaterial itemActive = convertView.findViewById(R.id.regimeItemActive);
                itemActive.setChecked(regime.isActive);
                itemActive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        regime.isActive = b;
                    }
                });

                LinearLayout itemLayout = convertView.findViewById(R.id.regimeItemLinearLayout);
                itemLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent editIntent = new Intent(fragment.getContext(), RegimeActivity.class);
                        editIntent.putExtra("regimeIndex", position);
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
                                dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
                        return true;
                    }
                });

                return convertView;
            }
        };
        regimeList.setAdapter(arrayAdapter);
    }

}
