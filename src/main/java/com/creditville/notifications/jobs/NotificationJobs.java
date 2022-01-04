//package com.creditville.notifications.jobs;
//
//import com.creditville.notifications.exceptions.CustomCheckedException;
//import com.creditville.notifications.models.NotificationGeneralConfig;
//import com.creditville.notifications.models.NotificationType;
//import com.creditville.notifications.services.DispatcherService;
//import com.creditville.notifications.services.NotificationConfigService;
//import com.creditville.notifications.services.PartialDebitService;
//import com.creditville.notifications.services.TransferService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
///**
// * Created by Chuks on 02/06/2021.
// */
//@Slf4j
//@Component
//public class NotificationJobs {
//
//    @Autowired
//    private DispatcherService dispatcherService;
//
//    @Value("${app.schedule.recurringCharges.enabled}")
//    private Boolean recurringChargesEnabled;
//
//    @Value("${app.schedule.mandateDebitInstruction.enabled}")
//    private Boolean mandateDebitInstructionEnabled;
//
//    @Value("${app.schedule.partialDebit.enabled}")
//    private Boolean partialDebitEnabled;
//
//    @Autowired
//    private PartialDebitService partialDebitService;
//
//    @Autowired
//    private NotificationConfigService notificationConfigService;
//
//    @Autowired
//    TransferService transferService;
//
//       // @Async("schedulePool1")
//    @Scheduled(cron = "${app.schedule.dueRentalOne}")
//   // @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void dueRentalNotification() {
//        try {
//            NotificationGeneralConfig dueRentalOneConfig = notificationConfigService.getNotificationGeneralConfig(NotificationType.DUE_RENTAL_ONE.name());
//            if(dueRentalOneConfig.getIsEnabled())
//                dispatcherService.performDueRentalOperation();
//            else
//                log.info("Schedule for due rental one has reached it's schedule time but notification is disabled from configuration".toUpperCase());
//        }catch (CustomCheckedException cce) {
//            cce.printStackTrace();
//            log.info(cce.getMessage());
//        }
//    }
//
//      //  @Async("schedulePool2")
//    @Scheduled(cron = "${app.schedule.dueRentalTwo}")
////    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void dueRentalNotification2() {
//        try {
//            NotificationGeneralConfig dueRentalTwoConfig = notificationConfigService.getNotificationGeneralConfig(NotificationType.DUE_RENTAL_TWO.name());
//            if(dueRentalTwoConfig.getIsEnabled())
//                dispatcherService.performDueRentalTwoOperation();
//            else
//                log.info("Schedule for due rental two has reached it's schedule time but notification is disabled from configuration".toUpperCase());
//        }catch (CustomCheckedException cce) {
//            cce.printStackTrace();
//            log.info(cce.getMessage());
//        }
//    }
//
//       // @Async("schedulePool3")
//    @Scheduled(cron = "${app.schedule.dueRentalThree}")
////    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void dueRentalNotification3() {
//        try {
//            NotificationGeneralConfig dueRentalThreeConfig = notificationConfigService.getNotificationGeneralConfig(NotificationType.DUE_RENTAL_THREE.name());
//            if(dueRentalThreeConfig.getIsEnabled())
//                dispatcherService.performDueRentalThreeOperation();
//            else
//                log.info("Schedule for due rental three has reached it's schedule time but notification is disabled from configuration".toUpperCase());
//        }catch (CustomCheckedException cce) {
//            cce.printStackTrace();
//            log.info(cce.getMessage());
//        }
//    }
//
//
//    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void disburselaon() {
//        try {
//            transferService.disburseLoan();
//        }catch (Exception cce) {
//            cce.printStackTrace();
//            log.info(cce.getMessage());
//        }
//    }
//
//       // @Async("schedulePool4")
//    @Scheduled(cron = "${app.schedule.arrears}")
////    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void arrearsNotification() {
//        try {
//            NotificationGeneralConfig arrearsConfig = notificationConfigService.getNotificationGeneralConfig(NotificationType.ARREARS.name());
//            if(arrearsConfig.getIsEnabled())
//                dispatcherService.performArrearsOperation();
//            else
//                log.info("Schedule for arrears has reached it's schedule time but notification is disabled from configuration".toUpperCase());
//        }catch (CustomCheckedException cce) {
//            cce.printStackTrace();
//            log.info(cce.getMessage());
//        }
//    }
//
//       // @Async("schedulePool5")
//    @Scheduled(cron = "${app.schedule.postMaturity}")
////    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void postMaturityNotification() {
//        try {
//            NotificationGeneralConfig postMaturityConfig = notificationConfigService.getNotificationGeneralConfig(NotificationType.POST_MATURITY.name());
//            if(postMaturityConfig.getIsEnabled())
//                dispatcherService.performPostMaturityOperation();
//            else
//                log.info("Schedule for post maturity has reached it's schedule time but notification is disabled from configuration".toUpperCase());
//        }catch (CustomCheckedException cce) {
//            cce.printStackTrace();
//            log.info(cce.getMessage());
//        }
//    }
//
//    //    @Async("schedulePool6")
//    @Scheduled(cron = "${app.schedule.chequeLodgement}")
////    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void chequeLodgementNotification() {
//        try {
//            NotificationGeneralConfig chequeLodgementConfig = notificationConfigService.getNotificationGeneralConfig(NotificationType.CHEQUE_LODGEMENT.name());
//            if(chequeLodgementConfig.getIsEnabled())
//                dispatcherService.performChequeLodgementOperation();
//            else
//                log.info("Schedule for cheque lodgement has reached it's schedule time but notification is disabled from configuration".toUpperCase());
//        }catch (CustomCheckedException cce) {
//            cce.printStackTrace();
//            log.info(cce.getMessage());
//        }
//    }
////
//    @Scheduled(cron = "${app.schedule.recurringCharges}")
////    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void recurringChargesNotification() {
//        if(recurringChargesEnabled)
//            dispatcherService.performRecurringChargesOperation();
//        else log.info("Schedule for recurring charges has reached it's schedule time but notification is disabled from configuration".toUpperCase());
//    }
//
//    @Scheduled(cron = "${app.schedule.partialDebit}")
////    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void partialDebitOperation() {
//        if(partialDebitEnabled)
//            partialDebitService.performPartialDebitOp();
//        else log.info("Schedule for partial debit operation has reached it's schedule time but is operation is disabled from configuration".toUpperCase());
//    }
//
////    @Scheduled(cron = "${app.schedule.recurringCharges}")
//    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void mandateDebitInstruction() {
//        if(mandateDebitInstructionEnabled)
//            dispatcherService.performRecurringMandateDebitInstruction();
//        else log.info("Schedule for recurring mandate charge (remita) has reached it's schedule time but notification is disabled from configuration".toUpperCase());
//    }
//
//    @Scheduled(cron = "${app.schedule.notifyTeam}")
////    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
//    public void notifyTeamOperation() {
//        try {
//            dispatcherService.notifyTeamOfOperation();
//        }catch (CustomCheckedException cce) {
//            log.info("An error occurred while trying to notify team of operation");
//        }
//    }
//}
