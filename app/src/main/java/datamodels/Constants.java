package datamodels;

/**
 * Created by Shamyyoun on 6/24/2015.
 */
public class Constants {
    // json response constants
    public static final String JSON_MSG_FALSE = "false";
    public static final String JSON_MSG_TRUE = "true";

    // keys
    public static final String KEY_FORGET_ME = "forget_me";
    public static final String KEY_CATEGORY = "category";
    public static final String KEY_OFFER = "offer";
    public static final String KEY_STORE = "store";

    // receivers request codes
    public static final int RECEIVER_LOCATION_UPDATER = 1;

    // menu drawer static items IDs
    public static final int MENU_DRAWER_ITEM_INTEREST_LIST = -1;
    public static final int MENU_DRAWER_ITEM_OFFERS_NEAR_ME = -2;
    public static final int MENU_DRAWER_ITEM_STORES_MAP = -3;
    public static final int MENU_DRAWER_ITEM_SEARCH = -4;
    public static final int MENU_DRAWER_ITEM_LOGOUT = -5;

    // activity request codes
    public static final int REQUEST_UPDATE_INTEREST_LIST = 1;
    public static final int REQUEST_REQUEST_OFFER = 2;
}
