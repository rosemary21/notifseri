package com.creditville.notifications;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.utils.DateUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@SpringBootTest
class NotificationsApplicationTests {
    @Autowired
    private DateUtil dateUtil;

    @Test
    void contextLoads() {
    }

    @Test
    void currencyFormatTest() {
        System.out.println(NumberFormat.getCurrencyInstance(new Locale("en", "NG"))
                .format(new BigDecimal("10000")));
        System.out.println("Year: " + dateUtil.getYearByDate("2021-03-12T00:00:00.000+01:00"));
    }

}
