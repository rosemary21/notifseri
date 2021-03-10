package com.creditville.notifications.models.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by Chuks on 02/07/2021.
 */
@Getter
@Setter
public class Client implements Serializable {
    private String externalID;
    private String name;
    private String email;
    private String clientStatus;
    private String branch;
}
