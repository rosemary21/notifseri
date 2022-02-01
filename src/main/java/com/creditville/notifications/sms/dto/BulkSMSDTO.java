package com.creditville.notifications.sms.dto;

import java.util.ArrayList;
import java.util.List;

public class BulkSMSDTO {
    private String src;
    private String text;
    private List<String> destinations=new ArrayList<>();

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<String> destinations) {
        this.destinations = destinations;
    }
}
