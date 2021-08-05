package tk.lakatstudio.timeallocator;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settingsFrame, new SettingsFragment())
                .commit();

        Button seeCode = findViewById(R.id.settingsCode);
        seeCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.github.com"));
                startActivity(intent);
            }
        });
        Button donate = findViewById(R.id.settingsDonate);
        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                donationDialog(SettingsActivity.this);
            }
        });
    }

    void donationDialog(Context context){
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = getLayoutInflater().inflate(R.layout.donation_dialog, null);
        builder.setView(dialogView);
        final AlertDialog alertDialog = builder.create();

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final Button moneroCopy = dialogView.findViewById(R.id.donationMoneroCopy);
        final Button bitcoinCopy = dialogView.findViewById(R.id.donationBitcoinCopy);
        final Button kofiView = dialogView.findViewById(R.id.donationKofi);
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        moneroCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("address", getString(R.string.monero_address));
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, getString(R.string.monero_copied), Toast.LENGTH_SHORT).show();
            }
        });

        bitcoinCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("address", getString(R.string.bitcoin_address));
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, getString(R.string.bitcoin_copied), Toast.LENGTH_SHORT).show();
            }
        });

        kofiView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.kofi_link)));
                startActivity(intent);
            }
        });

        alertDialog.show();
    }
}
