package tk.lakatstudio.timeallocator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class LineupWidgetProvider extends AppWidgetProvider {
    ListView listView;
    Context context;
    SharedPreferences sharedPreferences;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        System.out.println("factory_debug: " + "onUpdate");
        this.context = context;
        if(DayInit.sharedPreferences != null){
            sharedPreferences = DayInit.sharedPreferences;
        } else {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
        for (int appWidgetId : appWidgetIds) {
            // Create an Intent to launch ExampleActivity
            /*
            Intent rIntent = new Intent(context, MainActivity.class);
            rIntent.putExtra("dayItemStart", start);
            rIntent.putExtra("dayItemID", dayItemID.toString());
            PendingIntent resultIntent = PendingIntent.getActivity(context, 0, rIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            */
            System.out.println("factory_debug: " + "onUpdate_");

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.lineup_layout);

            Intent intent = new Intent(context, LineupWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            views.setRemoteAdapter(R.id.lineupWidgetListView, intent);

            views.setTextViewText(R.id.lineupWidgetDate, new SimpleDateFormat(sharedPreferences.getString("date_format", context.getString(R.string.default_date_format)), Locale.getDefault()).format(Calendar.getInstance().getTime()));

            Intent activityIntent = new Intent(context, MainActivity.class);
            // Set the action for the intent.
            // When the user touches a particular view.
            activityIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            activityIntent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.lineupWidgetListView, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}

class LineupWidgetRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

    Context context;
    Day day;
    ArrayList<DayItem> dayItems;

    LineupWidgetRemoteViewFactory(Context context, Intent intent){
        this.context = context;

        System.out.println("factory_debug:\t" + "setting up");
        if(DayInit.currentDay != null){
            this.day = DayInit.currentDay;
        } else {
            DayInit.initGson();
            this.day = Day.getDay(context, Calendar.getInstance().getTime());
        }
        dayItems = new ArrayList<>(day.dayItems.values());
        Day.defaultTimeSortDayItems(dayItems);
    }

    void updateWidgetData(){
        System.out.println("factory_debug:\t" + "updateing widget data");
        if(DayInit.currentDay != null){
            day = DayInit.currentDay;
        } else {
            day = Day.getDay(context, Calendar.getInstance().getTime());
        }
        dayItems = new ArrayList<>(day.dayItems.values());
        Day.defaultTimeSortDayItems(dayItems);
    }

    @Override
    public void onCreate() {
        updateWidgetData();
    }

    @Override
    public void onDataSetChanged() {
        updateWidgetData();
    }

    @Override
    public void onDestroy() {
        updateWidgetData();
    }

    @Override
    public int getCount() {
        return dayItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        DayItem dayItem = dayItems.get(position);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_dayitem_item);
        if(dayItem.name.length() != 0) {
            remoteViews.setTextViewText(R.id.dayPlannerItem, dayItem.name);
        } else {
            remoteViews.setViewVisibility(R.id.dayPlannerItem, View.GONE);
        }
        DayInit.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        remoteViews.setTextViewText(R.id.dayPlannerItemStart, new SimpleDateFormat(DayInit.getHourFormat(context), Locale.getDefault()).format(dayItem.start.getTime()));

        remoteViews.setTextViewText(R.id.dayPlannerItemActivity, dayItem.type.name);
        //final Drawable textBackground = AppCompatResources.getDrawable(context, R.drawable.spinner_background);
        //textBackground.setColorFilter(dayItem.type.color, PorterDuff.Mode.SRC);
        //itemType.setBackground(textBackground);
        //remoteViews.setBitmap(R.id.dayPlannerItemActivity, "setBackgroundResource", textBackground.);
        remoteViews.setInt(R.id.dayPlannerItemActivity, "setBackgroundColor", dayItem.type.color);

        Bundle extra = new Bundle();
        extra.putString("dayItemID", dayItem.ID.toString());
        extra.putLong("dayItemStart", dayItem.start.getTime());
        Intent rIntent = new Intent(context, MainActivity.class);
        rIntent.putExtras(extra);
        //rIntent.putExtra("dayItemID", dayItem.ID.toString());

        remoteViews.setOnClickFillInIntent(R.id.dayPlannerItemRoot, rIntent);

        return remoteViews;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
