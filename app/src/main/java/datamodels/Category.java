package datamodels;

import java.io.Serializable;

/**
 * Created by Shamyyoun on 6/26/2015.
 */
public class Category implements Serializable {
    private int id;
    private String title;
    private int iconResId = -1;

    private boolean checked; // used for display purposes in list view

    public Category(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public Category(int id, String title, int iconResId) {
        this.id = id;
        this.title = title;
        this.iconResId = iconResId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
