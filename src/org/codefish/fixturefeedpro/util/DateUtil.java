/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.codefish.fixturefeedpro.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * date utilities
 * @author Matthew
 */
public class DateUtil {
    private static Calendar CALENDAR = Calendar.getInstance(Locale.ENGLISH);

    /**
     *
     * @param daysAgo how many days ago we want the date for
     * @return
     */
    public static Date getDaysAgo(int daysAgo) {
        Calendar cal = CALENDAR;
        synchronized (cal) {
            cal.setTime(new Date());
            //take away how ever many days we want
            cal.add(Calendar.DATE, -daysAgo);
            //return the date object
            return cal.getTime();
        }
    }

    /**
     * get a date object representing the start of the day
     * @return
     */
    public static Date startOfDay() {
        Calendar calendar = CALENDAR;
        synchronized (calendar) {
            calendar.setTime(new Date());
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MINUTE, 0);
            return calendar.getTime();
        }
    }

    /**
     * set the time of a date with that of another date
     * @param date
     * @param time
     * @return
     */
    public static Date setTime(Date date, Date time) {
        Calendar calendar = CALENDAR;
        Calendar calendar2 = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTime(date);
        calendar2.setTime(time);
        //set the time of the original date with that from the time-only date and return it
        calendar.set(Calendar.HOUR_OF_DAY, calendar2.get(Calendar.HOUR_OF_DAY));
        calendar.set(Calendar.MILLISECOND,  calendar2.get(Calendar.MILLISECOND));
        calendar.set(Calendar.SECOND,  calendar2.get(Calendar.SECOND));
        calendar.set(Calendar.MINUTE,  calendar2.get(Calendar.MINUTE));
        return calendar.getTime();
    }

        /**
     * <p>Checks if two dates are on the same day ignoring time.</p>
     * @param date1  the first date, not altered, not null
     * @param date2  the second date, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either date is <code>null</code>
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    /**
     * <p>Checks if two calendars represent the same day ignoring time.</p>
     * @param cal1  the first calendar, not altered, not null
     * @param cal2  the second calendar, not altered, not null
     * @return true if they represent the same day
     * @throws IllegalArgumentException if either calendar is <code>null</code>
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * <p>Checks if a date is today.</p>
     * @param date the date, not altered, not null.
     * @return true if the date is today.
     * @throws IllegalArgumentException if the date is <code>null</code>
     */
    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }
}
