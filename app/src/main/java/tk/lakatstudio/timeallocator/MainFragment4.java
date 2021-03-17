package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import java.util.Random;

public class MainFragment4 extends Fragment {

    ListView pickerList;

    ArrayAdapter<ActivityType> pickerAdapter;

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
        pickerAdapter = new ArrayAdapter<ActivityType>(fragment.getContext(), R.layout.spinner_item, ActivityType.allActivityTypes){
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
        pickerList.setAdapter(pickerAdapter);
        pickerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                activityTypeAdd(i, fragment);
            }
        });
        pickerList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                ActivityType.allActivityTypes.remove(ActivityType.allActivityTypes.get(i));
                                pickerAdapter.notifyDataSetChanged();
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                };

                AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
                builder.setMessage(getString(R.string.remove_activity)).setPositiveButton(getString(R.string.yes), dialogClickListener).setNegativeButton(getString(R.string.no), dialogClickListener).show();
                return true;
            }
        });
    }

    void activityTypeAdd(int index, Fragment fragment){
        final AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.activity_edit_dialog, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();
        final EditText editText = dialogView.findViewById(R.id.activityEditName);
        final EditText editTimeText = dialogView.findViewById(R.id.activityEditTime);
        Button done = dialogView.findViewById(R.id.activityEditDone);

        final ActivityType activityType;
        if(index != -1) {
            activityType = ActivityType.allActivityTypes.get(index);
            editText.setText(activityType.name);
            if(activityType.preferredLength > 0) {
                editTimeText.setText(String.valueOf(activityType.preferredLength));
            }
        } else {
            activityType = ActivityType.addActivityType("", 0);
            editText.setHint(getString(R.string.activity_name));
        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityType.name = editText.getText().toString();
                activityType.color = getResources().getIntArray(R.array.default_colors)[Math.abs(new Random().nextInt()) % getResources().getIntArray(R.array.default_colors).length];
                //editText.setText(activityType.name);
                if(editTimeText.getText().length() > 0) {
                    activityType.preferredLength = Integer.parseInt(editTimeText.getText().toString());
                } else {
                    activityType.preferredLength = -1;
                }
                alertDialog.cancel();
                pickerAdapter.notifyDataSetChanged();
            }
        });

        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                if(activityType.name.length() == 0){
                    ActivityType.allActivityTypes.remove(activityType);
                }
            }
        });

        alertDialog.show();
    }
}
