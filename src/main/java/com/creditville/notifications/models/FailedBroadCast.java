package com.creditville.notifications.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
public class FailedBroadCast {

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Lob
    private String emailAddress;

}
