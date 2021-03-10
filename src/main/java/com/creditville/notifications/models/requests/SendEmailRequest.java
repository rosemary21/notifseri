package com.creditville.notifications.models.requests;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
public class SendEmailRequest implements Serializable {
    @NotNull(message = "Mail subject cannot be null")
    private String mailSubject;
    private String mailMessage;
    @NotNull(message = "Mail data is required")
    private ObjectNode mailData;
    @NotNull(message = "Mail template is required")
    private String mailTemplate;
}
