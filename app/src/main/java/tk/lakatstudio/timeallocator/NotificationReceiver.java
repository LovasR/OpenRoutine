package tk.lakatstudio.timeallocator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

    private static String CHANNEL_ID = "ATNotifications";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("notification", "onReceive NotifRec");
        ATNotificationManager.dayItemNotification(context, intent.getStringExtra("dayItemID"),
                intent.getStringExtra("dayItemTypeName"), intent.getLongExtra("dayItemStart", -1),
                intent.getIntExtra("requestID", -1));
    }
}
