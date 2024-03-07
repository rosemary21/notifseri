package com.creditville.notifications.pushnotifications.service;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.pushnotifications.dto.EmailSmsReq;
import com.creditville.notifications.pushnotifications.dto.PushNotificationRequest;
import com.creditville.notifications.pushnotifications.dto.PushNotificationResponse;
import com.creditville.notifications.pushnotifications.dto.SubscribeTopicDto;
import com.google.firebase.messaging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;


public interface PushNotificationService  {

    PushNotificationResponse sendPushNotificationToDevice(PushNotificationRequest request, String token);
    PushNotificationResponse sendPushNotificationToTopic(PushNotificationRequest request,String topic);

    PushNotificationResponse subscribeToTopic(SubscribeTopicDto dto);
    PushNotificationResponse unSubscribeToTopic(SubscribeTopicDto dto);

    void sendEmailOrSmsOrBothNotification(EmailSmsReq emailSmsReq) throws CustomCheckedException;


}
