package com.creditville.notifications.controllers;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.requests.SendEmailRequest;
import com.creditville.notifications.models.response.SuccessResponse;
import com.creditville.notifications.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequestMapping("/api/email")
@RestController
public class EmailController {
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/send")
    public ResponseEntity<?> sendOutEmail(@Valid @RequestBody SendEmailRequest sendEmailRequest) throws CustomCheckedException {
        notificationService.sendEmailNotification(sendEmailRequest);
        return new ResponseEntity<>(new SuccessResponse("Mail dispatched successfully"), HttpStatus.OK);
    }

    @PostMapping("/send-broadcast-message")
    public ResponseEntity<?> sendOutBroadcastEmail(@Valid @RequestBody SendEmailRequest sendEmailRequest) throws CustomCheckedException {
        notificationService.sendEmailBroadcastNotification(sendEmailRequest);
        return new ResponseEntity<>(new SuccessResponse("Mails dispatched successfully"), HttpStatus.OK);
    }
}
