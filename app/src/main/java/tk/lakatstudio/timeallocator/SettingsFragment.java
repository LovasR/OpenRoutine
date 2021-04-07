package tk.lakatstudio.timeallocator;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // create ContextThemeWrapper from the original Activity Context with the custom theme
        //final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.SettingsTheme);

        // clone the inflater using the ContextThemeWrapper
        //LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        // inflate the layout using the cloned inflater, not default inflater
        //return localInflater.inflate(R.layout.yourLayout, container, false);
        container.getContext().setTheme(R.style.SettingsTheme);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        ListPreference hourFormatP = findPreference("hour_format");
        //hourFormatP.setValueIndex(0);
        Preference colorPreference = findPreference("color");
        EditTextPreference editTextPreference = findPreference("date_format");


        editTextPreference.setOnBindEditTextListener(new EditTextPreference.OnBindEditTextListener() {
            @Override
            public void onBindEditText(@NonNull EditText editText) {
                final String allowedCharacters = getString(R.string.allowed_characters_date_format);
                InputFilter inputFilter = new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence in, int start, int end, Spanned spanned, int i2, int i3) {
                        //filter allows only day, month, year related chars or when in quotes according to the SimpleDateFormat formatting
                        StringBuilder builder = new StringBuilder();
                        boolean inQuotes = false;
                        for (int i = start; i < end; i++) {
                            char c = in.charAt(i);
                            if (allowedCharacters.contains(String.valueOf(c)) || inQuotes || c == '\'') {
                                builder.append(c);
                                if(c == '\''){
                                    inQuotes =! inQuotes;
                                }
                            }
                        }

                        // If all characters are valid, return null, otherwise only return the filtered characters
                        boolean allCharactersValid = (builder.length() == end - start);
                        return allCharactersValid ? null : builder.toString();
                    }
                };
                InputFilter[] filters = new InputFilter[]{inputFilter};
                editText.setFilters(filters);
            }
        });
    }
}
