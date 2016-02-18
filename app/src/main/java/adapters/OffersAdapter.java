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
import com.squareup.picasso.Picasso;

import java.util.List;

import datamodels.Offer;

/**
 * Created by Shamyyoun on 2/8/2015.
 */
public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.ViewHolder> {
    private Context context;
    private List<Offer> data;
    private int layoutResourceId;
    private Typeface typefaceLight;
    private Typeface typefaceMedium;

    private OnItemClickListener onItemClickListener;

    public OffersAdapter(Context context, List<Offer> data, int layoutResourceId) {
        this.context = context;
        this.data = data;
        this.layoutResourceId = layoutResourceId;
        typefaceLight = Typeface.createFromAsset(context.getAssets(), "roboto_l.ttf");
        typefaceMedium = Typeface.createFromAsset(context.getAssets(), "roboto_m.ttf");
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Offer offer = data.get(position);

        // load images
        if (!offer.getStorePhoto().isEmpty())
            Picasso.with(context).load(offer.getStorePhoto()).into(holder.imageStorePhoto);
        if (!offer.getImage().isEmpty())
            Picasso.with(context).load(offer.getImage()).into(holder.imageOfferImage);

        // set text data
        holder.textDesc.setText(offer.getDesc());
        if (holder.textStoreName != null) {
            holder.textStoreName.setText(offer.getStoreName());
            holder.textTime.setText(offer.getFormattedDate());
        } else {
            // check to add top margin
            if (position == 0) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.rootView.getLayoutParams();
                layoutParams.topMargin = 0;
                holder.rootView.setLayoutParams(layoutParams);
            }
        }
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
        public View rootView;
        public ImageView imageStorePhoto;
        public TextView textStoreName;
        public TextView textTime;
        public TextView textDesc;
        private ImageView imageOfferImage;

        public ViewHolder(View v) {
            super(v);
            rootView = v;
            imageStorePhoto = (ImageView) v.findViewById(R.id.image_storePhoto);
            textStoreName = (TextView) v.findViewById(R.id.text_storeName);
            textTime = (TextView) v.findViewById(R.id.text_time);
            textDesc = (TextView) v.findViewById(R.id.text_desc);
            imageOfferImage = (ImageView) v.findViewById(R.id.image_offerImage);

            // customize fonts
            if (textStoreName != null) {
                textStoreName.setTypeface(typefaceMedium);
                textTime.setTypeface(typefaceLight);
            }
            textDesc.setTypeface(typefaceLight);

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