package com.miyuan.smarthome.temp.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static final String DEFAULT_YMD = "yyyy-MM-dd";

    public static final String DEFAULT_HMS = "HH:mm:ss";
    public static final String DEFAULT_H = "HH";

    private static SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_YMD);

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
        Calendar calendar = Calendar.getInstance();
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(date));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        return hour * 3600 + minute * 60 + second;
    }

    public static int getSecondForCurrentHour(long date) {
        Calendar calendar = Calendar.getInstance();
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour * 3600;
    }
}
