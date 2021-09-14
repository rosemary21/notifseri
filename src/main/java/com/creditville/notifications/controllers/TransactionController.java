package com.creditville.notifications.controllers;

import com.creditville.notifications.exceptions.CustomCheckedException;
import com.creditville.notifications.models.requests.HookEvent;
import com.creditville.notifications.models.response.SuccessResponse;
import com.creditville.notifications.services.TransactionService;
import com.creditville.notifications.utils.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/transaction")
@RestController
public class TransactionController {
    @Autowired
    private ValidationUtil validationUtil;

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/paystack/receive-hook-events")
    public ResponseEntity<?> receivePaystackHookEvents(@RequestBody HookEvent hookEvent, HttpServletRequest httpServletRequest) throws CustomCheckedException {
        validationUtil.validatePaystackRequest(httpServletRequest);
        transactionService.handlePaystackTransactionEvent(hookEvent);
        return new ResponseEntity<>(new SuccessResponse("Event received successfully", null), HttpStatus.OK);
    }

    @PostMapping("/remitta/receive-hook-events")
    public ResponseEntity<?> receiveRemittaHookEvents(@RequestBody HookEvent hookEvent, HttpServletRequest httpServletRequest) throws CustomCheckedException {
        transactionService.handlePaystackTransactionEvent(hookEvent);
        return new ResponseEntity<>(new SuccessResponse("Event received successfully", null), HttpStatus.OK);
    }
}
