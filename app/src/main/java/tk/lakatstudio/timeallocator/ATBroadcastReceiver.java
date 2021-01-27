package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ATBroadcastReceiver extends android.content.BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("Notification_test", intent.toString());
        switch(intent.getAction()){
            case ATNotificationManager.ACTION_ACTION:
                Log.e("Notification_test", "action performed");
                break;
            case ATNotificationManager.ACTION_OTHER_ACTIVITY:
                Intent intent1 = new Intent(context, OtherActivityDialog.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent1.putExtra("notificationID", intent.getIntExtra("notificationID", 0));
                context.startActivity(intent1);
                break;
        }
    }
}
