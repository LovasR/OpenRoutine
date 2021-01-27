package tk.lakatstudio.timeallocator;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class OtherActivityDialog extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.notification_other_activity));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice, getResources().getStringArray(R.array.default_activities));
        //TODO add users activities
        //arrayAdapter.add("Hardik");

        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String activityName = arrayAdapter.getItem(i);
                Log.v("Notification_test", activityName);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(getIntent().getIntExtra("notificationID", 0));

                if(!OtherActivityDialog.this.isFinishing()){
                    finish();
                }
            }
        });
        builder.create();
        AlertDialog dialog = builder.show();
    }


    public void onCancel(DialogInterface dialogInterface) {
        if(!OtherActivityDialog.this.isFinishing()){
            finish();
        }
    }

    public void onDismiss(DialogInterface dialogInterface) {
        if(!OtherActivityDialog.this.isFinishing()){
            finish();
        }
    }
}
