package tk.lakatstudio.timeallocator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat implements ActivityResultCallback {

    ActivityResultLauncher<String> requestPermissionLauncher;
    ActivityResultLauncher<String> getExportPath;
    ActivityResultLauncher<String[]> getImportPath;

    boolean isPermissionForExport = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // create ContextThemeWrapper from the original Activity Context with the custom theme
        //final Context contextThemeWrapper = new ContextThemeWrapper(getActivity(), R.style.SettingsTheme);

        // clone the inflater using the ContextThemeWrapper
        //LayoutInflater localInflater = inflater.cloneInContext(contextThemeWrapper);

        // inflate the layout using the cloned inflater, not default inflater
        //return localInflater.inflate(R.layout.yourLayout, container, false);
        container.getContext().setTheme(R.style.SettingsTheme);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean isGranted) {
                if (isPermissionForExport) {
                    if (isGranted) {
                        getOutputPath();
                    } else {
                        // Explain to the user that the feature is unavailable because the
                        // features requires a permission that the user has denied. At the
                        // same time, respect the user's decision. Don't link to system
                        // settings in an effort to convince the user to change their
                        // decision.
                    }
                } else {
                    if (isGranted) {
                        getImportPath.launch(new String[]{"*/*"});
                    } else {
                        //not granted for import
                    }
                }
            }
        });

        getExportPath = registerForActivityResult(new ActivityResultContracts.CreateDocument(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        try {
                            Log.v("export_data", uri.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        DayInit.exportData(requireContext(), uri);
                    }
                });
        getImportPath = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                try {
                    Log.v("export_data", uri.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DayInit.importData(requireContext(), uri);
            }
        });
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        ListPreference hourFormatP = findPreference("hour_format");
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

        Preference darkPreference = findPreference("dark_mode");
        darkPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                DayInit.setSelectedTheme(newValue);
                return true;
            }
        });

        Preference exportPreference = findPreference("export_data");
        exportPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    isPermissionForExport = true;
                    requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                } else {
                    getOutputPath();
                }
                return false;
            }
        });
        Preference importPreference = findPreference("import_data");
        importPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener(){
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    isPermissionForExport = false;
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                } else {
                    getImportPath.launch(new String[]{"*/*"});
                }
                return false;
            }
        });
    }
    @Override
    public void onActivityResult(Object result) {

    }
    String getOutputPath() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");

        getExportPath.launch("OpenRoutine_Backup.zip");


        return null;
    }
}
