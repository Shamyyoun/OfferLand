package adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.offerland.app.R;

import java.util.List;

import datamodels.Category;

/**
 * Created by Shamyyoun on 2/8/2015.
 */
public class MenuDrawerAdapter extends RecyclerView.Adapter<MenuDrawerAdapter.ViewHolder> {
    private Context context;
    private List<Category> data;
    private int layoutResourceId;
    private Typeface typeface;

    private OnItemClickListener onItemClickListener;

    public MenuDrawerAdapter(Context context, List<Category> data, int layoutResourceId) {
        this.context = context;
        this.data = data;
        this.layoutResourceId = layoutResourceId;
        typeface = Typeface.createFromAsset(context.getAssets(), "roboto_l.ttf");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Category item = data.get(position);

        // check icon res id
        if (item.getIconResId() == -1) {
            holder.imageIcon.setImageResource(R.drawable.ic_pin);
        } else {
            holder.imageIcon.setImageResource(item.getIconResId());
        }
        holder.textTitle.setText(item.getTitle());

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(layoutResourceId, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    public void setOnItemClickListener(final OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView imageIcon;
        public TextView textTitle;

        public ViewHolder(View v) {
            super(v);
            imageIcon = (ImageView) v.findViewById(R.id.image_icon);
            textTitle = (TextView) v.findViewById(R.id.text_title);

            // customize fonts
            textTitle.setTypeface(typeface);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(v, getPosition());
            }
        }
    }

}