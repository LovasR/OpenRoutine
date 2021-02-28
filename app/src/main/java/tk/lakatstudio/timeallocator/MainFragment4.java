package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
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
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

public class MainFragment4 extends Fragment {

    ListView pickerList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_fragment_4, container, false);

        pickerList = view.findViewById(R.id.activityListview);
        Log.e("UI_test", "oncreateview");
        activityPickerInit(this);

        return view;
    }

    void activityPickerInit(final Fragment fragment){
        ArrayAdapter<ActivityType> adapter = new ArrayAdapter<ActivityType>(fragment.getContext(), R.layout.spinner_item, ActivityType.allActivityTypes){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView==null){
                    convertView = getLayoutInflater().inflate(R.layout.spinner_item, null);
                }
                ActivityType activityType = ActivityType.allActivityTypes.get(position);

                TextView textView = (TextView) convertView;
                textView.setText(activityType.name);
                final Drawable textBackground = AppCompatResources.getDrawable(fragment.getContext(), R.drawable.spinner_background);
                textBackground.setColorFilter(activityType.color, PorterDuff.Mode.SRC);
                textView.setBackground(textBackground);

                return convertView;
            }
        };
        pickerList.setAdapter(adapter);
        pickerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }
}
