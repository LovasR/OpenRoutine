package tk.lakatstudio.timeallocator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import androidx.annotation.Nullable;

public class SilenceDialog extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.notification_other_activity));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, getResources().getStringArray(R.array.silence_length));


        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String activityName = arrayAdapter.getItem(i);
                Log.v("Notification_test", activityName);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(getIntent().getIntExtra("notificationID", 0));

                if(!SilenceDialog.this.isFinishing()){
                    finish();
                }
            }
        });
        builder.create();
        AlertDialog alertDialog = builder.show();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    public void onCancel(DialogInterface dialogInterface) {
        if(!SilenceDialog.this.isFinishing()){
            finish();
        }
    }

    public void onDismiss(DialogInterface dialogInterface) {
        if(!SilenceDialog.this.isFinishing()){
            finish();
        }
    }
}
