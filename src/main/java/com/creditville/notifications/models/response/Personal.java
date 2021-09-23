package com.creditville.notifications.models.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class Personal implements Serializable {
    private String firstName;
    private String lastName;
    private String middleName;
    private String dateOfBirth;
}
