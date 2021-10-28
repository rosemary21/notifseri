package com.creditville.notifications.models.requests;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class CreatePdRecord implements Serializable {
    private LocalDate startDate;
    private LocalDate endDate;
}
