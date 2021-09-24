package com.creditville.notifications.sms.dto;

import javax.persistence.Lob;

public class RequestDTO {
    private String dest;
    private String src;
    @Lob
    private String text;
    private String ticketId;
    private boolean unicode;

    @Override
    public String toString() {
        return "RequestDTO{" +
                "dest='" + dest + '\'' +
                ", src='" + src + '\'' +
                ", text='" + text + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", unicode=" + unicode +
                '}';
    }

    public String getDest() {
        return dest;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

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

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public boolean isUnicode() {
        return unicode;
    }

    public void setUnicode(boolean unicode) {
        this.unicode = unicode;
    }
}
