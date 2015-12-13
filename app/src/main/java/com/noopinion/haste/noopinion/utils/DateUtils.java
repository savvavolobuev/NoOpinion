package com.noopinion.haste.noopinion.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by haste on 13.12.15.
 */
public class DateUtils {

    public static String parseDate(long date) {

        if (date != 0L) {
            SimpleDateFormat format = new SimpleDateFormat("kk:mm dd.MM.yyyy");
            return format.format(new Date(date*1000)).toString();
        } else {
            return "";
        }
    }
}
