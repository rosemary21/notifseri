package com.creditville.notifications.disburse.model;

import lombok.Data;

import java.util.List;

@Data
public class ListTransferStatus {
    private boolean status;
    private String message;
    private List<ListenRecipientData> data;
}
