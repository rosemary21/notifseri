package com.creditville.notifications.jobs;

import com.creditville.notifications.services.DispatcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Chuks on 02/06/2021.
 */
@Slf4j
@Component
public class NotificationJobs {
    @Autowired
    private DispatcherService dispatcherService;

    @Value("${app.schedule.dueRentalOne.enabled}")
    private Boolean dueRentalOneEnabled;
    
    @Value("${app.schedule.dueRentalTwo.enabled}")
    private Boolean dueRentalTwoEnabled;
    
    @Value("${app.schedule.dueRentalThree.enabled}")
    private Boolean dueRentalThreeEnabled;
    
    @Value("${app.schedule.arrears.enabled}")
    private Boolean arrearsEnabled;
    
    @Value("${app.schedule.postMaturity.enabled}")
    private Boolean postMaturityEnabled;
    
    @Value("${app.schedule.chequeLodgement.enabled}")
    private Boolean chequeLodgementEnabled;
    
    @Value("${app.schedule.recurringCharges.enabled}")
    private Boolean recurringChargesEnabled;

//    @Async("schedulePool1")
    @Scheduled(cron = "${app.schedule.dueRentalOne}")
//    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
    public void dueRentalNotification() {
        if(dueRentalOneEnabled)
            dispatcherService.performDueRentalOperation();
        else log.info("Schedule for due rental one has reached it's schedule time but notification is disabled from configuration".toUpperCase());
    }

//    @Async("schedulePool2")
    @Scheduled(cron = "${app.schedule.dueRentalTwo}")
//    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
    public void dueRentalNotification2() {
        if(dueRentalTwoEnabled)
            dispatcherService.performDueRentalTwoOperation();
        else log.info("Schedule for due rental two has reached it's schedule time but notification is disabled from configuration".toUpperCase());
    }

//    @Async("schedulePool3")
    @Scheduled(cron = "${app.schedule.dueRentalThree}")
//    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
    public void dueRentalNotification3() {
        if(dueRentalThreeEnabled)
            dispatcherService.performDueRentalThreeOperation();
        else log.info("Schedule for due rental three has reached it's schedule time but notification is disabled from configuration".toUpperCase());
    }

//    @Async("schedulePool4")
    @Scheduled(cron = "${app.schedule.arrears}")
//    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
    public void arrearsNotification() {
        if(arrearsEnabled)
            dispatcherService.performArrearsOperation();
        else log.info("Schedule for arrears has reached it's schedule time but notification is disabled from configuration".toUpperCase());
    }

//    @Async("schedulePool5")
    @Scheduled(cron = "${app.schedule.postMaturity}")
//    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
    public void postMaturityNotification() {
        if(postMaturityEnabled)
            dispatcherService.performPostMaturityOperation();
        else log.info("Schedule for post maturity has reached it's schedule time but notification is disabled from configuration".toUpperCase());
    }

//    @Async("schedulePool6")
    @Scheduled(cron = "${app.schedule.chequeLodgement}")
//    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
    public void chequeLodgementNotification() {
        if(chequeLodgementEnabled)
            dispatcherService.performChequeLodgementOperation();
        else log.info("Schedule for cheque lodgement has reached it's schedule time but notification is disabled from configuration".toUpperCase());
    }

    @Scheduled(cron = "${app.schedule.recurringCharges}")
//    @Scheduled(cron = "${app.schedule.everyThirtySeconds}")
    public void recurringChargesNotification() {
        if(recurringChargesEnabled)
            dispatcherService.performRecurringChargesOperation();
        else log.info("Schedule for recurring charges has reached it's schedule time but notification is disabled from configuration".toUpperCase());
    }
}
