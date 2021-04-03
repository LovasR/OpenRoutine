package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DayItemAdapter extends RecyclerView.Adapter<DayItemAdapter.ViewHolder> {

    ArrayList<DayItem> dayItems;
    LayoutInflater inflater;
    ItemClickListener clickListener;
    ItemLongClickListener longClickListener;
    Context context;

    DayFragment dayFragment;

    DayItemAdapter(Context context, ArrayList<DayItem> dayItems, DayFragment dayFragment) {
        this.inflater = LayoutInflater.from(context);
        this.dayItems = dayItems;
        Log.v("recyclerView_test", dayItems.size() + "");
        this.context = context;
        this.dayFragment = dayFragment;
    }

    void refreshContents(ArrayList<DayItem> newDayItems){
        dayItems = newDayItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.dayplanner_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final DayItem dayItem = dayItems.get(position);
        View itemRoot = holder.itemView;
        Log.v("recyclerView_test", "day_item_position" + position);

        TextView itemStart = itemRoot.findViewById(R.id.dayPlannerItemStart);
        itemStart.setText(new SimpleDateFormat(DayInit.getHourFormat(context), Locale.getDefault()).format(dayItem.start.getTime()));

        TextView itemLength = itemRoot.findViewById(R.id.dayPlannerItemLength);
        itemLength.setText(dayFragment.lengthAdapter(dayItem.start, dayItem.end));

        TextView itemName = itemRoot.findViewById(R.id.dayPlannerItem);
        if (dayItem.name.length() != 0){
            itemName.setText(dayItem.name);
            itemName.setVisibility(View.VISIBLE);
        } else {
            itemName.setVisibility(View.GONE);
        }

        TextView itemType = itemRoot.findViewById(R.id.dayPlannerItemActivity);
        itemType.setText(dayItem.type.name);
        final Drawable textBackground = AppCompatResources.getDrawable(context, R.drawable.spinner_background);
        textBackground.setColorFilter(dayItem.type.color, PorterDuff.Mode.SRC);
        itemType.setBackground(textBackground);
    }

    @Override
    public int getItemCount() {
        return dayItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(view, getAdapterPosition());
            }
            return true;
        }
    }

    public DayItem getItem(int position) {
        return dayItems.get(position);
    }
    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }
    void setLongClickListener(ItemLongClickListener itemLongClickListener) {
        this.longClickListener = itemLongClickListener;
    }
    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
    public interface ItemLongClickListener{
        void onItemLongClick(View view, int position);
    }
}
