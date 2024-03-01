package com.creditville.notifications.pushnotifications.service.impl;

import com.creditville.notifications.pushnotifications.dto.PushNotificationRequest;
import com.creditville.notifications.pushnotifications.dto.PushNotificationResponse;
import com.creditville.notifications.pushnotifications.dto.SubscribeTopicDto;
import com.creditville.notifications.pushnotifications.service.PushNotificationService;
import com.google.firebase.messaging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
@Service
public class PushNotificationServiceImpl implements PushNotificationService {
    @Autowired
    FirebaseMessaging fcmMessage;
    @Autowired
    MessageSource messageSource;


    public PushNotificationResponse sendPushNotificationToDevice(PushNotificationRequest request, String token) {
        PushNotificationResponse pushResp = new PushNotificationResponse();

        Notification notification = Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getMessage())
                .build();

        Message message = Message.builder()
                .setNotification(notification)
                .setToken(token)
                .build();

        try {
            fcmMessage.send(message);
            pushResp.setCode(messageSource.getMessage("service.success.code",null, Locale.ENGLISH));
            pushResp.setMessage(messageSource.getMessage("push.notification.sent",null, Locale.ENGLISH));
            return pushResp;



        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            pushResp.setCode(messageSource.getMessage("service.error.code",null, Locale.ENGLISH));
            pushResp.setMessage(messageSource.getMessage("push.notification.error",null, Locale.ENGLISH));
            return pushResp;
        }
    }
    public PushNotificationResponse sendPushNotificationToTopic(PushNotificationRequest request,String topic) {

        PushNotificationResponse pushResp = new PushNotificationResponse();

        Notification notification = Notification
                .builder()
                .setTitle(request.getTitle())
                .setBody(request.getMessage())
                .build();

        Message message = Message.builder()
                .setNotification(notification)
                .setTopic(topic)
                .build();

        try {
            fcmMessage.send(message);
            pushResp.setCode(messageSource.getMessage("service.success.code",null, Locale.ENGLISH));
            pushResp.setMessage(messageSource.getMessage("push.notification.sent",null, Locale.ENGLISH));
            return pushResp;
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            pushResp.setCode(messageSource.getMessage("service.error.code",null, Locale.ENGLISH));
            pushResp.setMessage(messageSource.getMessage("push.notification.error",null, Locale.ENGLISH));
            return pushResp;
        }
    }

    public PushNotificationResponse subscribeToTopic(SubscribeTopicDto dto){
        PushNotificationResponse resp = new PushNotificationResponse();

        List<String> registrationTokens = Arrays.asList(
                dto.getFcmToken()        );
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
                    registrationTokens, dto.getTopic());

            resp.setCode(messageSource.getMessage("service.success.code",null, Locale.ENGLISH));
            resp.setMessage(response.getSuccessCount() + " token(s) have been subscribed to topic "+ dto.getTopic());
            return resp;


        } catch (FirebaseMessagingException e) {
            resp.setCode(messageSource.getMessage("service.error.code",null, Locale.ENGLISH));
            resp.setMessage("An error occurred while subscribing to topic " + e.getMessage());
            return resp;
        }

    }

    public PushNotificationResponse unSubscribeToTopic(SubscribeTopicDto dto){
        PushNotificationResponse resp = new PushNotificationResponse();


        List<String> registrationTokens = Arrays.asList(
                dto.getFcmToken()
        );
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(
                    registrationTokens, dto.getTopic());
            resp.setCode(messageSource.getMessage("service.success.code",null, Locale.ENGLISH));
            resp.setMessage(response.getSuccessCount() + " token(s) have been unsubscribed from topic "+ dto.getTopic());
            return resp;
        } catch (FirebaseMessagingException e) {
            resp.setCode(messageSource.getMessage("service.error.code",null, Locale.ENGLISH));
            resp.setMessage("An error occurred while unsubscribing to topic " + e.getMessage());
            return resp;
        }

    }



}
