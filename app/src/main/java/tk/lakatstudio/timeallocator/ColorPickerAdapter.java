package tk.lakatstudio.timeallocator;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> {

    ArrayList<Integer> colors;
    LayoutInflater inflater;
    ItemClickListener clickListener;
    Context context;

    ColorPickerAdapter(Context context, ArrayList<Integer> colors) {
        this.inflater = LayoutInflater.from(context);
        this.colors = colors;
        this.context = context;
    }

    @NonNull
    @Override
    public ColorPickerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.color_picker_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.circle_24 );
        drawable.setColorFilter(colors.get(position), PorterDuff.Mode.SRC);
        holder.imageView.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    ImageView lastClicked;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.colorPickerItem);
            itemView.setOnClickListener(this);
            lastClicked = imageView;
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                lastClicked.setBackground(null);
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.selected_background);
                imageView.setBackground(drawable);
                lastClicked = imageView;
                clickListener.onItemClick(view, getAdapterPosition());
            }
        }
    }

    public int getItem(int position) {
        return colors.get(position);
    }
    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
