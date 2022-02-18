package com.creditville.notifications;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.repositories.CardTransactionRepository;
import com.creditville.notifications.services.NotificationService;
import com.creditville.notifications.services.PartialDebitService;
import com.creditville.notifications.services.RemitaService;
import com.creditville.notifications.utils.DateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.*;

@SpringBootTest
class NotificationsApplicationTests {
    @Autowired
    private DateUtil dateUtil;

    @Autowired
    private PartialDebitService partialDebitService;

    @Autowired
    private CardTransactionRepository cardTransactionRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RemitaService remitaService;
    @Autowired
    private ObjectMapper om;

    @Test
    void contextLoads() {
    }

    @Test
    void currencyFormatTest() {
        System.out.println(NumberFormat.getCurrencyInstance(new Locale("en", "NG"))
                .format(new BigDecimal("10000")));
        System.out.println("Year: " + dateUtil.getYearByDate("2021-03-12T00:00:00.000+01:00"));
        System.out.println("Within: " + dateUtil.isPaymentDateWithin("2021-05-12T00:00:00.000+01:00", LocalDate.now().minusDays(19), LocalDate.now()));
        BigDecimal bigDecimal = new BigDecimal("-50");
        System.out.println("Response "+ (bigDecimal.compareTo(BigDecimal.ZERO) < 0));
    }

    @Test
    void thirtyPercentOfAmountTest() {
        System.out.println("Division test: "+ new BigDecimal(65033).divide(new BigDecimal(100)).setScale(2, RoundingMode.CEILING));
        System.out.println("Amount gotten: "+ partialDebitService.getLeastPartialDebitAmount(new BigDecimal(55000.10)));

        ObjectNode on = new ObjectNode(JsonNodeFactory.instance);
        on.put("test", "This is a test");
        System.out.println("ON: "+ on.get("test").toString());
        System.out.println("ON TXT: "+ on.get("test").textValue());
    }

    @Test
    void sendDummyNotification() {
//        try {
//            Map<String, String> notificationData = new HashMap<>();
//            notificationData.put("toAddress", "david.udechukwu@creditville.ng");
//            notificationData.put("toName", "David Udechukwu");
//            notificationData.put("totalSuccessfulCount", String.valueOf(12));
//            notificationData.put("totalFailedCount", String.valueOf(0));
//            notificationData.put("operationName", "Test OP");
//            notificationService.sendEmailNotification("DAILY TEST REPORT", notificationData, "email/dispatched_mails");
//        }catch (CustomCheckedException cce) {
//            cce.printStackTrace();
//        }
    }

    @Test
    void getAllActiveMandate() throws JsonProcessingException {
        var resp = remitaService.getAllActiveMandates(0,100);
        System.out.println("resp: "+om.writerWithDefaultPrettyPrinter().writeValueAsString(resp));
    }

    @Test
    void getMandate() throws JsonProcessingException {
        List result=new ArrayList();
        result.add("pending");
        var resp = cardTransactionRepository.findByRemitaRequestIdAndMandateIdAndStatusInAndTransactionDate(" ","260606348065",result,"2022-01-26");
        System.out.println("resp: "+om.writerWithDefaultPrettyPrinter().writeValueAsString(resp));
    }

//    @Test
//    void instalment(){
//        var resp = remitaService.isLoanRePaid("01500099113");
//    }

}
