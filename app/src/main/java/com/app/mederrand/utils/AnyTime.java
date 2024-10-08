package com.app.mederrand.utils;

import android.text.TextUtils;
import android.util.Log;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/********** Developed Zoey **********
 * Created by : Zoey on 13/08/2021 at 11:37 AM
 ******************************************************/

public class AnyTime {

    public static final int DAY_IN_MILLIS = 86400000;
    public static final int HOUR_IN_MILLIS = 3600000;
    public static final int SEC_IN_MILLIS = 1000;
    public static final int MIN_IN_MILLIS = 60000;
    public static final long MONTH_IN_MILLIS = 2629743000L;
    public static final long YEAR_IN_MILLIS = 31556926000L;
    public static final int WEEK_IN_MILLIS = 604800000;
    final TimeZone timeZone;
    final int standardTime;
    final int dayLightSaving;
    final int netTime;

    public AnyTime() {
        timeZone = TimeZone.getDefault();
        standardTime = timeZone.getRawOffset();
        dayLightSaving = timeZone.getDSTSavings();
        netTime = standardTime + dayLightSaving;
    }


    public String getTime(long milliseconds) {

        //setup the current time
        long currentTime = System.currentTimeMillis();
        //subtract the zone time from current time
        currentTime = currentTime + netTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy-hh:mm a", Locale.UK);//UK time is UTC or 0 GMT
        String currentDate = dateFormat.format(new Date(currentTime));

        //changing to utc time according to relative timezone
        milliseconds = milliseconds + netTime;

        //setup the input time
        String inputDate = dateFormat.format(new Date(milliseconds));


        String output = "";
        long timeDiff = currentTime - milliseconds;
        //Same year
        long YEAR = YEAR_IN_MILLIS;
        if (timeDiff < YEAR) {

            //Same Month
            long MONTH = MONTH_IN_MILLIS;
            if (timeDiff < MONTH) {

                //Same Day
                long DAY = DAY_IN_MILLIS;
                if (timeDiff < DAY) {

                    //within Same Hour
                    long HOUR = HOUR_IN_MILLIS;
                    if (timeDiff < HOUR) {

                        //within same minute
                        int MIN = MIN_IN_MILLIS;
                        if (timeDiff < MIN) {

                            //seconds ago
                            int seconds = (int) (timeDiff / SEC_IN_MILLIS);
                            if (seconds < 30) {
                                output = "Just now";
                            } else {
                                output = seconds + " seconds ago";
                            }
                        } else {
                            //Minutes ago
                            int mins = (int) (timeDiff / MIN);
                            output = mins + " minutes ago";
                        }
                    } else {

                        //Hours ago
                        int hours = (int) (timeDiff / HOUR);
                        output = hours + " hours ago";
                    }

                } else { //Difference from 1 day to 1 month

                    //Same Week
                    long WEEK = WEEK_IN_MILLIS;
                    if (timeDiff < WEEK) {
                        //Days ago
                        output = (timeDiff / DAY) + " days ago";
                    } else {
                        //Weeks ago
                        output = (timeDiff / WEEK) + " weeks ago";
                    }

                }
            } else {
                //Months ago
                output = (timeDiff / MONTH) + " months ago";
            }

        } else {
            //years ago
            output = (timeDiff / YEAR) + " years ago";
        }

        return output;
    }

    // API Time : 2021-05-11T09:10:14.000000Z
    public long getMillisecondsFromApiTime(String input) {
        Date output = new Date();
        try {
            input = input.split("\\.")[0];
            Log.e("YAM", "Input : " + input);
            String[] arr = input.split("T");
            String date = arr[0];
            Log.e("YAM", "Date : " + date);
            String time = arr[1];
            Log.e("YAM", "Time : " + time);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            output = dateFormat.parse(date + " " + time);
            return Objects.requireNonNull(output).getTime() + netTime;
        } catch (Exception e) {
            Log.e("YAM", "Exception : " + e.getMessage());
            e.printStackTrace();
        }
        return Objects.requireNonNull(output).getTime() + netTime;
    }

    public String getTime(long milliseconds, String outputPattern) {
        return new SimpleDateFormat(outputPattern, Locale.getDefault()).format(new Date(milliseconds));
    }

    public long getTimeFromPattern(String time, String pattern) {
        if (TextUtils.isEmpty(time)) return 0;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(time);
            if (date == null) return 0;
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public long convertDateTimeToTimestamp(String dateTimeString, String pattern) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        Date date = sdf.parse(dateTimeString);

        if (date != null) {
            return date.getTime()/1000;
        } else {
            throw new ParseException("Error parsing date and time", 0);
        }
    }

    public String getTimeFromMillis(long millis, String pattern) {
        if (millis <= 0) return "";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, Locale.getDefault());
        return simpleDateFormat.format(new Date(millis));
    }

    public String convertToPattern(String time, String fromPattern, String toPattern) {
        if (TextUtils.isEmpty(time)) return time;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromPattern, Locale.getDefault());
        try {
            Date date = simpleDateFormat.parse(time);
            simpleDateFormat.applyPattern(toPattern);
            return date == null ? time : simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }

    public String convertToPatternByLocal(String time, String fromPattern, String toPattern, Locale locale) {
        if (TextUtils.isEmpty(time)) return time;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(fromPattern, locale);
        try {
            Date date = simpleDateFormat.parse(time);
            simpleDateFormat.applyPattern(toPattern);
            return date == null ? time : simpleDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return time;
        }
    }

    public String convertToHrsAgo(String time){
        long newTime = Long.parseLong(time);
        Date date = new Date(newTime * 1000);
        String ago = new PrettyTime().format(date);
        ago = ago.replace("moments ago", "just now");
        ago = ago.replace("minutes ago", "min ago");
        ago = ago.replace("minutes", "min");
        ago = ago.replace("seconds", "sec");
        ago = ago.replace("hours", "h");
        ago = ago.replace("days", "d");
        ago = ago.replace("week", "w");
        ago = ago.replace("weeks", "w");
        ago = ago.replace("month", "m");
        ago = ago.replace("months", "m");
        ago = ago.replace("year", "y");
        ago = ago.replace("years", "y");
        return ago;
    }
}
