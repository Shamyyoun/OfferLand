package database;

import android.content.Context;

/**
 * Created by Shamyyoun on 6/26/2015.
 */
public class InterestListDAO extends CategoryDAO {
    public InterestListDAO(Context context) {
        super(context);
        setTableName(DatabaseSQLiteHelper.TABLE_INTEREST_LIST);
    }
}
