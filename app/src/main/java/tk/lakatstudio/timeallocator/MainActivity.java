package tk.lakatstudio.timeallocator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

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


        //TODO notif_test
        //ATNotificationManager.notif(this);

        for(String aTName : getResources().getStringArray(R.array.default_activities)){
            Log.e("AT_test", aTName);
            ActivityType.addActivityType(aTName, Color.valueOf(Color.parseColor("#883FBF5F")));
        }

        ImageButton addDayItem = findViewById(R.id.addDayItemButton);
        addDayItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, DayItemActivity.class);
                startActivity(intent);
            }
        });

        dayPlannerInit(Integer.parseInt(testSpinner.getSelectedItem().toString()));
    }

    void dayPlannerInit(int cycleLength){
        ListView dayPlanner = findViewById(R.id.testDayPlanner);

        final ArrayList<String> items = new ArrayList<String>();
        items.addAll(Arrays.asList(getResources().getStringArray(R.array.default_activities)));
        for(ActivityType at : ActivityType.userActivityTypes) {
            items.add(at.name);
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.dayplanner_item, getResources().getStringArray(R.array.default_activities)){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView==null){
                    convertView = getLayoutInflater().inflate(R.layout.dayplanner_item, null);
                }
                convertView.setOutlineProvider(ViewOutlineProvider.BACKGROUND);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(getContext(), "povo", Toast.LENGTH_SHORT).show();
                    }
                });

                //DayItem dayItem = CycleManager.currentDay.dayItems.get(position);


                TextView itemStart = convertView.findViewById(R.id.dayPlannerItemStart);
                itemStart.setText("04:20");

                TextView itemName = convertView.findViewById(R.id.dayPlannerItem);
                itemName.setText("Mi havas gefiloj en mia kelo");
                //TODO fix for ling items

                TextView itemType = convertView.findViewById(R.id.dayPlannerItemActivity);
                itemType.setText("povo aktiveco");

                Drawable drawable = getDrawable(R.drawable.spinner_background);
                drawable.setColorFilter(Color.parseColor("#3FBF5F"), PorterDuff.Mode.SRC);
                itemType.setBackground(drawable);
                return convertView;
            }
        };
        dayPlanner.setDividerHeight(10);
        dayPlanner.setAdapter(arrayAdapter);
    }
}