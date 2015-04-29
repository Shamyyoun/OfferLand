package offerland.com.app;

import android.app.Application;
import android.content.Context;

import utils.FontUtil;

/**
 * Created by Shamyyoun on 3/15/2015.
 */
public class AppController extends Application {
    public AppController() {
        super();
    }

    /**
     * overriden method
     */
    @Override
    public void onCreate() {
        super.onCreate();

        // override default font
        FontUtil.setDefaultFont(getApplicationContext(), "MONOSPACE", "font.ttf");
    }

    /**
     * method used to return current application instance
     */
    public static AppController getInstance(Context context) {
        return (AppController) context.getApplicationContext();
    }
}
