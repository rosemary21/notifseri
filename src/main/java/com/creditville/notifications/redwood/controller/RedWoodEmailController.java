package com.creditville.notifications.redwood.controller;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.response.SuccessResponse;
import com.creditville.notifications.redwood.model.EmailRequest;
import com.creditville.notifications.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/redwood")
@RestController
public class RedWoodEmailController {

    @Autowired
    private NotificationService notificationService;

    @CrossOrigin
    @PostMapping("/send")
    public ResponseEntity<?> sendOutEmail(@Valid @RequestBody EmailRequest sendEmailRequest) throws CustomCheckedException {
        System.out.println("Äbout sending email address"+sendEmailRequest.getEmail());
        System.out.println("Äbout sending name"+sendEmailRequest.getName());
        notificationService.sendEmailNotification(sendEmailRequest);
        return new ResponseEntity<>(new SuccessResponse("Mail dispatched successfully"), HttpStatus.OK);
    }
}
