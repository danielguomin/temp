package com.miyuan.smarthome.temp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtils {

    public static final String DEFAULT_YMD = "yyyy-MM-dd";

    public static final String DEFAULT_HMS = "HH:mm:ss";
    public static final String DEFAULT_H = "HH";

    private static SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_YMD);
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

    public static boolean isYesterday(Date date) {
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - 1);
        Date yesterday = calendar.getTime();
        String nowDay = sdf.format(yesterday);
        String day = sdf.format(date);
        return nowDay.equals(day);
    }


    public static String getNormal() {
        return sdfNormal.format(new Date());
    }

    public static String getHourStr(Date date) {
        sdfTime.setTimeZone(TimeZone.getDefault());
        return sdfTime.format(date);
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
}
