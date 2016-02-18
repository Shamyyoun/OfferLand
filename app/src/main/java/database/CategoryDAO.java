package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import datamodels.Category;

public class CategoryDAO {
    private String tableName = DatabaseSQLiteHelper.TABLE_CATEGORIES; // default is categories table

    private SQLiteDatabase database;
    private DatabaseSQLiteHelper dbHelper;

    public CategoryDAO(Context context) {
        dbHelper = new DatabaseSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * method, used to add list of categories to database
     */
    public void add(List<Category> categories) {
        for (Category category : categories) {
            ContentValues values = new ContentValues();
            values.put(DatabaseSQLiteHelper.CATEGORIES_ID, category.getId());
            values.put(DatabaseSQLiteHelper.CATEGORIES_TITLE, category.getTitle());

            database.insert(tableName, null, values);
        }
    }

    /**
     * method, used to delete all categories from database
     */
    public void deleteAll() {
        database.delete(tableName, null, null);
    }

    /**
     * method, used to getAll all categories from db
     */
    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        Cursor cursor = database.query(tableName, null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Category category = cursorToCategory(cursor);
            categories.add(category);
            cursor.moveToNext();
        }
        cursor.close();
        return categories;
    }

    /**
     * method, used to getAll category values from cursor row
     */
    private Category cursorToCategory(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(DatabaseSQLiteHelper.CATEGORIES_ID));
        String title = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.CATEGORIES_TITLE));

        Category category = new Category(id, title);
        return category;
    }

    /**
     * method, used to check if database has items or not based on count
     */
    public boolean hasItems() {
        Cursor mCount = database.rawQuery("SELECT COUNT(" + DatabaseSQLiteHelper.CATEGORIES_ID +
                ") FROM " + tableName, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);
        mCount.close();

        if (count == 0)
            return false;
        else
            return true;
    }

    /**
     * method, used in child class to pass new table name
     * which its structure must be the same as categories table
     */
    protected void setTableName(String tableName) {
        this.tableName = tableName;
    }
}