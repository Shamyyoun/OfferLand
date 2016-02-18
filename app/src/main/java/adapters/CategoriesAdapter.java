package adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.offerland.app.R;

import java.util.ArrayList;
import java.util.List;

import datamodels.Category;

public class CategoriesAdapter extends ArrayAdapter<Category> {
    private Context context;
    private int layoutResourceId;
    private List<Category> categories = null;
    private ViewHolder[] viewHolders;
    private Typeface typeface;

    public CategoriesAdapter(Context context, int layoutResourceId, List<Category> categories) {
        super(context, layoutResourceId, categories);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.categories = categories;
        viewHolders = new ViewHolder[categories.size()];
        typeface = Typeface.createFromAsset(context.getAssets(), "roboto_l.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new ViewHolder();

            holder.textTitle = (TextView) row.findViewById(R.id.text_title);
            holder.checkBox = (CheckBox) row.findViewById(R.id.checkBox);
            holder.textTitle.setTypeface(typeface);

            row.setTag(holder);

        } else {
            holder = (ViewHolder) row.getTag();
        }

        // set data
        final Category category = categories.get(position);
        holder.textTitle.setText(category.getTitle());
        holder.checkBox.setChecked(category.isChecked());

        // add listeners
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean check = !category.isChecked();
                category.setChecked(check);
                holder.checkBox.setChecked(check);
            }
        });
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                category.setChecked(isChecked);
            }
        });

        viewHolders[position] = holder;
        return row;
    }

    static class ViewHolder {
        TextView textTitle;
        CheckBox checkBox;
    }

    /**
     * method, used to return list of checked items
     */
    public List<Category> getCheckedItems() {
        List<Category> checkedItems = new ArrayList<>();
        for (Category category : categories) {
            if (category.isChecked()) {
                checkedItems.add(category);
            }
        }

        return checkedItems;
    }
}
