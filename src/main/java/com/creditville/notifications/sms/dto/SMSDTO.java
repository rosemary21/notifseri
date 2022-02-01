package com.creditville.notifications.sms.dto;

public class SMSDTO {

    RequestDTO sms;

    @Override
    public String toString() {
        return "SMSDTO{" +
                "sms=" + sms +
                '}';
    }

    public RequestDTO getSms() {
        return sms;
    }

    public void setSms(RequestDTO sms) {
        this.sms = sms;
    }
}
