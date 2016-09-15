package co.touchlab.droidconandroid.utils;
import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by izzyoji :) on 8/5/15.
 */
public class TimeUtils
{
    public static  ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>()
    {
        @Override
        protected DateFormat initialValue()
        {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mma",
                    Locale.US);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            return simpleDateFormat;
        }
    };
    private static GregorianCalendar       calendar    = new GregorianCalendar();

    @NotNull
    public static Long sanitize(@NotNull Date date)
    {
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);            // set hour to midnight
        calendar.set(Calendar.MINUTE, 0);                 // set minute in hour
        calendar.set(Calendar.SECOND, 0);                 // set second in minute
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
}
