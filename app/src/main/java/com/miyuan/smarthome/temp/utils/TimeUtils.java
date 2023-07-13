package com.miyuan.smarthome.temp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

    public static final String DEFAULT_YMD = "yyyy-MM-dd";

    public static final String DEFAULT_HMS = "HH:mm:ss";
    public static final String DEFAULT_HM = "HH:mm";

    private static SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_YMD);
    private static SimpleDateFormat sdf_time = new SimpleDateFormat(DEFAULT_HM);
    private static SimpleDateFormat sdfTime = new SimpleDateFormat(DEFAULT_HMS);
    private static SimpleDateFormat sdfNormal = new SimpleDateFormat(DEFAULT_YMD + " " + DEFAULT_HMS);

    private static Calendar calendar = Calendar.getInstance();

    /**
     * 判断日期是不是今天
     *
     * @param date
     * @return
     */
    public static boolean isSameDay(Date date) {

        Date now = new Date();

        String nowDay = sdf.format(now);

        String day = sdf.format(date);

        return nowDay.equals(day);
    }

    public static boolean isYesterday(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        clearCalendar(calendar, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        long firstOfDay = calendar.getTimeInMillis(); // 昨天最早时间
        calendar.setTimeInMillis(timestamp);
        clearCalendar(calendar, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND); // 指定时间戳当天最早时间
        return firstOfDay == calendar.getTimeInMillis();
    }

    public static long getTimeTodayBegin(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        clearCalendar(calendar, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND); // 指定时间戳当天最早时间
        return calendar.getTimeInMillis();
    }

    private static void clearCalendar(Calendar c, int... fields) {
        for (int f : fields) {
            c.set(f, 0);
        }
    }

    public static String getTimeStr(long time) {
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(new Date(time));
    }

    public static String getNormal() {
        return sdfNormal.format(new Date());
    }

    public static String getHourStr(Date date) {
        sdfTime.setTimeZone(TimeZone.getDefault());
        return sdfTime.format(date);
    }

    public static String getHourMinStr(long time) {
        return sdf_time.format(new Date(time));
    }

    public static String getHourStrForSecond(Date date) {
        sdfTime.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        return sdfTime.format(date);
    }

    public static boolean isSameDay(Date date, Date other) {

        String otherDay = sdf.format(other);

        String day = sdf.format(date);

        return otherDay.equals(day);
    }

    public static boolean allSameDay(long date, int count, int step) {

        return false;
    }

    /**
     * 获取指定日期小时
     *
     * @param date
     * @return
     */
    public static int getHour(long date) {
        calendar.setTime(new Date(date));
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /**
     * 将时间转换秒
     *
     * @param date
     * @return
     */
    public static int getSecondForDate(long date) {
        calendar.setTime(new Date(date));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return hour * 3600 + minute * 60 + second;
    }

    public static int getSecondForCurrentHour(long date) {
        calendar.setTime(new Date(date));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour * 3600;
    }

    /**
     * 当前小时转换秒
     *
     * @return
     */
    public static int getSecondForCurrentHour() {
        calendar.setTime(new Date(System.currentTimeMillis()));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour * 3600;
    }

    public static String getTimeString(final long millisecond) {
        if (millisecond < 1000) {
            return "00:00:00";
        }
        long second = millisecond / 1000;
        long seconds = second % 60;
        long minutes = second / 60;
        long hours = 0;
        if (minutes >= 60) {
            hours = minutes / 60;
            minutes = minutes % 60;
        }
        String timeString = "";
        String secondString = "";
        String minuteString = "";
        String hourString = "";
        if (seconds < 10) {
            secondString = "0" + seconds;
        } else {
            secondString = String.valueOf(seconds);
        }
        if (minutes < 10) {
            minuteString = "0" + minutes;
        } else {
            minuteString = String.valueOf(minutes);
        }
        if (hours < 10) {
            hourString = "0" + hours;
        } else {
            hourString = String.valueOf(hours);
        }
        if (hours != 0) {
            timeString = hourString + ":" + minuteString + ":" + secondString;
        } else {
            timeString = "00:" + minuteString + ":" + secondString;
        }
        return timeString;
    }
}
