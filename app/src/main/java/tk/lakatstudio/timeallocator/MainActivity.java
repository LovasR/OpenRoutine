package tk.lakatstudio.timeallocator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner testSpinner = findViewById(R.id.testCycleSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.cycles, android.R.layout.simple_spinner_item);
        for(int i = 0; i < ActivityType.userActivityTypes.size(); i++) {
            adapter.add(ActivityType.userActivityTypes.get(i).name);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        testSpinner.setAdapter(adapter);
        testSpinner.setSelection(4);


        //notif_test
        ATNotificationManager.notif(this);

        for(String aTName : getResources().getStringArray(R.array.default_activities)){
            Log.e("AT_test", aTName);
            ActivityType.addActivityType(aTName, Color.valueOf(Color.parseColor("#883FBF5F")));
        }

        dayPlannerInit(Integer.parseInt(testSpinner.getSelectedItem().toString()));
    }

    void dayPlannerInit(int cycleLength){
        ListView dayPlanner = findViewById(R.id.testDayPlanner);

        final ArrayList<String> items = new ArrayList<String>();
        items.addAll(Arrays.asList(getResources().getStringArray(R.array.default_activities)));
        for(ActivityType at : ActivityType.userActivityTypes) {
            items.add(at.name);
        }

        //TODO replace adapter<spinner> with adapter<string> and init spinners adapters in getView
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.dayplanner_item, getResources().getStringArray(R.array.default_activities)){
            @RequiresApi(api = Build.VERSION_CODES.O)
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView==null){
                    convertView = getLayoutInflater().inflate(R.layout.dayplanner_item, null);
                }
                Spinner spinner = convertView.findViewById(R.id.dayPlannerItemSpinner);
                ArrayAdapter adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items){
                    @NonNull
                    @Override
                    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                        if(convertView==null){
                            convertView = getLayoutInflater().inflate(R.layout.spinner_dropdown_item, null);
                        }
                        TextView textView = (TextView) convertView;
                        convertView.setBackground(getDrawable(R.drawable.spinner_background));
                        textView.setText(items.get(position));
                        //convertView.setBackgroundColor(ActivityType.allActivityTypes.get(position).color.toArgb());
                        return convertView;
                    }
                };
                adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                spinner.getBackground().setColorFilter(Color.parseColor("#00000000"), PorterDuff.Mode.SRC);
                spinner.setPopupBackgroundDrawable(getDrawable(R.drawable.spinner_background));

                spinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                    }
                });
                spinner.setAdapter(adapter);
                return convertView;
            }
        };
        dayPlanner.setAdapter(arrayAdapter);
    }
}