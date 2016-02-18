package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import datamodels.User;

public class UserDAO {

    private SQLiteDatabase database;
    private DatabaseSQLiteHelper dbHelper;

    public UserDAO(Context context) {
        dbHelper = new DatabaseSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * method, used to add user to database
     */
    public void add(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLiteHelper.USERS_ID, user.getId());
        values.put(DatabaseSQLiteHelper.USERS_EMAIL, user.getEmail());
        values.put(DatabaseSQLiteHelper.USERS_PHONE, user.getPhone());
        values.put(DatabaseSQLiteHelper.USERS_PASSWORD, user.getPassword());
        values.put(DatabaseSQLiteHelper.USERS_FIRST_NAME, user.getFirstName());
        values.put(DatabaseSQLiteHelper.USERS_LAST_NAME, user.getLastName());
        values.put(DatabaseSQLiteHelper.USERS_PHOTO, user.getPhoto());
        values.put(DatabaseSQLiteHelper.USERS_BIRTH_DATE, user.getBirthDateString());
        values.put(DatabaseSQLiteHelper.USERS_REG_ID, user.getRegId());

        database.insert(DatabaseSQLiteHelper.TABLE_USERS, null, values);
    }

    /**
     * method, used to update user in database
     */
    public void update(User user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseSQLiteHelper.USERS_EMAIL, user.getEmail());
        values.put(DatabaseSQLiteHelper.USERS_PHONE, user.getPhone());
        values.put(DatabaseSQLiteHelper.USERS_PASSWORD, user.getPassword());
        values.put(DatabaseSQLiteHelper.USERS_FIRST_NAME, user.getFirstName());
        values.put(DatabaseSQLiteHelper.USERS_LAST_NAME, user.getLastName());
        values.put(DatabaseSQLiteHelper.USERS_PHOTO, user.getPhoto());
        values.put(DatabaseSQLiteHelper.USERS_BIRTH_DATE, user.getBirthDateString());
        values.put(DatabaseSQLiteHelper.USERS_REG_ID, user.getRegId());

        database.update(DatabaseSQLiteHelper.TABLE_USERS, values, DatabaseSQLiteHelper.USERS_ID + " = " + user.getId(), null);
    }

    /**
     * method, used to delete all users from database
     */
    public void deleteAll() {
        database.delete(DatabaseSQLiteHelper.TABLE_USERS, null, null);
    }

    /**
     * method, used to getAll last user from db
     */
    public User get() {
        User user = null;
        Cursor cursor = database.query(DatabaseSQLiteHelper.TABLE_USERS, null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            user = cursorToUser(cursor);
            cursor.moveToNext();
        }
        cursor.close();
        return user;
    }

    /**
     * method, used to getAll user values from cursor row
     */
    private User cursorToUser(Cursor cursor) {
        int id = cursor.getInt(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_ID));
        String email = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_EMAIL));
        String phone = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_PHONE));
        String password = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_PASSWORD));
        String firstName = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_FIRST_NAME));
        String lastName = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_LAST_NAME));
        String photo = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_PHOTO));
        String birthDate = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_BIRTH_DATE));
        String regId = cursor.getString(cursor.getColumnIndex(DatabaseSQLiteHelper.USERS_REG_ID));

        User user = new User(id);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoto(photo);
        user.setBirthDate(birthDate);
        user.setRegId(regId);

        return user;
    }
}