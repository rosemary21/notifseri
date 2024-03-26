package com.creditville.notifications.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class EmailTemplate {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Lob
    private String templateMessage;

    private String sender;

    private String enableBroadcast="N";
    private String emailSubject;
    @Lob
    private String failedEmail;

    private String unregisteredTemplate;

    private String enableUnregistered="N";

}
