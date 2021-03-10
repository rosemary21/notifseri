package com.creditville.notifications.utils;

import com.creditville.notifications.exceptions.CustomCheckedException;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Chuks on 02/09/2021.
 */
@Service
public class DateUtil {
    public Calendar convertDateToCalendar(String dateToConvert) {
        var newDate = dateToConvert.substring(0,23);
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH);
        LocalDate date = LocalDate.parse(newDate, inputFormatter);

        Calendar calendar = Calendar.getInstance();
        Date date1 = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        calendar.setTime(date1);
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
}
