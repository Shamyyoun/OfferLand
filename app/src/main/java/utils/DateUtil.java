package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Shamyyoun on 6/24/2015.
 */
public class DateUtil {
    /**
     * method, used to convert string to date
     */
    public static Date convertToDate(String strDate, String strFormat) {
        Date date;
        try {
            SimpleDateFormat format = new SimpleDateFormat(strFormat);
            date = format.parse(strDate);
        } catch (ParseException e) {
            date = null;
            e.printStackTrace();
        }

        return date;
    }

    /**
     * method, used to convert date to string
     */
    public static String convertToString(Date date, String strFormat) {
        String strDate;
        try {
            SimpleDateFormat format = new SimpleDateFormat(strFormat);
            strDate = format.format(date);
        } catch (Exception e) {
            strDate = null;
            e.printStackTrace();
        }

        return strDate;
    }
}
