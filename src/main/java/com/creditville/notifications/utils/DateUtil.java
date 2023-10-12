package com.creditville.notifications.utils;

import com.creditville.notifications.exceptions.CustomCheckedException;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Chuks on 02/09/2021.
 */
@Service
public class DateUtil {
    private Calendar convertDateToCalendar(String dateToConvert) {
        var newDate = dateToConvert.substring(0,23);
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(newDate, inputFormatter);

        Calendar calendar = Calendar.getInstance();
        Date date1 = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        calendar.setTime(date1);
        return calendar;
    }

    private Calendar convertDateToCalendar(LocalDate dateToConvert) {
        Calendar calendar = Calendar.getInstance();
        Date date = Date.from(dateToConvert.atStartOfDay(ZoneId.systemDefault()).toInstant());
        calendar.setTime(date);
        return calendar;
    }

    public boolean isPaymentDateLtOrEqToday(String paymentDate) {
        Calendar paymentDateCalendar = this.convertDateToCalendar(paymentDate);
        paymentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendar.set(Calendar.MINUTE, 0);
        paymentDateCalendar.set(Calendar.SECOND, 0);
        paymentDateCalendar.set(Calendar.MILLISECOND, 0);

        Calendar calendarForToday = Calendar.getInstance();
        calendarForToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarForToday.set(Calendar.MINUTE, 0);
        calendarForToday.set(Calendar.SECOND, 0);
        calendarForToday.set(Calendar.MILLISECOND, 0);
        return (paymentDateCalendar.compareTo(calendarForToday) <= 0);
//        return (paymentDate.equalsIgnoreCase("2022-04-04T00:00:00.000+01:00"));
    }

    public boolean isPaymentDateGtOrEqToday(String paymentDate) {
        Calendar paymentDateCalendar = this.convertDateToCalendar(paymentDate);
        paymentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendar.set(Calendar.MINUTE, 0);
        paymentDateCalendar.set(Calendar.SECOND, 0);
        paymentDateCalendar.set(Calendar.MILLISECOND, 0);

        Calendar calendarForToday = Calendar.getInstance();
        calendarForToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarForToday.set(Calendar.MINUTE, 0);
        calendarForToday.set(Calendar.SECOND, 0);
        calendarForToday.set(Calendar.MILLISECOND, 0);
        return (paymentDateCalendar.compareTo(calendarForToday) >= 0);
    }

    public boolean isPaymentDateWithinDaysNumber(String paymentDate, int numberOfDays) {
        Calendar paymentDateCalendarWithAddedDays = this.convertDateToCalendar(paymentDate);
        paymentDateCalendarWithAddedDays.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.MINUTE, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.SECOND, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.MILLISECOND, 0);

        Calendar calendarForTodayWithAddedDays = Calendar.getInstance();
        calendarForTodayWithAddedDays.set(Calendar.HOUR_OF_DAY, 0);
        calendarForTodayWithAddedDays.set(Calendar.MINUTE, 0);
        calendarForTodayWithAddedDays.set(Calendar.SECOND, 0);
        calendarForTodayWithAddedDays.set(Calendar.MILLISECOND, 0);
        calendarForTodayWithAddedDays.add(Calendar.DATE, numberOfDays);
        return (paymentDateCalendarWithAddedDays.compareTo(calendarForTodayWithAddedDays) == 0);
    }

    public boolean isPaymentDateLateWithinDaysNumber(String paymentDate, int numberOfDays) {
        Calendar paymentDateCalendarWithAddedDays = this.convertDateToCalendar(paymentDate);
        paymentDateCalendarWithAddedDays.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.MINUTE, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.SECOND, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.MILLISECOND, 0);

        Calendar calendarForTodayWithAddedDays = Calendar.getInstance();
        calendarForTodayWithAddedDays.set(Calendar.HOUR_OF_DAY, 0);
        calendarForTodayWithAddedDays.set(Calendar.MINUTE, 0);
        calendarForTodayWithAddedDays.set(Calendar.SECOND, 0);
        calendarForTodayWithAddedDays.set(Calendar.MILLISECOND, 0);
        calendarForTodayWithAddedDays.add(Calendar.DATE, numberOfDays);
        return (paymentDateCalendarWithAddedDays.compareTo(calendarForTodayWithAddedDays) == 0);
    }

    public boolean isPaymentDateWithinDaysNumber(LocalDate paymentDate, int numberOfDays) {
        Calendar paymentDateCalendarWithAddedDays = this.convertDateToCalendar(paymentDate);
        paymentDateCalendarWithAddedDays.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.MINUTE, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.SECOND, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.MILLISECOND, 0);

        Calendar calendarForTodayWithAddedDays = Calendar.getInstance();
        calendarForTodayWithAddedDays.set(Calendar.HOUR_OF_DAY, 0);
        calendarForTodayWithAddedDays.set(Calendar.MINUTE, 0);
        calendarForTodayWithAddedDays.set(Calendar.SECOND, 0);
        calendarForTodayWithAddedDays.set(Calendar.MILLISECOND, 0);
        calendarForTodayWithAddedDays.add(Calendar.DATE, numberOfDays);
        return (paymentDateCalendarWithAddedDays.compareTo(calendarForTodayWithAddedDays) == 0);
    }

    public boolean isPaymentDateBeforeOrWithinNumber(LocalDate paymentDate, int numberOfDays) {
        Calendar paymentDateCalendarWithAddedDays = this.convertDateToCalendar(paymentDate);
        paymentDateCalendarWithAddedDays.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.MINUTE, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.SECOND, 0);
        paymentDateCalendarWithAddedDays.set(Calendar.MILLISECOND, 0);

        Calendar calendarForTodayWithAddedDays = Calendar.getInstance();
        calendarForTodayWithAddedDays.set(Calendar.HOUR_OF_DAY, 0);
        calendarForTodayWithAddedDays.set(Calendar.MINUTE, 0);
        calendarForTodayWithAddedDays.set(Calendar.SECOND, 0);
        calendarForTodayWithAddedDays.set(Calendar.MILLISECOND, 0);
        calendarForTodayWithAddedDays.add(Calendar.DATE, numberOfDays);
        return (paymentDateCalendarWithAddedDays.compareTo(calendarForTodayWithAddedDays) <= 0);
    }



    public boolean isPaymentDateToday(String paymentDate) {
        Calendar paymentDateCalendar = this.convertDateToCalendar(paymentDate);
        paymentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendar.set(Calendar.MINUTE, 0);
        paymentDateCalendar.set(Calendar.SECOND, 0);
        paymentDateCalendar.set(Calendar.MILLISECOND, 0);

        Calendar calendarForToday = Calendar.getInstance();
        calendarForToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarForToday.set(Calendar.MINUTE, 0);
        calendarForToday.set(Calendar.SECOND, 0);
        calendarForToday.set(Calendar.MILLISECOND, 0);
        return (paymentDateCalendar.compareTo(calendarForToday) == 0);
    }

    public boolean paymentDateMatches(String paymentDate, LocalDate dateToCheck) {
        Calendar paymentDateCalendar = this.convertDateToCalendar(paymentDate);
        paymentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendar.set(Calendar.MINUTE, 0);
        paymentDateCalendar.set(Calendar.SECOND, 0);
        paymentDateCalendar.set(Calendar.MILLISECOND, 0);

        Calendar calendarForToday = this.convertDateToCalendar(dateToCheck);
        calendarForToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarForToday.set(Calendar.MINUTE, 0);
        calendarForToday.set(Calendar.SECOND, 0);
        calendarForToday.set(Calendar.MILLISECOND, 0);
        return (paymentDateCalendar.compareTo(calendarForToday) == 0);
    }

    public boolean isPaymentDateLtToday(String paymentDate) {
        Calendar paymentDateCalendar = this.convertDateToCalendar(paymentDate);
        paymentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendar.set(Calendar.MINUTE, 0);
        paymentDateCalendar.set(Calendar.SECOND, 0);
        paymentDateCalendar.set(Calendar.MILLISECOND, 0);

        Calendar calendarForToday = Calendar.getInstance();
        calendarForToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarForToday.set(Calendar.MINUTE, 0);
        calendarForToday.set(Calendar.SECOND, 0);
        calendarForToday.set(Calendar.MILLISECOND, 0);
        return (paymentDateCalendar.compareTo(calendarForToday) < 0);
    }

    public boolean isPaymentDateGtToday(String paymentDate) {
        Calendar paymentDateCalendar = this.convertDateToCalendar(paymentDate);
        paymentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendar.set(Calendar.MINUTE, 0);
        paymentDateCalendar.set(Calendar.SECOND, 0);
        paymentDateCalendar.set(Calendar.MILLISECOND, 0);

        Calendar calendarForToday = Calendar.getInstance();
        calendarForToday.set(Calendar.HOUR_OF_DAY, 0);
        calendarForToday.set(Calendar.MINUTE, 0);
        calendarForToday.set(Calendar.SECOND, 0);
        calendarForToday.set(Calendar.MILLISECOND, 0);
        return (paymentDateCalendar.compareTo(calendarForToday) > 0);
    }

    public boolean isPaymentDateWithinCurrentMonth(String paymentDate) {
        Calendar calendar = this.convertDateToCalendar(paymentDate);
        int paymentDateMonth = calendar.get(Calendar.MONTH);
        Calendar todaysCalendar = Calendar.getInstance();
        int todaysDateMonth = todaysCalendar.get(Calendar.MONTH);
        return (paymentDateMonth == todaysDateMonth);
    }

    public boolean isPaymentDateWithin(String paymentDate, LocalDate firstDate, LocalDate secondDate) {
        Calendar paymentDateCalendar = this.convertDateToCalendar(paymentDate);
        paymentDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        paymentDateCalendar.set(Calendar.MINUTE, 0);
        paymentDateCalendar.set(Calendar.SECOND, 0);
        paymentDateCalendar.set(Calendar.MILLISECOND, 0);

        Calendar firstDateCalendar = this.convertDateToCalendar(firstDate);
        firstDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        firstDateCalendar.set(Calendar.MINUTE, 0);
        firstDateCalendar.set(Calendar.SECOND, 0);
        firstDateCalendar.set(Calendar.MILLISECOND, 0);

        Calendar secondDateCalendar = this.convertDateToCalendar(secondDate);
        secondDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        secondDateCalendar.set(Calendar.MINUTE, 0);
        secondDateCalendar.set(Calendar.SECOND, 0);
        secondDateCalendar.set(Calendar.MILLISECOND, 0);
        return (
                paymentDateCalendar.compareTo(firstDateCalendar) >= 0
                && paymentDateCalendar.compareTo(secondDateCalendar) <= 0
        );
    }

    public String getMonthByDate(String paymentDate) throws CustomCheckedException {
        Calendar calendar = this.convertDateToCalendar(paymentDate);
        int paymentDateMonth = calendar.get(Calendar.MONTH);
        return getMonthStringByIntIdentifier(paymentDateMonth);
    }

    public int getYearByDate(String paymentDate) {
        Calendar calendar = this.convertDateToCalendar(paymentDate);
        return calendar.get(Calendar.YEAR);
    }

    private String getMonthStringByIntIdentifier(int month) throws CustomCheckedException {
        switch (month) {
            case 0:
                return "January";
            case 1:
                return "February";
            case 2:
                return "March";
            case 3:
                return "April";
            case 4:
                return "May";
            case 5:
                return "June";
            case 6:
                return "July";
            case 7:
                return "August";
            case 8:
                return "September";
            case 9:
                return "October";
            case 10:
                return "November";
            case 11:
                return "December";
            default:
                throw new CustomCheckedException("Invalid month identifier provided");
        }
    }

    public LocalDate convertDateToLocalDate(String date) {
        var dueDate = date.split("T")[0];
        return LocalDate.parse(dueDate);
    }

    public  String convertLocalDateToString(String date){
        var dueDate = date.split("T")[0];
         LocalDate localDate= LocalDate.parse(dueDate);
         return convertDateToYear(localDate);
    }

    public String convertDateToYear(LocalDate localDate){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy");
        return localDate.format(formatter);
    }
    public String newDate(){
        LocalDate localDate = LocalDate.now();//For reference
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyy");
        return localDate.format(formatter);
    }

    public String getTimeStamp(){
        return new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss'+000000'").format(Calendar.getInstance().getTime());
    }

    public LocalDateTime getDate(){
        ZoneId zoneId = ZoneId.of ( "Africa/Lagos" );
        LocalDateTime ldt = new Date().toInstant().atZone(zoneId).toLocalDateTime();
        //Get current date time
        LocalDateTime now = LocalDateTime.now();

        System.out.println("Before : " + now);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(GeneralUtil.DatePattern.thirdPattern);

        String formatDateTime2 = ldt.format(formatter);

        System.out.println("After : " + formatDateTime2);

        return ldt;
    }

    public static String dateFormatter(String sDate){
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        sdf1.setTimeZone(TimeZone.getTimeZone("Africa/Lagos"));
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyyy",Locale.ENGLISH);
        sdf2.setTimeZone(TimeZone.getTimeZone("Africa/Lagos"));
        String ds2 = null;
        try {
            ds2 = sdf2.format(sdf1.parse(sDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ds2;
    }

    public static  String currentDate(){
        LocalDate localDate = LocalDate.now();//For reference
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return localDate.format(formatter);
    }

    public boolean compareDates(String setDate,String loanCreatedDate) {
        boolean applyCharge = false;
        try{
            // Create 2 dates starts
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date date1 = sdf.parse(setDate);
            Date date2 = sdf.parse(loanCreatedDate);
            // after() will return true if and only if date1 is after or equal date 2
            if(date2.after(date1) || date1.equals(date2)){
                System.out.println("validated and okay");
                applyCharge = true;
            }
        }
        catch(ParseException ex){
            ex.printStackTrace();
            applyCharge = false;
        }
        return applyCharge;
    }

    //2022-03-16
    public LocalDate formatDateToLocalDate(Date inputDate){
         return LocalDate.ofInstant(inputDate.toInstant(), ZoneId.systemDefault());
    }


}
