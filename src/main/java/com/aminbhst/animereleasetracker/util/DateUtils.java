package com.aminbhst.animereleasetracker.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.MonthDay;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Slf4j
public class DateUtils {

    public static String getCurrentSeason() {
        java.util.Date now = new Date();
        ZoneId tz = ZoneId.systemDefault();
        ZonedDateTime zdt = now.toInstant().atZone(tz);
        MonthDay md = MonthDay.of(zdt.getMonth(), zdt.getDayOfMonth());

        MonthDay beginOfSpring = MonthDay.of(3, 1);
        MonthDay beginOfSummer = MonthDay.of(5, 1);
        MonthDay beginOfAutumn = MonthDay.of(9, 1);
        MonthDay beginOfWinter = MonthDay.of(11, 1);
        String season;
        if (md.isBefore(beginOfSpring)) {
            season = Seasons.WINTER;
        } else if (md.isBefore(beginOfSummer)) {
            season = Seasons.SPRING;
        } else if (md.isBefore(beginOfAutumn)) {
            season = Seasons.SUMMER;
        } else if (md.isBefore(beginOfWinter)) {
            season = Seasons.FALL;
        } else {
            season = Seasons.WINTER;
        }

        return season;
    }


    public static Calendar calendarFromSimpleDateStr(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(sdf.parse(dateStr));
            return cal;
        } catch (ParseException e) {
            log.error("Failed to parse date!", e);
        }
        return null;
    }

    public static boolean isToday(Calendar targetCal) {
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTime(new Date());
        return nowCal.get(Calendar.DAY_OF_YEAR) == targetCal.get(Calendar.DAY_OF_YEAR) &&
                nowCal.get(Calendar.YEAR) == targetCal.get(Calendar.YEAR);
    }

    public static int getCurrentYear() {
        return Year.now().getValue();
    }
}
