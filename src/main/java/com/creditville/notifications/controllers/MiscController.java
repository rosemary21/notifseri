package com.creditville.notifications.controllers;

import com.creditville.notifications.models.requests.CreatePdRecord;
import com.creditville.notifications.models.response.ErrorResponse;
import com.creditville.notifications.models.response.SuccessResponse;
import com.creditville.notifications.services.DispatcherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequestMapping("/api/misc")
@RestController
public class MiscController {
    @Autowired
    private DispatcherService dispatcherService;

    @PostMapping("/create-pd-record-for-arrears-loans")
    public ResponseEntity<?> createPdRecordForArrearsLoans(@RequestBody CreatePdRecord record) {
        dispatcherService.performMiscOperation(record.getStartDate(), record.getEndDate());
        return new ResponseEntity<>(new SuccessResponse("Operation successful", null), HttpStatus.OK);
    }

    @GetMapping("/send-out-holiday-mail/{name}")
    public ResponseEntity<?> sendOutHolidayMail(@PathVariable String name) {
        if(name.equals("eid-el_kabir")) {
            dispatcherService.sendOutEidNotification();
            return new ResponseEntity<>(new SuccessResponse("Operation successful", null), HttpStatus.OK);
        }else return new ResponseEntity<>(new ErrorResponse("Operation failed with error: Invalid name provided"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
