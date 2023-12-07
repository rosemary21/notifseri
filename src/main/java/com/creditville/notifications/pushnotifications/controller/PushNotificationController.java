package com.creditville.notifications.pushnotifications.controller;

import com.creditville.notifications.pushnotifications.dto.PushNotificationRequest;
import com.creditville.notifications.pushnotifications.dto.PushNotificationResponse;
import com.creditville.notifications.pushnotifications.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/push/notification")
public class PushNotificationController {

    @Autowired
    PushNotificationService service;

    @PostMapping("/token/{token}")
    public PushNotificationResponse sendTokenNotification(@RequestBody PushNotificationRequest request, @PathVariable("token")String token) {
        var resp = service.sendPushNotificationToDevice(request, token);
        return resp;
    }

    @PostMapping("/topic/{topic}")
    public PushNotificationResponse sendTopicNotification(@RequestBody PushNotificationRequest request,@PathVariable("topic") String topic){
        var resp = service.sendPushNotificationToTopic(request, topic);
        return resp;
    }


}
