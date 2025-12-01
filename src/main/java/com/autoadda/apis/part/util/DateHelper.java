package com.autoadda.apis.part.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateHelper {

    public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

    // Private constructor to hide the implicit public one
    private DateHelper() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static Date dbTimeStampp() {
        SimpleDateFormat sdfAmerica = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_SSS);
        SimpleDateFormat formatter = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_SSS);
        TimeZone tzAmerica = TimeZone.getTimeZone("America/New_York");
        sdfAmerica.setTimeZone(tzAmerica);
        Date date = new Date();
        String dateAmerica = sdfAmerica.format(date);
        Date dateInAmerica = null;
        try {
            dateInAmerica = formatter.parse(dateAmerica);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return dateInAmerica;
    }


    public static Date getVerifyEndDate() {
        SimpleDateFormat sdfAmerica = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_SSS);
        SimpleDateFormat formatter = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_SSS);
        Date date = new Date();
        Date dateInAmerica = null;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 1);
        String strDate = sdfAmerica.format(c.getTime());
        try {
            dateInAmerica = formatter.parse(strDate);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return dateInAmerica;
    }

    public static Date getDateWith90Days() {
        SimpleDateFormat sdfAmerica = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_SSS);
        SimpleDateFormat formatter = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS_SSS);
        Date date = new Date();
        Date dateInAmerica = null;
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, 90);
        String strDate = sdfAmerica.format(c.getTime());
        try {
            dateInAmerica = formatter.parse(strDate);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return dateInAmerica;
    }
}