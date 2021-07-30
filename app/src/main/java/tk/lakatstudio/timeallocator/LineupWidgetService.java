package tk.lakatstudio.timeallocator;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class LineupWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        System.out.println("factory_debug:\t" + "service start");
        return new LineupWidgetRemoteViewFactory(this.getApplicationContext(), intent);
    }
}
