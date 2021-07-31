package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
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
        pickerAdapter = new ArrayAdapter<ActivityType>(fragment.getContext(), R.layout.activity_item, ActivityType.allActivityTypes){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if(convertView==null){
                    convertView = getLayoutInflater().inflate(R.layout.activity_item, null);
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
                builder.setMessage(getString(R.string.remove_activity, getString(R.string.activity_singular))).setPositiveButton(getString(R.string.yes), dialogClickListener)
                        .setNegativeButton(getString(R.string.no), dialogClickListener).show().getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_background);
                return true;
            }
        });
    }

    int selectedColor;

    void activityTypeAdd(int index, final Fragment fragment){
        final AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.activity_edit_dialog, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText nameEditText = dialogView.findViewById(R.id.activityEditName);
        final EditText editTimeText = dialogView.findViewById(R.id.activityEditTime);
        final Button colorPicker = dialogView.findViewById(R.id.activityEditColor);
        Button done = dialogView.findViewById(R.id.activityEditDone);


        final ActivityType activityType;
        if(index != -1) {
            activityType = ActivityType.allActivityTypes.get(index);
            nameEditText.setText(activityType.name);
            if(activityType.preferredLength > 0) {
                editTimeText.setText(String.valueOf(activityType.preferredLength));
            }
            selectedColor = activityType.color;
        } else {
            activityType = ActivityType.addActivityType("", 0);
            selectedColor = getResources().getIntArray(R.array.default_colors)[Math.abs(new Random().nextInt()) % getResources().getIntArray(R.array.default_colors).length];
        }

        Drawable drawable = ContextCompat.getDrawable(fragment.getContext(), R.drawable.spinner_background);
        drawable.setColorFilter(selectedColor, PorterDuff.Mode.SRC);
        colorPicker.setBackground(drawable);
        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPickerDialog(fragment, colorPicker, activityType);
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityType.name = nameEditText.getText().toString();
                Log.v("selectedColor", "will be: " + selectedColor);
                activityType.color = selectedColor;
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

    int customColor;

    void colorPickerDialog(final Fragment fragment, final Button colorPickerButton, final ActivityType activityType){
        final AlertDialog.Builder builder = new AlertDialog.Builder(fragment.getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.color_picker_dialog, null);
        RecyclerView colorSheet = dialogView.findViewById(R.id.colorPickerRecyclerView);
        final EditText customColorEditText = dialogView.findViewById(R.id.colorPickerCustomColor);
        final ImageView customCircle = dialogView.findViewById(R.id.colorPickerCustomCircle);
        Button done = dialogView.findViewById(R.id.colorPickerDone);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        colorSheet.setLayoutManager(new GridLayoutManager(fragment.getContext(), 3));
        ArrayList<Integer> colorList = new ArrayList<Integer>();
        for(int color : getResources().getIntArray(R.array.default_colors)) colorList.add(color);

        final ColorPickerAdapter adapter = new ColorPickerAdapter(fragment.getContext(), colorList);
        adapter.setClickListener(new ColorPickerAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //set the circle to none
                customCircle.setBackground(null);
                activityType.isColorCustom = false;

                Log.v("selectedColor", "was: " + selectedColor);
                selectedColor = adapter.getItem(position);
                Drawable drawable = ContextCompat.getDrawable(fragment.getContext(), R.drawable.spinner_background);
                drawable.setColorFilter(selectedColor, PorterDuff.Mode.SRC);
                colorPickerButton.setBackground(drawable);
                Log.v("selectedColor", "now: " + selectedColor);
            }
        });
        colorSheet.setAdapter(adapter);

        final String allowedCharacters = getString(R.string.allowed_characters_color);
        InputFilter inputFilter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence in, int start, int end, Spanned spanned, int i2, int i3) {
                //filter allows only day, month, year related chars or when in quotes according to the SimpleDateFormat formatting
                StringBuilder builder = new StringBuilder();
                for (int i = start; i < end; i++) {
                    char c = in.charAt(i);
                    if (allowedCharacters.contains(String.valueOf(c))) {
                        builder.append(c);
                    }
                }

                // If all characters are valid, return null, otherwise only return the filtered characters
                boolean allCharactersValid = (builder.length() == end - start);
                return allCharactersValid ? null : builder.toString();
            }
        };
        InputFilter[] filters = new InputFilter[]{inputFilter, new InputFilter.LengthFilter(8)};
        customColorEditText.setFilters(filters);
        customColorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int count, int end) {
                if(charSequence.length() == 6 || charSequence.length() == 8){
                    Log.v("color_test", charSequence.toString());
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.circle_24 );
                    customColor = Color.parseColor('#' + charSequence.toString());
                    drawable.setColorFilter(customColor, PorterDuff.Mode.SRC);
                    customCircle.setImageDrawable(drawable);
                    customCircle.setVisibility(View.VISIBLE);
                } else {
                    customCircle.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        customCircle.setVisibility(View.GONE);
        customCircle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.deselectAny();
                Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.selected_background);
                customCircle.setBackground(drawable);

                selectedColor = customColor;
                activityType.isColorCustom = true;
                activityType.color = selectedColor;
                drawable = ContextCompat.getDrawable(fragment.getContext(), R.drawable.spinner_background);
                drawable.setColorFilter(selectedColor, PorterDuff.Mode.SRC);
                colorPickerButton.setBackground(drawable);
            }
        });
        if(activityType.isColorCustom){
            customColor = activityType.color;
            selectedColor = customColor;
            Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.circle_24 );
            drawable.setColorFilter(customColor, PorterDuff.Mode.SRC);
            customCircle.setImageDrawable(drawable);
            customCircle.setVisibility(View.VISIBLE);

            drawable = ContextCompat.getDrawable(getContext(), R.drawable.selected_background);
            customCircle.setBackground(drawable);

            //set editText text to color
            customColorEditText.setText(String.format("#%06X", 0xFFFFFF & activityType.color));

            drawable = ContextCompat.getDrawable(fragment.getContext(), R.drawable.spinner_background);
            drawable.setColorFilter(selectedColor, PorterDuff.Mode.SRC);
            colorPickerButton.setBackground(drawable);
        }

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.cancel();
            }
        });

        alertDialog.show();
    }
}
