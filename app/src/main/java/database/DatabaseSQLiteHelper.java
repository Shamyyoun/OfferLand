package database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseSQLiteHelper extends SQLiteOpenHelper {
    private Context context;

    // database info
    private static final String DATABASE_NAME = "offer_land.db";
    private static final int DATABASE_VERSION = 3;

    // table users
    public static final String TABLE_USERS = "users";
    public static final String USERS_ID = "_id";
    public static final String USERS_EMAIL = "email";
    public static final String USERS_PHONE = "phone";
    public static final String USERS_PASSWORD = "password";
    public static final String USERS_FIRST_NAME = "first_name";
    public static final String USERS_LAST_NAME = "last_name";
    public static final String USERS_PHOTO = "photo";
    public static final String USERS_BIRTH_DATE = "birth_date";
    public static final String USERS_REG_ID = "reg_id";

    // table categories
    public static final String TABLE_CATEGORIES = "categories";
    public static final String CATEGORIES_ID = "_id";
    public static final String CATEGORIES_TITLE = "title";

    // table interest list
    public static final String TABLE_INTEREST_LIST = "interest_list";
    // column names is the same as categories table

    // tables creation
    private static final String USERS_CREATE = "CREATE TABLE " + TABLE_USERS
            + "("
            + USERS_ID + " INTEGER PRIMARY KEY, "
            + USERS_EMAIL + " TEXT NOT NULL, "
            + USERS_PHONE + " TEXT, "
            + USERS_PASSWORD + " TEXT NOT NULL, "
            + USERS_FIRST_NAME + " TEXT NOT NULL, "
            + USERS_LAST_NAME + " TEXT NOT NULL, "
            + USERS_PHOTO + " TEXT, "
            + USERS_BIRTH_DATE + " TEXT NOT NULL, "
            + USERS_REG_ID + " TEXT"
            + ");";
    private static final String CATEGORIES_CREATE = "CREATE TABLE " + TABLE_CATEGORIES
            + "("
            + CATEGORIES_ID + " INTEGER PRIMARY KEY, "
            + CATEGORIES_TITLE + " TEXT NOT NULL"
            + ");";
    private static final String INTEREST_LIST_CREATE = "CREATE TABLE " + TABLE_INTEREST_LIST
            + "("
            + CATEGORIES_ID + " INTEGER PRIMARY KEY, "
            + CATEGORIES_TITLE + " TEXT NOT NULL"
            + ");";

    public DatabaseSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // create tables
        database.execSQL(USERS_CREATE);
        database.execSQL(CATEGORIES_CREATE);
        database.execSQL(INTEREST_LIST_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INTEREST_LIST);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

}